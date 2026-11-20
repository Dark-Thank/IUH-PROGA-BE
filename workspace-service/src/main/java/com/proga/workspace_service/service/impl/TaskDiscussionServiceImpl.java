package com.proga.workspace_service.service.impl;

import com.proga.workspace_service.dto.TaskNoteRequest;
import com.proga.workspace_service.dto.TaskNoteResponse;
import com.proga.workspace_service.dto.WorkspaceLogResponse;
import com.proga.workspace_service.model.Space;
import com.proga.workspace_service.model.Task;
import com.proga.workspace_service.model.TaskNote;
import com.proga.workspace_service.model.TaskStatus;
import com.proga.workspace_service.model.WorkspaceLog;
import com.proga.workspace_service.repository.SpaceRepository;
import com.proga.workspace_service.repository.TaskNoteRepository;
import com.proga.workspace_service.repository.TaskRepository;
import com.proga.workspace_service.repository.WorkspaceLogRepository;
import com.proga.workspace_service.service.TaskDiscussionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskDiscussionServiceImpl implements TaskDiscussionService {

    private final TaskNoteRepository taskNoteRepository;
    private final WorkspaceLogRepository workspaceLogRepository;
    private final TaskRepository taskRepository;
    private final SpaceRepository spaceRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.proga.workspace_service.service.AppNotificationService appNotificationService;

    @Override
    public List<TaskNoteResponse> getNotesByTaskId(long taskId) {
        Task task = taskRepository.findById(taskId).orElse(null);
        String taskTitle = task != null ? task.getTitle() : "Task #" + taskId;

        return taskNoteRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(note -> TaskNoteResponse.builder()
                        .id(note.getId())
                        .taskId(note.getTaskId())
                        .taskTitle(taskTitle)
                        .authorId(note.getAuthorId())
                        .noteContent(note.getNoteContent())
                        .createdAt(note.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskNoteResponse createNote(long taskId, TaskNoteRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new RuntimeException("Công việc này đã hoàn thành. Không thể gửi thêm thảo luận mới.");
        }

        long authorId = request.getAuthorId() != null ? request.getAuthorId() : 1L;

        TaskNote note = TaskNote.builder()
                .taskId(taskId)
                .authorId(authorId)
                .noteContent(request.getNoteContent())
                .build();

        TaskNote saved = taskNoteRepository.save(note);

        TaskNoteResponse response = TaskNoteResponse.builder()
                .id(saved.getId())
                .taskId(saved.getTaskId())
                .taskTitle(task.getTitle())
                .authorId(saved.getAuthorId())
                .noteContent(saved.getNoteContent())
                .createdAt(saved.getCreatedAt())
                .build();

        // Broadcast realtime qua STOMP WebSocket
        try {
            String destination = "/topic/space/" + task.getSpaceId() + "/tasks";
            messagingTemplate.convertAndSend(destination, (Object) Map.of(
                    "type", "NEW_NOTE",
                    "note", response,
                    "taskId", taskId
            ));
        } catch (Exception e) {
            log.warn("Failed to broadcast note via WebSocket: {}", e.getMessage());
        }

        // Gửi thông báo đến người phụ trách công việc nếu người khác bình luận
        try {
            appNotificationService.sendTaskCommented(task, authorId, null, request.getNoteContent());
        } catch (Exception e) {
            log.warn("Failed to dispatch task comment notification: {}", e.getMessage());
        }

        return response;
    }

    @Override
    public List<TaskNoteResponse> getNotesBySpaceId(long spaceId) {
        List<Task> tasks = taskRepository.findBySpaceIdAndIsDeletedFalse(spaceId);
        if (tasks.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, String> taskTitleMap = tasks.stream()
                .collect(Collectors.toMap(Task::getId, Task::getTitle, (a, b) -> a));

        List<Long> taskIds = new ArrayList<>(taskTitleMap.keySet());
        return taskNoteRepository.findByTaskIdInOrderByCreatedAtDesc(taskIds).stream()
                .map(note -> TaskNoteResponse.builder()
                        .id(note.getId())
                        .taskId(note.getTaskId())
                        .taskTitle(taskTitleMap.getOrDefault(note.getTaskId(), "Task #" + note.getTaskId()))
                        .authorId(note.getAuthorId())
                        .noteContent(note.getNoteContent())
                        .createdAt(note.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkspaceLogResponse> getLogsByTaskId(long taskId) {
        Task task = taskRepository.findById(taskId).orElse(null);
        String taskTitle = task != null ? task.getTitle() : "Task #" + taskId;

        return workspaceLogRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .filter(logItem -> !"COMMENT".equalsIgnoreCase(logItem.getActionType()))
                .filter(logItem -> logItem.getLogMessage() == null ||
                        (!logItem.getLogMessage().toLowerCase().contains("thảo luận") &&
                         !logItem.getLogMessage().toLowerCase().contains("ghi chú")))
                .map(logItem -> WorkspaceLogResponse.builder()
                        .id(logItem.getId())
                        .workspaceId(logItem.getWorkspaceId())
                        .taskId(logItem.getTaskId())
                        .taskTitle(taskTitle)
                        .userId(logItem.getUserId())
                        .actionType(logItem.getActionType())
                        .oldValue(logItem.getOldValue())
                        .newValue(logItem.getNewValue())
                        .logMessage(logItem.getLogMessage())
                        .createdAt(logItem.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkspaceLogResponse> getLogsBySpaceId(long spaceId) {
        List<Task> tasks = taskRepository.findBySpaceId(spaceId);
        if (tasks.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, String> taskTitleMap = tasks.stream()
                .collect(Collectors.toMap(Task::getId, Task::getTitle, (a, b) -> a));

        List<Long> taskIds = new ArrayList<>(taskTitleMap.keySet());
        return workspaceLogRepository.findByTaskIdInOrderByCreatedAtDesc(taskIds).stream()
                .filter(logItem -> !"COMMENT".equalsIgnoreCase(logItem.getActionType()))
                .filter(logItem -> logItem.getLogMessage() == null ||
                        (!logItem.getLogMessage().toLowerCase().contains("thảo luận") &&
                         !logItem.getLogMessage().toLowerCase().contains("ghi chú")))
                .map(logItem -> WorkspaceLogResponse.builder()
                        .id(logItem.getId())
                        .workspaceId(logItem.getWorkspaceId())
                        .taskId(logItem.getTaskId())
                        .taskTitle(taskTitleMap.getOrDefault(logItem.getTaskId(), "Task #" + logItem.getTaskId()))
                        .userId(logItem.getUserId())
                        .actionType(logItem.getActionType())
                        .oldValue(logItem.getOldValue())
                        .newValue(logItem.getNewValue())
                        .logMessage(logItem.getLogMessage())
                        .createdAt(logItem.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteNote(long noteId, long userId) {
        TaskNote note = taskNoteRepository.findById(noteId).orElse(null);
        if (note == null) return;
        if (note.getAuthorId() != userId && userId != 1L) {
            throw new RuntimeException("Bạn chỉ có quyền xóa thảo luận của chính mình!");
        }
        long taskId = note.getTaskId();
        taskNoteRepository.delete(note);

        Task task = taskRepository.findById(taskId).orElse(null);
        if (task != null) {
            try {
                String destination = "/topic/space/" + task.getSpaceId() + "/tasks";
                messagingTemplate.convertAndSend(destination, (Object) Map.of(
                        "type", "DELETE_NOTE",
                        "noteId", noteId,
                        "taskId", taskId
                ));
            } catch (Exception e) {
                log.warn("Failed to broadcast note deletion via WebSocket: {}", e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public WorkspaceLog logAction(long workspaceId, Long taskId, long userId, String actionType,
                                 String oldValue, String newValue, String message) {
        try {
            WorkspaceLog logEntry = WorkspaceLog.builder()
                    .workspaceId(workspaceId)
                    .taskId(taskId)
                    .userId(userId)
                    .actionType(actionType)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .logMessage(message)
                    .build();

            return workspaceLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to save workspace log: {}", e.getMessage());
            return null;
        }
    }
}
