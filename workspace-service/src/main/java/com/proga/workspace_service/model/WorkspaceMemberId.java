package com.proga.workspace_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class WorkspaceMemberId implements Serializable {

    @Column(name = "workspace_id", nullable = false)
    private long workspaceId;

    @Column(name = "user_id", nullable = false)
    private long userId;
}
