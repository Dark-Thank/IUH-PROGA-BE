package com.proga.workspace_service.service;

import com.proga.workspace_service.dto.TaskRequest;
import com.proga.workspace_service.dto.TaskResponse;
import com.proga.workspace_service.model.Priority;
import com.proga.workspace_service.model.TaskStatus;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskResponse createTask(TaskRequest request);
    TaskResponse getTaskById(long id);
    List<TaskResponse> getTasksBySpace(long spaceId);
    List<TaskResponse> getTasksByOwner(long ownerId);
    TaskResponse updateTask(long id, TaskRequest request);
    TaskResponse updateTaskStatus(long id, TaskStatus status);
    void deleteTask(long id);
}
