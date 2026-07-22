package com.proga.workspace_service.repository;

import com.proga.workspace_service.model.Priority;
import com.proga.workspace_service.model.Task;
import com.proga.workspace_service.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findBySpaceId(UUID spaceId);
    List<Task> findByOwnerId(UUID ownerId);
    List<Task> findBySpaceIdAndStatus(UUID spaceId, TaskStatus status);
    List<Task> findBySpaceIdAndPriority(UUID spaceId, Priority priority);
    List<Task> findBySpaceIdAndOwnerId(UUID spaceId, UUID ownerId);
}
