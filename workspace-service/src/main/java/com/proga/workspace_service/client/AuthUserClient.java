package com.proga.workspace_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "auth-service", path = "/api/v1/users")
public interface AuthUserClient {

    @GetMapping("/{id}")
    Map<String, Object> getUserById(@PathVariable("id") long id);
}
