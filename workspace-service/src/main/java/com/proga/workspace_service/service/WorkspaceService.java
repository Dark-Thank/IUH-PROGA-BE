package com.proga.workspace_service.service;

import com.proga.workspace_service.dto.WorkspaceRequest;
import com.proga.workspace_service.dto.WorkspaceResponse;

import java.util.List;
import java.util.UUID;

public interface WorkspaceService {
    WorkspaceResponse createWorkspace(WorkspaceRequest request);
    WorkspaceResponse getWorkspaceById(UUID id);
    List<WorkspaceResponse> getWorkspacesByOwner(UUID ownerId);
    WorkspaceResponse updateWorkspace(UUID id, WorkspaceRequest request);
    void deleteWorkspace(UUID id);
}
