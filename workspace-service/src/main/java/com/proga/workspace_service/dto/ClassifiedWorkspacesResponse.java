package com.proga.workspace_service.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassifiedWorkspacesResponse {
    private List<WorkspaceResponse> ownedWorkspaces;
    private List<WorkspaceResponse> joinedWorkspaces;
    private List<WorkspaceResponse> pendingWorkspaces;
}
