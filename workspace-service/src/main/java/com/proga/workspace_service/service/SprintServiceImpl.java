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
    private final com.proga.workspace_service.repository.TaskRepository taskRepository;

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
    @org.springframework.transaction.annotation.Transactional
    public void deleteSprint(long id, boolean deleteTasks) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + id));

        if (sprint.getStatus() == SprintStatus.CLOSED || sprint.getStatus() == SprintStatus.ACTIVE) {
            throw new IllegalStateException("Không được phép xóa Sprint đã đóng hoặc Sprint đang diễn ra.");
        }

        java.util.List<com.proga.workspace_service.model.Task> sprintTasks = taskRepository.findBySprintId(id);
        if (deleteTasks) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            for (com.proga.workspace_service.model.Task t : sprintTasks) {
                t.setDeleted(true);
                t.setDeletedAt(now);
            }
            taskRepository.saveAll(sprintTasks);
        } else {
            for (com.proga.workspace_service.model.Task t : sprintTasks) {
                t.setSprintId(null);
            }
            taskRepository.saveAll(sprintTasks);
        }
        sprintRepository.deleteById(id);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteSprint(long id) {
        deleteSprint(id, false);
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
