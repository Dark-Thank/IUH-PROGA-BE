package com.proga.workspace_service.service;

import com.proga.workspace_service.dto.WorkspaceRequest;
import com.proga.workspace_service.dto.WorkspaceResponse;

import java.util.List;
import java.util.UUID;

public interface WorkspaceService {
    WorkspaceResponse createWorkspace(WorkspaceRequest request);
    WorkspaceResponse getWorkspaceById(long id);
    List<WorkspaceResponse> getWorkspacesByOwner(long ownerId);
    WorkspaceResponse updateWorkspace(long id, WorkspaceRequest request);
    void deleteWorkspace(long id);
}
