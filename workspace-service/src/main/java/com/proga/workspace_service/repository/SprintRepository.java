package com.proga.workspace_service.repository;

import com.proga.workspace_service.model.Sprint;
import com.proga.workspace_service.model.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findBySpaceId(long spaceId);
    List<Sprint> findBySpaceIdAndStatus(long spaceId, SprintStatus status);
}
