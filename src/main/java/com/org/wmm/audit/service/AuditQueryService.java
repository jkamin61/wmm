package com.org.wmm.audit.service;

import com.org.wmm.audit.dto.AuditLogDto;
import com.org.wmm.audit.entity.AuditLogEntity;
import com.org.wmm.audit.mapper.AuditMapper;
import com.org.wmm.audit.repository.AuditLogRepository;
import com.org.wmm.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Query service for admin audit log listing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditQueryService {

    private final AuditLogRepository auditLogRepository;
    private final AuditMapper auditMapper;

    /**
     * Returns paginated, filtered audit log entries.
     *
     * @param entityType optional entity type filter (e.g. "item", "category", "image", "tasting_note")
     * @param entityId   optional entity ID filter
     * @param action     optional action filter (e.g. "create", "update", "publish", "delete")
     * @param userId     optional user ID filter
     * @param from       optional start date (inclusive)
     * @param to         optional end date (inclusive)
     * @param page       page number (0-based)
     * @param size       page size
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLogDto> getAuditLogs(String entityType, Long entityId,
                                                  String action, Long userId,
                                                  OffsetDateTime from, OffsetDateTime to,
                                                  int page, int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));

        Page<AuditLogEntity> auditPage = auditLogRepository.findFullyFiltered(
                entityType, entityId, action, userId, from, to, pageable);

        List<AuditLogDto> content = auditPage.getContent().stream()
                .map(auditMapper::toDto)
                .collect(Collectors.toList());

        log.debug("Audit query: type={}, entityId={}, action={}, userId={}, from={}, to={} → {} results",
                entityType, entityId, action, userId, from, to, auditPage.getTotalElements());

        return PageResponse.<AuditLogDto>builder()
                .content(content)
                .page(auditPage.getNumber())
                .size(auditPage.getSize())
                .totalElements(auditPage.getTotalElements())
                .totalPages(auditPage.getTotalPages())
                .first(auditPage.isFirst())
                .last(auditPage.isLast())
                .build();
    }
}

