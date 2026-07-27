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
import java.util.UUID;

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
    public ResponseEntity<ApiResponse<List<SpaceResponse>>> getSpacesByWorkspace(@PathVariable long workspaceId) {
        return ResponseEntity.ok(ApiResponse.success(spaceService.getSpacesByWorkspace(workspaceId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SpaceResponse>> updateSpace(@PathVariable long id, @Valid @RequestBody SpaceRequest request) {
        SpaceResponse response = spaceService.updateSpace(id, request);
        return ResponseEntity.ok(ApiResponse.success("Space updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSpace(@PathVariable long id) {
        spaceService.deleteSpace(id);
        return ResponseEntity.ok(ApiResponse.success("Space deleted successfully", null));
    }
}
