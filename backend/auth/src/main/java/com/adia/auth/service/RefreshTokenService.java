package com.adia.auth.service;

import com.adia.user.User;
import com.adia.auth.entity.RefreshToken;
import com.adia.auth.repository.RefreshTokenRepository;
import com.adia.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refreshExpiration}")
    private long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Create and persist a refresh token for the given user
     */
    public RefreshToken createRefreshToken(User user) {

        String refreshTokenStr = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshExpiration);

        RefreshToken token = RefreshToken.builder()
                .token(refreshTokenStr)
                .userId(user.getId())
                .expiryDate(expiryDate)
                .build();

        return refreshTokenRepository.save(token);
    }

    public String rotateRefreshToken(RefreshToken oldToken) {
        String newToken = UUID.randomUUID().toString();

        oldToken.setToken(newToken);
        oldToken.setExpiryDate(LocalDateTime.now().plusNanos(refreshExpiration)); // or `.plusDays(30)` depending on how you store expiration

        refreshTokenRepository.save(oldToken);

        return newToken;
    }

    /**
     * Verify token is valid and not expired
     */
    public Optional<RefreshToken> verify(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> rt.getExpiryDate().isAfter(LocalDateTime.now()));
    }

    public boolean isTokenExpired(RefreshToken token) {
        return token.getExpiryDate().isBefore(LocalDateTime.now());
    }

    /**
     * Delete all tokens for a user (e.g. on logout)
     */
    public void deleteByUserId(long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    public Optional<RefreshToken> findByToken(String requestToken) {
        return refreshTokenRepository.findByToken(requestToken);
    }
}
