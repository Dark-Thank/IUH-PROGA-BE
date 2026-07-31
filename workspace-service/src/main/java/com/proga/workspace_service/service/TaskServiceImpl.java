package com.proga.workspace_service.service;

import com.proga.workspace_service.dto.TaskRequest;
import com.proga.workspace_service.dto.TaskResponse;
import com.proga.workspace_service.model.Priority;
import com.proga.workspace_service.model.Task;
import com.proga.workspace_service.model.TaskStatus;
import com.proga.workspace_service.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

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
        return mapToResponse(saved);
    }

    @Override
    public TaskResponse getTaskById(long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        return mapToResponse(task);
    }

    @Override
    public List<TaskResponse> getTasksBySpace(long spaceId) {
        return taskRepository.findBySpaceId(spaceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getTasksBySprint(long sprintId) {
        return taskRepository.findBySprintId(sprintId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getTasksByOwner(long ownerId) {
        return taskRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TaskResponse updateTask(long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        task.setTitle(request.getTitle());
        task.setSprintId(request.getSprintId());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getOwnerId() != null) task.setOwnerId(request.getOwnerId());
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());

        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    @Override
    public TaskResponse updateTaskStatus(long id, TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        task.setStatus(status);
        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    @Override
    public void deleteTask(long id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
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
                .build();
    }
}
