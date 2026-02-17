package com.org.wmm.auth.repository;

import com.org.wmm.auth.domain.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.user.id = :userId")
    void deleteAllByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.expiresAt < :now")
    void deleteAllExpiredTokens(OffsetDateTime now);

    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.revokedAt = :now WHERE rt.tokenHash = :tokenHash")
    void revokeToken(String tokenHash, OffsetDateTime now);
}

