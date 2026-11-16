package com.proga.workspace_service.controller;

import com.proga.workspace_service.dto.ApiResponse;
import com.proga.workspace_service.dto.TaskNoteRequest;
import com.proga.workspace_service.dto.TaskNoteResponse;
import com.proga.workspace_service.dto.WorkspaceLogResponse;
import com.proga.workspace_service.service.TaskDiscussionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TaskDiscussionController {

    private final TaskDiscussionService taskDiscussionService;

    // --- TASK NOTES (THẢO LUẬN / GHI CHÚ) ---

    @GetMapping("/tasks/{taskId}/notes")
    public ResponseEntity<ApiResponse<List<TaskNoteResponse>>> getNotesByTaskId(@PathVariable long taskId) {
        List<TaskNoteResponse> notes = taskDiscussionService.getNotesByTaskId(taskId);
        return ResponseEntity.ok(ApiResponse.success("Task notes fetched successfully", notes));
    }

    @PostMapping("/tasks/{taskId}/notes")
    public ResponseEntity<ApiResponse<TaskNoteResponse>> createNote(
            @PathVariable long taskId,
            @Valid @RequestBody TaskNoteRequest request) {
        TaskNoteResponse note = taskDiscussionService.createNote(taskId, request);
        return ResponseEntity.ok(ApiResponse.success("Task note created successfully", note));
    }

    @DeleteMapping("/tasks/notes/{noteId}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable long noteId,
            @RequestParam(required = false, defaultValue = "1") long userId) {
        taskDiscussionService.deleteNote(noteId, userId);
        return ResponseEntity.ok(ApiResponse.success("Xóa thảo luận thành công", null));
    }

    @GetMapping("/spaces/{spaceId}/notes")
    public ResponseEntity<ApiResponse<List<TaskNoteResponse>>> getNotesBySpaceId(@PathVariable long spaceId) {
        List<TaskNoteResponse> notes = taskDiscussionService.getNotesBySpaceId(spaceId);
        return ResponseEntity.ok(ApiResponse.success("Space notes fetched successfully", notes));
    }

    // --- ACTIVITY LOGS (NHẬT KÝ HOẠT ĐỘNG CÔNG VIỆC) ---

    @GetMapping("/tasks/{taskId}/logs")
    public ResponseEntity<ApiResponse<List<WorkspaceLogResponse>>> getLogsByTaskId(@PathVariable long taskId) {
        List<WorkspaceLogResponse> logs = taskDiscussionService.getLogsByTaskId(taskId);
        return ResponseEntity.ok(ApiResponse.success("Task activity logs fetched successfully", logs));
    }

    @GetMapping("/spaces/{spaceId}/logs")
    public ResponseEntity<ApiResponse<List<WorkspaceLogResponse>>> getLogsBySpaceId(@PathVariable long spaceId) {
        List<WorkspaceLogResponse> logs = taskDiscussionService.getLogsBySpaceId(spaceId);
        return ResponseEntity.ok(ApiResponse.success("Space activity logs fetched successfully", logs));
    }
}
