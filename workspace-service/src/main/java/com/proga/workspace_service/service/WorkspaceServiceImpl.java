package com.proga.workspace_service.service;

import com.proga.workspace_service.dto.ClassifiedWorkspacesResponse;
import com.proga.workspace_service.dto.WorkspaceRequest;
import com.proga.workspace_service.dto.WorkspaceResponse;
import com.proga.workspace_service.model.Workspace;
import com.proga.workspace_service.model.WorkspaceMember;
import com.proga.workspace_service.model.WorkspaceMemberId;
import com.proga.workspace_service.repository.WorkspaceMemberRepository;
import com.proga.workspace_service.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Override
    @Transactional
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

        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .id(new WorkspaceMemberId(saved.getId(), request.getOwnerId()))
                .workspace(saved)
                .roleId(2L)
                .status("ACCEPTED")
                .build();
        workspaceMemberRepository.save(ownerMember);

        return mapToResponse(saved);
    }

    @Override
    public WorkspaceResponse getWorkspaceById(long id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace not found with id: " + id));
        return mapToResponse(workspace);
    }

    @Override
    public List<WorkspaceResponse> getWorkspacesByOwner(long ownerId) {
        return workspaceRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ClassifiedWorkspacesResponse getClassifiedWorkspaces(long userId) {
        List<WorkspaceResponse> owned = workspaceRepository.findByOwnerId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        List<WorkspaceMember> userMemberships = workspaceMemberRepository.findByIdUserId(userId);

        List<WorkspaceResponse> joined = new ArrayList<>();
        List<WorkspaceResponse> pending = new ArrayList<>();

        for (WorkspaceMember member : userMemberships) {
            Workspace ws = member.getWorkspace();
            if (ws == null) {
                ws = workspaceRepository.findById(member.getId().getWorkspaceId()).orElse(null);
            }
            if (ws != null && ws.getOwnerId() != userId) {
                if ("ACCEPTED".equalsIgnoreCase(member.getStatus())) {
                    joined.add(mapToResponse(ws));
                } else if ("PENDING".equalsIgnoreCase(member.getStatus())) {
                    pending.add(mapToResponse(ws));
                }
            }
        }

        return ClassifiedWorkspacesResponse.builder()
                .ownedWorkspaces(owned)
                .joinedWorkspaces(joined)
                .pendingWorkspaces(pending)
                .build();
    }

    @Override
    @Transactional
    public void inviteMember(long workspaceId, long userId, long roleId) {
        Workspace ws = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found with id: " + workspaceId));

        WorkspaceMemberId memberId = new WorkspaceMemberId(workspaceId, userId);
        WorkspaceMember member = WorkspaceMember.builder()
                .id(memberId)
                .workspace(ws)
                .roleId(roleId)
                .status("PENDING")
                .build();

        workspaceMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void acceptInvitation(long workspaceId, long userId) {
        WorkspaceMemberId memberId = new WorkspaceMemberId(workspaceId, userId);
        WorkspaceMember member = workspaceMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Invitation not found for user: " + userId + " in workspace: " + workspaceId));

        member.setStatus("ACCEPTED");
        workspaceMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void declineInvitation(long workspaceId, long userId) {
        WorkspaceMemberId memberId = new WorkspaceMemberId(workspaceId, userId);
        if (workspaceMemberRepository.existsById(memberId)) {
            workspaceMemberRepository.deleteById(memberId);
        }
    }

    @Override
    public WorkspaceResponse updateWorkspace(long id, WorkspaceRequest request) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace not found with id: " + id));

        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        workspace.setOwnerId(request.getOwnerId());

        Workspace updated = workspaceRepository.save(workspace);
        return mapToResponse(updated);
    }

    @Override
    public void deleteWorkspace(long id) {
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
