package com.proga.workspace_service.repository;

import com.proga.workspace_service.model.SpaceMember;
import com.proga.workspace_service.model.SpaceMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpaceMemberRepository extends JpaRepository<SpaceMember, SpaceMemberId> {
    List<SpaceMember> findByIdSpaceId(long spaceId);
    List<SpaceMember> findByIdUserId(long userId);
}
