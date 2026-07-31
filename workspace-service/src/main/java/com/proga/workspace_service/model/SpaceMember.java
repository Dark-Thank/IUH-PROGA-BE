package com.proga.workspace_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "space_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpaceMember {

    @EmbeddedId
    private SpaceMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("spaceId")
    @JoinColumn(name = "space_id")
    @JsonIgnore
    private Space space;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;
}
