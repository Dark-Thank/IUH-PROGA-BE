package com.proga.workspace_service.repository;

import com.proga.workspace_service.model.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {
    List<Space> findByWorkspaceId(long workspaceId);

    boolean existsByNameAndWorkspaceId(String name, long workspaceId);
}
