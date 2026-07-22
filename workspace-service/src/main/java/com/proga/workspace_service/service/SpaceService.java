package com.proga.workspace_service.service;

import com.proga.workspace_service.dto.SpaceRequest;
import com.proga.workspace_service.dto.SpaceResponse;

import java.util.List;
import java.util.UUID;

public interface SpaceService {
    SpaceResponse createSpace(SpaceRequest request);
    SpaceResponse getSpaceById(UUID id);
    List<SpaceResponse> getSpacesByWorkspace(UUID workspaceId);
    SpaceResponse updateSpace(UUID id, SpaceRequest request);
    void deleteSpace(UUID id);
}
