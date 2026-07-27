package com.proga.workspace_service.repository;

import com.proga.workspace_service.model.TaskNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskNoteRepository extends JpaRepository<TaskNote, Long> {
    List<TaskNote> findByTaskIdOrderByCreatedAtAsc(long taskId);
}
