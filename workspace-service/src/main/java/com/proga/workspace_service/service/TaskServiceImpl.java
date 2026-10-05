package com.proga.workspace_service.service;

import com.proga.workspace_service.dto.TaskRequest;
import com.proga.workspace_service.dto.TaskResponse;
import com.proga.workspace_service.model.Priority;
import com.proga.workspace_service.model.Task;
import com.proga.workspace_service.model.TaskStatus;
import com.proga.workspace_service.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final com.proga.workspace_service.repository.SprintRepository sprintRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public TaskResponse createTask(TaskRequest request) {
        Task task = Task.builder()
                .spaceId(request.getSpaceId())
                .sprintId(request.getSprintId())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .ownerId(request.getOwnerId())
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .build();

        Task saved = taskRepository.save(task);
        TaskResponse response = mapToResponse(saved);
        notifyWebsocket(saved.getSpaceId(), "CREATE", response);
        return response;
    }

    @Override
    public TaskResponse getTaskById(long id) {
        Task task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        return mapToResponse(task);
    }

    @Override
    public List<TaskResponse> getTasksBySpace(long spaceId) {
        return taskRepository.findBySpaceIdAndIsDeletedFalse(spaceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getTasksBySprint(long sprintId) {
        return taskRepository.findBySprintIdAndIsDeletedFalse(sprintId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getTasksByOwner(long ownerId) {
        return taskRepository.findByOwnerIdAndIsDeletedFalse(ownerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TaskResponse updateTask(long id, TaskRequest request) {
        Task task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        task.setTitle(request.getTitle());
        task.setSprintId(request.getSprintId());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getOwnerId() == null || request.getOwnerId() <= 0) {
            task.setOwnerId(null);
        } else {
            task.setOwnerId(request.getOwnerId());
        }
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());

        Task updated = taskRepository.save(task);
        TaskResponse response = mapToResponse(updated);
        notifyWebsocket(updated.getSpaceId(), "UPDATE", response);
        return response;
    }

    @Override
    public TaskResponse updateTaskStatus(long id, TaskStatus status) {
        Task task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        task.setStatus(status);
        Task updated = taskRepository.save(task);
        TaskResponse response = mapToResponse(updated);
        notifyWebsocket(updated.getSpaceId(), "UPDATE_STATUS", response);
        return response;
    }

    @Override
    public void deleteTask(long id) {
        Task task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        validateTaskDeletion(task);

        task.setDeleted(true);
        task.setDeletedAt(java.time.LocalDateTime.now());
        Task saved = taskRepository.save(task);

        long spaceId = saved.getSpaceId();
        TaskResponse response = mapToResponse(saved);
        notifyWebsocket(spaceId, "DELETE", response);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteTasksBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        List<Task> tasks = taskRepository.findAllById(ids).stream()
                .filter(t -> !t.isDeleted())
                .collect(Collectors.toList());

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (Task t : tasks) {
            validateTaskDeletion(t);
            t.setDeleted(true);
            t.setDeletedAt(now);
        }
        taskRepository.saveAll(tasks);

        for (Task t : tasks) {
            notifyWebsocket(t.getSpaceId(), "DELETE", mapToResponse(t));
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void assignTasksBatch(List<com.proga.workspace_service.dto.BatchAssignTaskRequest.AssignItem> assignments) {
        if (assignments == null || assignments.isEmpty()) return;
        Map<Long, Long> map = assignments.stream()
                .filter(a -> a.getTaskId() != null && a.getOwnerId() != null)
                .collect(Collectors.toMap(
                        com.proga.workspace_service.dto.BatchAssignTaskRequest.AssignItem::getTaskId,
                        com.proga.workspace_service.dto.BatchAssignTaskRequest.AssignItem::getOwnerId,
                        (k1, k2) -> k2
                ));

        List<Task> tasks = taskRepository.findAllById(map.keySet());
        for (Task t : tasks) {
            Long newOwnerId = map.get(t.getId());
            t.setOwnerId(newOwnerId);
        }
        List<Task> savedTasks = taskRepository.saveAll(tasks);
        for (Task t : savedTasks) {
            notifyWebsocket(t.getSpaceId(), "UPDATE", mapToResponse(t));
        }
    }

    @Override
    public List<TaskResponse> getDeletedTasksBySpace(long spaceId) {
        return taskRepository.findBySpaceIdAndIsDeletedTrueOrderByDeletedAtDesc(spaceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TaskResponse restoreTask(long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        if (!task.isDeleted()) {
            return mapToResponse(task);
        }
        task.setDeleted(false);
        task.setDeletedAt(null);
        Task saved = taskRepository.save(task);
        TaskResponse response = mapToResponse(saved);
        notifyWebsocket(saved.getSpaceId(), "RESTORE", response);
        return response;
    }

    @Override
    public void permanentDeleteTask(long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        taskRepository.deleteById(id);
        notifyWebsocket(task.getSpaceId(), "PERMANENT_DELETE", mapToResponse(task));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void purgeExpiredDeletedTasks() {
        java.time.LocalDateTime threshold = java.time.LocalDateTime.now().minusDays(15);
        List<Task> expiredTasks = taskRepository.findByIsDeletedTrueAndDeletedAtBefore(threshold);
        if (!expiredTasks.isEmpty()) {
            taskRepository.deleteAll(expiredTasks);
            for (Task t : expiredTasks) {
                notifyWebsocket(t.getSpaceId(), "PERMANENT_DELETE", mapToResponse(t));
            }
        }
    }

    private void validateTaskDeletion(Task task) {
        if (task.getSprintId() != null) {
            com.proga.workspace_service.model.Sprint sprint = sprintRepository.findById(task.getSprintId()).orElse(null);
            if (sprint != null) {
                if (sprint.getStatus() == com.proga.workspace_service.model.SprintStatus.CLOSED) {
                    throw new IllegalStateException("Không được phép xóa công việc trong Sprint đã đóng.");
                }
                if (sprint.getStatus() == com.proga.workspace_service.model.SprintStatus.ACTIVE && task.getStatus() != com.proga.workspace_service.model.TaskStatus.TODO) {
                    throw new IllegalStateException("Trong Sprint đang diễn ra, chỉ được phép xóa các công việc ở trạng thái Cần làm (TODO).");
                }
            }
        }
    }

    private void notifyWebsocket(long spaceId, String action, TaskResponse response) {
        try {
            Map<String, Object> payload = Map.of(
                    "action", action,
                    "spaceId", spaceId,
                    "taskId", response.getId(),
                    "task", response
            );
            String destination = "/topic/space/" + spaceId + "/tasks";
            messagingTemplate.convertAndSend(destination, (Object) payload);
        } catch (Exception e) {
            System.err.println("STOMP Websocket notification error: " + e.getMessage());
        }
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .spaceId(task.getSpaceId())
                .sprintId(task.getSprintId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .ownerId(task.getOwnerId())
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .isDeleted(task.isDeleted())
                .deletedAt(task.getDeletedAt())
                .build();
    }
}
