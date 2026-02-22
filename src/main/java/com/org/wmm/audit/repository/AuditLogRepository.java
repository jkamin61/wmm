package com.org.wmm.audit.repository;

import com.org.wmm.audit.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}

