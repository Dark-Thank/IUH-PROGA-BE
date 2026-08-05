package com.proga.ai_service.repository;

import com.proga.ai_service.model.AiChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {

    List<AiChatMessage> findByThreadIdOrderByCreatedAtAsc(long threadId);
}
