package com.proga.ai_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_threads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "openai_thread_id", length = 100)
    private String openaiThreadId;

    @Column(name = "space_id", nullable = false)
    private long spaceId;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_type", length = 30, nullable = false)
    private AgentType agentType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
