package com.proga.workspace_service.service;

import com.proga.workspace_service.dto.SpaceRequest;
import com.proga.workspace_service.dto.SpaceResponse;
import com.proga.workspace_service.model.SpaceMember;

import java.util.List;
import java.util.UUID;

public interface SpaceService {
    SpaceResponse createSpace(SpaceRequest request);
    SpaceResponse getSpaceById(long id);
    List<SpaceResponse> getSpacesByWorkspace(long workspaceId);
    List<SpaceResponse> getSpacesByWorkspace(long workspaceId, Long userId);
    SpaceResponse updateSpace(long id, SpaceRequest request);
    void deleteSpace(long id);
    void addMemberToSpace(long spaceId, long userId, long roleId);
    List<SpaceMember> getSpaceMembers(long spaceId);
}
