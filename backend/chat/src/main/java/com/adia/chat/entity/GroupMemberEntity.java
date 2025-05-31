package com.adia.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_members")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"group"}) // 'user' (UserEntity) is gone, userId is in 'id'
@EqualsAndHashCode(of = "id") // Use composite ID for equals/hashCode
public class GroupMemberEntity {

    @EmbeddedId
    private GroupMemberId id; // Contains Long userId and Long groupId

    // The UserEntity user field and its @ManyToOne, @MapsId("userId") are removed.
    // The userId is accessed via id.getUserId()

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId") // This tells JPA that the 'groupId' attribute of the EmbeddedId 'id'
    // is used as the foreign key for this 'group' relationship.
    @JoinColumn(name = "group_id", referencedColumnName = "id", insertable = false, updatable = false)
    // name="group_id" is the FK column in group_members table.
    // referencedColumnName="id" is the PK column in group_conversations table (GroupConversationEntity's ID).
    // insertable=false, updatable=false because GroupMemberId.groupId is the owner of this column mapping.
    private GroupConversationEntity group;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}