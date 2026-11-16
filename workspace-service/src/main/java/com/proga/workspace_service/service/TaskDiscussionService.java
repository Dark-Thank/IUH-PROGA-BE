package com.proga.workspace_service.service;

import com.proga.workspace_service.dto.TaskNoteRequest;
import com.proga.workspace_service.dto.TaskNoteResponse;
import com.proga.workspace_service.dto.WorkspaceLogResponse;
import com.proga.workspace_service.model.WorkspaceLog;

import java.util.List;

public interface TaskDiscussionService {
    List<TaskNoteResponse> getNotesByTaskId(long taskId);

    TaskNoteResponse createNote(long taskId, TaskNoteRequest request);

    List<TaskNoteResponse> getNotesBySpaceId(long spaceId);

    List<WorkspaceLogResponse> getLogsByTaskId(long taskId);

    List<WorkspaceLogResponse> getLogsBySpaceId(long spaceId);

    void deleteNote(long noteId, long userId);

    WorkspaceLog logAction(long workspaceId, Long taskId, long userId, String actionType, String oldValue, String newValue, String message);
}
