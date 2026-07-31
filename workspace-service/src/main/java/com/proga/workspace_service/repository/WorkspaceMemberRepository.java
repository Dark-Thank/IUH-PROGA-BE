package com.proga.workspace_service.repository;

import com.proga.workspace_service.model.WorkspaceMember;
import com.proga.workspace_service.model.WorkspaceMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, WorkspaceMemberId> {
    List<WorkspaceMember> findByIdWorkspaceId(long workspaceId);
    List<WorkspaceMember> findByIdUserId(long userId);
    boolean existsByIdWorkspaceIdAndIdUserId(long workspaceId, long userId);
    void deleteByIdWorkspaceIdAndIdUserId(long workspaceId, long userId);
}
