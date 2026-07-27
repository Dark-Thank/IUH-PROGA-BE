package com.proga.workspace_service.repository;

import com.proga.workspace_service.model.Priority;
import com.proga.workspace_service.model.Task;
import com.proga.workspace_service.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findBySpaceId(long spaceId);
    List<Task> findByOwnerId(long ownerId);
    List<Task> findBySpaceIdAndStatus(long spaceId, TaskStatus status);
    List<Task> findBySpaceIdAndPriority(long spaceId, Priority priority);
    List<Task> findBySpaceIdAndOwnerId(long spaceId, long ownerId);
}
