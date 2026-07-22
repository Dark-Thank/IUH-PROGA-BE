package com.proga.workspace_service.repository;

import com.proga.workspace_service.model.WorkspaceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkspaceLogRepository extends JpaRepository<WorkspaceLog, Long> {
    List<WorkspaceLog> findByWorkspaceIdOrderByCreatedAtDesc(UUID workspaceId);
    List<WorkspaceLog> findByTaskIdOrderByCreatedAtDesc(UUID taskId);
}
