package com.proga.workspace_service.controller;

import com.proga.workspace_service.dto.ApiResponse;
import com.proga.workspace_service.dto.SprintRequest;
import com.proga.workspace_service.dto.SprintResponse;
import com.proga.workspace_service.model.SprintStatus;
import com.proga.workspace_service.service.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sprints")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    @PostMapping
    public ResponseEntity<ApiResponse<SprintResponse>> createSprint(@Valid @RequestBody SprintRequest request) {
        SprintResponse response = sprintService.createSprint(request);
        return ResponseEntity.ok(ApiResponse.success("Sprint created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SprintResponse>> getSprintById(@PathVariable long id) {
        return ResponseEntity.ok(ApiResponse.success(sprintService.getSprintById(id)));
    }

    @GetMapping("/space/{spaceId}")
    public ResponseEntity<ApiResponse<List<SprintResponse>>> getSprintsBySpace(
            @PathVariable long spaceId,
            @RequestParam(required = false) SprintStatus status) {
        if (status != null) {
            return ResponseEntity.ok(ApiResponse.success(sprintService.getSprintsBySpaceAndStatus(spaceId, status)));
        }
        return ResponseEntity.ok(ApiResponse.success(sprintService.getSprintsBySpace(spaceId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SprintResponse>> updateSprint(
            @PathVariable long id,
            @Valid @RequestBody SprintRequest request) {
        SprintResponse response = sprintService.updateSprint(id, request);
        return ResponseEntity.ok(ApiResponse.success("Sprint updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SprintResponse>> updateSprintStatus(
            @PathVariable long id,
            @RequestParam SprintStatus status) {
        SprintResponse response = sprintService.updateSprintStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Sprint status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSprint(@PathVariable long id) {
        sprintService.deleteSprint(id);
        return ResponseEntity.ok(ApiResponse.success("Sprint deleted successfully", null));
    }
}
