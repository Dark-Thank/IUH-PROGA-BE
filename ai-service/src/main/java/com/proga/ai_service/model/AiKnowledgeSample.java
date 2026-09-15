package com.proga.ai_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_knowledge_samples")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiKnowledgeSample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "space_id")
    private Long spaceId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "domain")
    private String domain;

    @Column(name = "requirement_text", columnDefinition = "TEXT", nullable = false)
    private String requirementText;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "tasks_json", columnDefinition = "TEXT")
    private String tasksJson;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
