package com.proga.workspace_service.service;

import com.proga.workspace_service.dto.SpaceRequest;
import com.proga.workspace_service.dto.SpaceResponse;
import com.proga.workspace_service.model.Space;
import com.proga.workspace_service.model.Sprint;
import com.proga.workspace_service.model.SprintStatus;
import com.proga.workspace_service.repository.SpaceMemberRepository;
import com.proga.workspace_service.repository.SpaceRepository;
import com.proga.workspace_service.repository.SprintRepository;
import com.proga.workspace_service.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpaceServiceImpl implements SpaceService {

    private final SpaceRepository spaceRepository;
    private final SprintRepository sprintRepository;
    private final SpaceMemberRepository spaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;

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
                .isPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false)
                .build();

        Space saved = spaceRepository.save(space);

        // Mặc định tự tạo 1 Sprint 0 (Kickoff & Setup) cho Space mới để không bao giờ bị trùng với Sprint 1 của AI
        Sprint defaultSprint = Sprint.builder()
                .spaceId(saved.getId())
                .name("Sprint 0: Kickoff & Setup")
                .goal("Sprint khởi tạo môi trường & lên kế hoạch cho Space " + saved.getName())
                .status(SprintStatus.ACTIVE)
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .build();
        sprintRepository.save(defaultSprint);

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
        return getSpacesByWorkspace(workspaceId, null);
    }

    @Override
    public List<SpaceResponse> getSpacesByWorkspace(long workspaceId, Long userId) {
        List<Space> allSpaces = spaceRepository.findByWorkspaceId(workspaceId);

        if (userId == null) {
            // Default filter out private spaces when no userId is passed
            return allSpaces.stream()
                    .filter(s -> s.getIsPrivate() == null || !s.getIsPrivate())
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        // Check if user is Workspace Owner
        boolean isOwner = workspaceRepository.findById(workspaceId)
                .map(w -> w.getOwnerId() == userId)
                .orElse(false);

        if (isOwner) {
            // Owner sees all spaces
            return allSpaces.stream().map(this::mapToResponse).collect(Collectors.toList());
        }

        // Get user's space memberships
        List<Long> memberSpaceIds = spaceMemberRepository.findByIdUserId(userId).stream()
                .map(sm -> sm.getId().getSpaceId())
                .collect(Collectors.toList());

        return allSpaces.stream()
                .filter(s -> s.getIsPrivate() == null || !s.getIsPrivate() || memberSpaceIds.contains(s.getId()))
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
        if (request.getIsPrivate() != null) space.setIsPrivate(request.getIsPrivate());

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

    @Override
    public void addMemberToSpace(long spaceId, long userId, long roleId) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new RuntimeException("Space not found with id: " + spaceId));

        com.proga.workspace_service.model.SpaceMemberId memberId = new com.proga.workspace_service.model.SpaceMemberId(spaceId, userId);
        com.proga.workspace_service.model.SpaceMember member = com.proga.workspace_service.model.SpaceMember.builder()
                .id(memberId)
                .space(space)
                .roleId(roleId)
                .build();
        spaceMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void removeMemberFromSpace(long spaceId, long userId) {
        com.proga.workspace_service.model.SpaceMemberId memberId = new com.proga.workspace_service.model.SpaceMemberId(spaceId, userId);
        if (spaceMemberRepository.existsById(memberId)) {
            spaceMemberRepository.deleteById(memberId);
        }
    }

    @Override
    public List<com.proga.workspace_service.model.SpaceMember> getSpaceMembers(long spaceId) {
        return spaceMemberRepository.findByIdSpaceId(spaceId);
    }

    private SpaceResponse mapToResponse(Space space) {
        return SpaceResponse.builder()
                .id(space.getId())
                .workspaceId(space.getWorkspaceId())
                .name(space.getName())
                .startDate(space.getStartDate())
                .endDate(space.getEndDate())
                .isPrivate(space.getIsPrivate())
                .createdAt(space.getCreatedAt())
                .build();
    }
}
