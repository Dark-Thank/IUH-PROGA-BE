package com.proga.workspace_service.repository;

import com.proga.workspace_service.model.TaskNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskNoteRepository extends JpaRepository<TaskNote, Long> {
    List<TaskNote> findByTaskIdOrderByCreatedAtAsc(long taskId);

    List<TaskNote> findByTaskIdOrderByCreatedAtDesc(long taskId);

    List<TaskNote> findByTaskIdInOrderByCreatedAtDesc(List<Long> taskIds);
}
