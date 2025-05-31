package com.adia.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode // Implement equals and hashCode for composite keys
public class GroupMemberId implements Serializable {
    private static final long serialVersionUID = 1L; // Recommended for Serializable classes

    @Column(name = "user_id") // Foreign key to users table (managed by user-service)
    private Long userId;

    @Column(name = "group_id") // Foreign key to group_conversations table
    private Long groupId;

    public GroupMemberId(Long userId, Long groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }

    // hashCode and equals are crucial for composite keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupMemberId that = (GroupMemberId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, groupId);
    }
}
