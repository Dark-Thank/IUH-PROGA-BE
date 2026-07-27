package com.proga.workspace_service.service;

import com.proga.workspace_service.dto.SpaceRequest;
import com.proga.workspace_service.dto.SpaceResponse;
import com.proga.workspace_service.model.Space;
import com.proga.workspace_service.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpaceServiceImpl implements SpaceService {

    private final SpaceRepository spaceRepository;

    @Override
    public SpaceResponse createSpace(SpaceRequest request) {
        if (spaceRepository.existsByNameAndWorkspaceId(request.getName(), request.getWorkspaceId())) {
            throw new RuntimeException("Space with name '" + request.getName() + "' already exists in this workspace");
        }

        Space space = Space.builder()
                .workspaceId(request.getWorkspaceId())
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        Space saved = spaceRepository.save(space);
        return mapToResponse(saved);
    }

    @Override
    public SpaceResponse getSpaceById(long id) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Space not found with id: " + id));
        return mapToResponse(space);
    }

    @Override
    public List<SpaceResponse> getSpacesByWorkspace(long workspaceId) {
        return spaceRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SpaceResponse updateSpace(long id, SpaceRequest request) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Space not found with id: " + id));

        space.setName(request.getName());
        space.setStartDate(request.getStartDate());
        space.setEndDate(request.getEndDate());

        Space updated = spaceRepository.save(space);
        return mapToResponse(updated);
    }

    @Override
    public void deleteSpace(long id) {
        if (!spaceRepository.existsById(id)) {
            throw new RuntimeException("Space not found with id: " + id);
        }
        spaceRepository.deleteById(id);
    }

    private SpaceResponse mapToResponse(Space space) {
        return SpaceResponse.builder()
                .id(space.getId())
                .workspaceId(space.getWorkspaceId())
                .name(space.getName())
                .startDate(space.getStartDate())
                .endDate(space.getEndDate())
                .createdAt(space.getCreatedAt())
                .build();
    }
}
