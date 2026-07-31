package com.proga.workspace_service.service;

import com.proga.workspace_service.dto.SprintRequest;
import com.proga.workspace_service.dto.SprintResponse;
import com.proga.workspace_service.model.Sprint;
import com.proga.workspace_service.model.SprintStatus;
import com.proga.workspace_service.repository.SprintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;

    @Override
    public SprintResponse createSprint(SprintRequest request) {
        Sprint sprint = Sprint.builder()
                .spaceId(request.getSpaceId())
                .name(request.getName())
                .goal(request.getGoal())
                .status(request.getStatus() != null ? request.getStatus() : SprintStatus.FUTURE)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        Sprint saved = sprintRepository.save(sprint);
        return mapToResponse(saved);
    }

    @Override
    public SprintResponse getSprintById(long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + id));
        return mapToResponse(sprint);
    }

    @Override
    public List<SprintResponse> getSprintsBySpace(long spaceId) {
        return sprintRepository.findBySpaceId(spaceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SprintResponse> getSprintsBySpaceAndStatus(long spaceId, SprintStatus status) {
        return sprintRepository.findBySpaceIdAndStatus(spaceId, status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SprintResponse updateSprint(long id, SprintRequest request) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + id));

        sprint.setName(request.getName());
        sprint.setGoal(request.getGoal());
        if (request.getStatus() != null) {
            sprint.setStatus(request.getStatus());
        }
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());

        Sprint updated = sprintRepository.save(sprint);
        return mapToResponse(updated);
    }

    @Override
    public SprintResponse updateSprintStatus(long id, SprintStatus status) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + id));

        sprint.setStatus(status);
        Sprint updated = sprintRepository.save(sprint);
        return mapToResponse(updated);
    }

    @Override
    public void deleteSprint(long id) {
        if (!sprintRepository.existsById(id)) {
            throw new RuntimeException("Sprint not found with id: " + id);
        }
        sprintRepository.deleteById(id);
    }

    private SprintResponse mapToResponse(Sprint sprint) {
        return SprintResponse.builder()
                .id(sprint.getId())
                .spaceId(sprint.getSpaceId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .status(sprint.getStatus())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .createdAt(sprint.getCreatedAt())
                .build();
    }
}
