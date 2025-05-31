package com.adia.user.repository;

import com.adia.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<UserEntity> getUserEntityById(long l);
    Optional<UserEntity> getUserEntityByEmail(String email);

    long countByIsSuspendedTrue();

    long countByIsEmailVerifiedFalse();

    long countByIsActivatedTrue();

    List<UserEntity> findDistinctByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);
}
