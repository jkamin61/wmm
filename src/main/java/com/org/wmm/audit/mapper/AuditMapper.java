package com.org.wmm.audit.mapper;

import com.org.wmm.audit.dto.AuditLogDto;
import com.org.wmm.audit.entity.AuditLogEntity;
import com.org.wmm.users.entity.UserEntity;
import com.org.wmm.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class AuditMapper {

    private final UserRepository userRepository;

    // Simple in-memory cache to avoid N+1 on user lookups during listing
    private final Map<Long, String> userEmailCache = new ConcurrentHashMap<>();

    public AuditLogDto toDto(AuditLogEntity entity) {
        return AuditLogDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .userEmail(resolveUserEmail(entity.getUserId()))
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .action(entity.getAction())
                .oldValues(entity.getOldValues())
                .newValues(entity.getNewValues())
                .ipAddress(entity.getIpAddress())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String resolveUserEmail(Long userId) {
        if (userId == null) return null;
        return userEmailCache.computeIfAbsent(userId, id ->
                userRepository.findById(id)
                        .map(UserEntity::getEmail)
                        .orElse("unknown (id=" + id + ")")
        );
    }
}

