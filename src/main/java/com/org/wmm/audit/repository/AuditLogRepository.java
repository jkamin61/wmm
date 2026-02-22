package com.org.wmm.audit.repository;

import com.org.wmm.audit.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    Page<AuditLogEntity> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    Page<AuditLogEntity> findByEntityType(String entityType, Pageable pageable);

    Page<AuditLogEntity> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT a FROM AuditLogEntity a WHERE " +
            "(:entityType IS NULL OR a.entityType = :entityType) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:userId IS NULL OR a.userId = :userId) " +
            "ORDER BY a.createdAt DESC")
    Page<AuditLogEntity> findFiltered(
            @Param("entityType") String entityType,
            @Param("action") String action,
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * Full filtering with entityId and date range support.
     */
    @Query("SELECT a FROM AuditLogEntity a WHERE " +
            "(:entityType IS NULL OR a.entityType = :entityType) AND " +
            "(:entityId IS NULL OR a.entityId = :entityId) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:userId IS NULL OR a.userId = :userId) AND " +
            "(:from IS NULL OR a.createdAt >= :from) AND " +
            "(:to IS NULL OR a.createdAt <= :to) " +
            "ORDER BY a.createdAt DESC")
    Page<AuditLogEntity> findFullyFiltered(
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId,
            @Param("action") String action,
            @Param("userId") Long userId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            Pageable pageable
    );
}

