package com.proga.workspace_service.controller;

import com.proga.workspace_service.dto.ApiResponse;
import com.proga.workspace_service.dto.SpaceRequest;
import com.proga.workspace_service.dto.SpaceResponse;
import com.proga.workspace_service.service.SpaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spaces")
@RequiredArgsConstructor
public class SpaceController {

    private final SpaceService spaceService;

    @PostMapping
    public ResponseEntity<ApiResponse<SpaceResponse>> createSpace(@Valid @RequestBody SpaceRequest request) {
        SpaceResponse response = spaceService.createSpace(request);
        return ResponseEntity.ok(ApiResponse.success("Space created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SpaceResponse>> getSpaceById(@PathVariable long id) {
        return ResponseEntity.ok(ApiResponse.success(spaceService.getSpaceById(id)));
    }

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<ApiResponse<List<SpaceResponse>>> getSpacesByWorkspace(
            @PathVariable long workspaceId,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success(spaceService.getSpacesByWorkspace(workspaceId, userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SpaceResponse>> updateSpace(@PathVariable long id,
            @Valid @RequestBody SpaceRequest request) {
        SpaceResponse response = spaceService.updateSpace(id, request);
        return ResponseEntity.ok(ApiResponse.success("Space updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSpace(@PathVariable long id) {
        spaceService.deleteSpace(id);
        return ResponseEntity.ok(ApiResponse.success("Space deleted successfully", null));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<Void>> addMemberToSpace(
            @PathVariable long id,
            @RequestParam long userId,
            @RequestParam(defaultValue = "3") long roleId) {
        spaceService.addMemberToSpace(id, userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Member added to space successfully", null));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMemberFromSpace(
            @PathVariable long id,
            @PathVariable long userId) {
        spaceService.removeMemberFromSpace(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed from space successfully", null));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<com.proga.workspace_service.model.SpaceMember>>> getSpaceMembers(
            @PathVariable long id) {
        return ResponseEntity.ok(ApiResponse.success(spaceService.getSpaceMembers(id)));
    }
}
