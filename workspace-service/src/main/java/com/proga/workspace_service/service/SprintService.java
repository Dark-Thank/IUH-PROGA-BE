package com.proga.workspace_service.service;

import com.proga.workspace_service.dto.SprintRequest;
import com.proga.workspace_service.dto.SprintResponse;
import com.proga.workspace_service.model.SprintStatus;

import java.util.List;

public interface SprintService {
    SprintResponse createSprint(SprintRequest request);
    SprintResponse getSprintById(long id);
    List<SprintResponse> getSprintsBySpace(long spaceId);
    List<SprintResponse> getSprintsBySpaceAndStatus(long spaceId, SprintStatus status);
    SprintResponse updateSprint(long id, SprintRequest request);
    SprintResponse updateSprintStatus(long id, SprintStatus status);
    void deleteSprint(long id);
}
