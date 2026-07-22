package com.proga.workspace_service.service;

import com.proga.workspace_service.dto.TaskRequest;
import com.proga.workspace_service.dto.TaskResponse;
import com.proga.workspace_service.model.Priority;
import com.proga.workspace_service.model.TaskStatus;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskResponse createTask(TaskRequest request);
    TaskResponse getTaskById(UUID id);
    List<TaskResponse> getTasksBySpace(UUID spaceId);
    List<TaskResponse> getTasksByOwner(UUID ownerId);
    TaskResponse updateTask(UUID id, TaskRequest request);
    TaskResponse updateTaskStatus(UUID id, TaskStatus status);
    void deleteTask(UUID id);
}
