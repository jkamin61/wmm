package com.org.wmm.audit.service;

import com.org.wmm.audit.entity.AuditLogEntity;
import com.org.wmm.audit.repository.AuditLogRepository;
import com.org.wmm.users.entity.UserEntity;
import com.org.wmm.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * Convenience method for create actions (only new values).
     */
    public void logCreate(String entityType, Long entityId, String newValues) {
        saveAuditLog(entityType, entityId, "create", null, newValues);
    }

    /**
     * Convenience method for update actions (old and new values).
     */
    public void logUpdate(String entityType, Long entityId, String oldValues, String newValues) {
        saveAuditLog(entityType, entityId, "update", oldValues, newValues);
    }

    /**
     * Convenience method for publish actions.
     */
    public void logPublish(String entityType, Long entityId) {
        saveAuditLog(entityType, entityId, "publish", null, null);
    }

    /**
     * Convenience method for unpublish actions.
     */
    public void logUnpublish(String entityType, Long entityId) {
        saveAuditLog(entityType, entityId, "unpublish", null, null);
    }

    /**
     * Convenience method for archive actions.
     */
    public void logArchive(String entityType, Long entityId) {
        saveAuditLog(entityType, entityId, "archive", null, null);
    }

    /**
     * Convenience method for delete actions.
     */
    public void logDelete(String entityType, Long entityId) {
        saveAuditLog(entityType, entityId, "delete", null, null);
    }

    // ─── PRIVATE ──────────────────────────────────────────────────

    private void saveAuditLog(String entityType, Long entityId, String action,
                              String oldValues, String newValues) {
        try {
            Long userId = getCurrentUserId();
            String ipAddress = getClientIpAddress();
            String userAgent = getClientUserAgent();

            AuditLogEntity auditLog = AuditLogEntity.builder()
                    .userId(userId)
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .oldValues(oldValues)
                    .newValues(newValues)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} {} {} by user {}", action, entityType, entityId, userId);
        } catch (Exception e) {
            log.error("Failed to create audit log: {} {} {}", action, entityType, entityId, e);
        }
    }

    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
                return userRepository.findByEmail(userDetails.getUsername())
                        .map(UserEntity::getId)
                        .orElse(null);
            }
        } catch (Exception e) {
            log.warn("Could not determine current user for audit log", e);
        }
        return null;
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.trace("Could not determine client IP", e);
        }
        return null;
    }

    private String getClientUserAgent() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String ua = attrs.getRequest().getHeader("User-Agent");
                return ua != null && ua.length() > 500 ? ua.substring(0, 500) : ua;
            }
        } catch (Exception e) {
            log.trace("Could not determine user agent", e);
        }
        return null;
    }
}
