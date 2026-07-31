package com.proga.ai_service.client;

import com.proga.ai_service.dto.ApiResponse;
import com.proga.ai_service.dto.SpaceDto;
import com.proga.ai_service.dto.TaskDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "workspace-service")
public interface WorkspaceClient {

    @GetMapping("/api/v1/spaces/{id}")
    ApiResponse<SpaceDto> getSpaceById(@PathVariable("id") long id);

    @GetMapping("/api/v1/tasks/space/{spaceId}")
    ApiResponse<List<TaskDto>> getTasksBySpace(@PathVariable("spaceId") long spaceId);

    @GetMapping("/api/v1/tasks/{id}")
    ApiResponse<TaskDto> getTaskById(@PathVariable("id") long id);
}
