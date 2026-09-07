package com.proga.ai_service.repository;

import com.proga.ai_service.model.AgentType;
import com.proga.ai_service.model.AiThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiThreadRepository extends JpaRepository<AiThread, Long> {

    Optional<AiThread> findBySpaceIdAndAgentType(long spaceId, AgentType agentType);

    Optional<AiThread> findBySpaceIdAndUserIdAndAgentType(long spaceId, long userId, AgentType agentType);

    List<AiThread> findBySpaceId(long spaceId);

    List<AiThread> findBySpaceIdAndUserId(long spaceId, long userId);
}
