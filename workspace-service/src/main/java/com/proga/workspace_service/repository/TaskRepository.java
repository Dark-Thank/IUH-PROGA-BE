package com.proga.workspace_service.repository;

import com.proga.workspace_service.model.Priority;
import com.proga.workspace_service.model.Task;
import com.proga.workspace_service.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findBySpaceIdAndIsDeletedFalse(long spaceId);

    List<Task> findBySprintIdAndIsDeletedFalse(Long sprintId);

    List<Task> findByOwnerIdAndIsDeletedFalse(long ownerId);

    List<Task> findBySpaceIdAndStatusAndIsDeletedFalse(long spaceId, TaskStatus status);

    List<Task> findBySpaceIdAndPriorityAndIsDeletedFalse(long spaceId, Priority priority);

    List<Task> findBySpaceIdAndOwnerIdAndIsDeletedFalse(long spaceId, long ownerId);

    Optional<Task> findByIdAndIsDeletedFalse(long id);

    List<Task> findBySpaceIdAndIsDeletedTrueOrderByDeletedAtDesc(long spaceId);

    List<Task> findBySpaceId(long spaceId);

    List<Task> findBySprintId(Long sprintId);

    List<Task> findByIsDeletedTrueAndDeletedAtBefore(java.time.LocalDateTime threshold);
}
