package com.proga.workspace_service.service;

import com.proga.workspace_service.dto.WorkspaceRequest;
import com.proga.workspace_service.dto.WorkspaceResponse;
import com.proga.workspace_service.model.Workspace;
import com.proga.workspace_service.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    @Override
    public WorkspaceResponse createWorkspace(WorkspaceRequest request) {
        if (workspaceRepository.existsByNameAndOwnerId(request.getName(), request.getOwnerId())) {
            throw new RuntimeException("Workspace with name '" + request.getName() + "' already exists for this owner");
        }

        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .ownerId(request.getOwnerId())
                .build();

        Workspace saved = workspaceRepository.save(workspace);
        return mapToResponse(saved);
    }

    @Override
    public WorkspaceResponse getWorkspaceById(UUID id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace not found with id: " + id));
        return mapToResponse(workspace);
    }

    @Override
    public List<WorkspaceResponse> getWorkspacesByOwner(UUID ownerId) {
        return workspaceRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WorkspaceResponse updateWorkspace(UUID id, WorkspaceRequest request) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace not found with id: " + id));

        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        if (request.getOwnerId() != null) {
            workspace.setOwnerId(request.getOwnerId());
        }

        Workspace updated = workspaceRepository.save(workspace);
        return mapToResponse(updated);
    }

    @Override
    public void deleteWorkspace(UUID id) {
        if (!workspaceRepository.existsById(id)) {
            throw new RuntimeException("Workspace not found with id: " + id);
        }
        workspaceRepository.deleteById(id);
    }

    private WorkspaceResponse mapToResponse(Workspace workspace) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .ownerId(workspace.getOwnerId())
                .createdAt(workspace.getCreatedAt())
                .build();
    }
}
