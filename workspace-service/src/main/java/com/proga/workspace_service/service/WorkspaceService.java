package com.proga.workspace_service.service;

import com.proga.workspace_service.dto.ClassifiedWorkspacesResponse;
import com.proga.workspace_service.dto.WorkspaceRequest;
import com.proga.workspace_service.dto.WorkspaceResponse;

import java.util.List;

public interface WorkspaceService {
    WorkspaceResponse createWorkspace(WorkspaceRequest request);
    WorkspaceResponse getWorkspaceById(long id);
    List<WorkspaceResponse> getWorkspacesByOwner(long ownerId);
    ClassifiedWorkspacesResponse getClassifiedWorkspaces(long userId);
    void inviteMember(long workspaceId, long userId, long roleId);
    void acceptInvitation(long workspaceId, long userId);
    void declineInvitation(long workspaceId, long userId);
    WorkspaceResponse updateWorkspace(long id, WorkspaceRequest request);
    void deleteWorkspace(long id);
}
