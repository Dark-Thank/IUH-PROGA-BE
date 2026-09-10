package com.proga.workspace_service.repository;

import com.proga.workspace_service.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    List<Workspace> findByOwnerId(long ownerId);

    boolean existsByNameAndOwnerId(String name, long ownerId);
}
