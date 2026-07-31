package com.proga.workspace_service.controller;

import com.proga.workspace_service.dto.ApiResponse;
import com.proga.workspace_service.dto.WorkspaceRequest;
import com.proga.workspace_service.dto.WorkspaceResponse;
import com.proga.workspace_service.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(@Valid @RequestBody WorkspaceRequest request) {
        WorkspaceResponse response = workspaceService.createWorkspace(request);
        return ResponseEntity.ok(ApiResponse.success("Workspace created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspaceById(@PathVariable long id) {
        return ResponseEntity.ok(ApiResponse.success(workspaceService.getWorkspaceById(id)));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getWorkspacesByOwner(@PathVariable long ownerId) {
        return ResponseEntity.ok(ApiResponse.success(workspaceService.getWorkspacesByOwner(ownerId)));
    }

    @GetMapping("/user/{userId}/classified")
    public ResponseEntity<ApiResponse<com.proga.workspace_service.dto.ClassifiedWorkspacesResponse>> getClassifiedWorkspaces(@PathVariable long userId) {
        return ResponseEntity.ok(ApiResponse.success(workspaceService.getClassifiedWorkspaces(userId)));
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<ApiResponse<Void>> inviteMember(
            @PathVariable long id,
            @RequestParam long userId,
            @RequestParam(defaultValue = "3") long roleId
    ) {
        workspaceService.inviteMember(id, userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Member invited successfully", null));
    }

    @PutMapping("/{id}/invitations/accept")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(
            @PathVariable long id,
            @RequestParam long userId
    ) {
        workspaceService.acceptInvitation(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Invitation accepted successfully", null));
    }

    @PutMapping("/{id}/invitations/decline")
    public ResponseEntity<ApiResponse<Void>> declineInvitation(
            @PathVariable long id,
            @RequestParam long userId
    ) {
        workspaceService.declineInvitation(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Invitation declined successfully", null));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<com.proga.workspace_service.model.WorkspaceMember>>> getWorkspaceMembers(@PathVariable long id) {
        return ResponseEntity.ok(ApiResponse.success(workspaceService.getWorkspaceMembers(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(@PathVariable long id, @Valid @RequestBody WorkspaceRequest request) {
        WorkspaceResponse response = workspaceService.updateWorkspace(id, request);
        return ResponseEntity.ok(ApiResponse.success("Workspace updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(@PathVariable long id) {
        workspaceService.deleteWorkspace(id);
        return ResponseEntity.ok(ApiResponse.success("Workspace deleted successfully", null));
    }
}
