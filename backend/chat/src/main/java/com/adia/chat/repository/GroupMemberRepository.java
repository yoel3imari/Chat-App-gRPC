package com.adia.chat.repository;

import com.adia.chat.entity.GroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.adia.chat.entity.GroupMemberEntity;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, GroupMemberId> {

} 