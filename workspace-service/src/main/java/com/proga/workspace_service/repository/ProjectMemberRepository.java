package com.proga.workspace_service.repository;

import com.proga.workspace_service.model.ProjectMember;
import com.proga.workspace_service.model.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    List<ProjectMember> findByIdWorkspaceId(UUID workspaceId);
    List<ProjectMember> findByIdUserId(UUID userId);
    boolean existsByIdWorkspaceIdAndIdUserId(UUID workspaceId, UUID userId);
    void deleteByIdWorkspaceIdAndIdUserId(UUID workspaceId, UUID userId);
}
