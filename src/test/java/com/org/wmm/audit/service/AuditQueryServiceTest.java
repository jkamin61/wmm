package com.org.wmm.audit.service;

import com.org.wmm.audit.dto.AuditLogDto;
import com.org.wmm.audit.entity.AuditLogEntity;
import com.org.wmm.audit.mapper.AuditMapper;
import com.org.wmm.audit.repository.AuditLogRepository;
import com.org.wmm.common.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditQueryServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private AuditMapper auditMapper;

    @InjectMocks
    private AuditQueryService service;

    private AuditLogEntity auditEntry;
    private AuditLogDto auditDto;

    @BeforeEach
    void setUp() {
        auditEntry = AuditLogEntity.builder()
                .id(1L)
                .userId(1L)
                .entityType("item")
                .entityId(42L)
                .action("create")
                .newValues("{\"slug\":\"talisker-10\"}")
                .createdAt(OffsetDateTime.now())
                .build();

        auditDto = AuditLogDto.builder()
                .id(1L)
                .userId(1L)
                .userEmail("admin@wmm.com")
                .entityType("item")
                .entityId(42L)
                .action("create")
                .newValues("{\"slug\":\"talisker-10\"}")
                .createdAt(auditEntry.getCreatedAt())
                .build();
    }

    @Nested
    @DisplayName("getAuditLogs")
    class GetAuditLogs {

        @Test
        @DisplayName("should return all entries without filters")
        void shouldReturnAllWithoutFilters() {
            Page<AuditLogEntity> page = new PageImpl<>(List.of(auditEntry));
            when(auditLogRepository.findFullyFiltered(isNull(), isNull(), isNull(),
                    isNull(), isNull(), isNull(), any(Pageable.class))).thenReturn(page);
            when(auditMapper.toDto(auditEntry)).thenReturn(auditDto);

            PageResponse<AuditLogDto> result = service.getAuditLogs(
                    null, null, null, null, null, null, 0, 20);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEntityType()).isEqualTo("item");
            assertThat(result.getContent().get(0).getUserEmail()).isEqualTo("admin@wmm.com");
        }

        @Test
        @DisplayName("should filter by entityType and entityId")
        void shouldFilterByEntityTypeAndId() {
            Page<AuditLogEntity> page = new PageImpl<>(List.of(auditEntry));
            when(auditLogRepository.findFullyFiltered(eq("item"), eq(42L), isNull(),
                    isNull(), isNull(), isNull(), any(Pageable.class))).thenReturn(page);
            when(auditMapper.toDto(auditEntry)).thenReturn(auditDto);

            PageResponse<AuditLogDto> result = service.getAuditLogs(
                    "item", 42L, null, null, null, null, 0, 20);

            assertThat(result.getContent()).hasSize(1);
            verify(auditLogRepository).findFullyFiltered(eq("item"), eq(42L), isNull(),
                    isNull(), isNull(), isNull(), any());
        }

        @Test
        @DisplayName("should filter by action")
        void shouldFilterByAction() {
            Page<AuditLogEntity> page = new PageImpl<>(List.of(auditEntry));
            when(auditLogRepository.findFullyFiltered(isNull(), isNull(), eq("create"),
                    isNull(), isNull(), isNull(), any(Pageable.class))).thenReturn(page);
            when(auditMapper.toDto(auditEntry)).thenReturn(auditDto);

            service.getAuditLogs(null, null, "create", null, null, null, 0, 20);

            verify(auditLogRepository).findFullyFiltered(isNull(), isNull(), eq("create"),
                    isNull(), isNull(), isNull(), any());
        }

        @Test
        @DisplayName("should filter by date range")
        void shouldFilterByDateRange() {
            OffsetDateTime from = OffsetDateTime.parse("2026-02-01T00:00:00+01:00");
            OffsetDateTime to = OffsetDateTime.parse("2026-02-28T23:59:59+01:00");

            Page<AuditLogEntity> page = new PageImpl<>(List.of());
            when(auditLogRepository.findFullyFiltered(isNull(), isNull(), isNull(),
                    isNull(), eq(from), eq(to), any(Pageable.class))).thenReturn(page);

            PageResponse<AuditLogDto> result = service.getAuditLogs(
                    null, null, null, null, from, to, 0, 20);

            assertThat(result.getContent()).isEmpty();
            verify(auditLogRepository).findFullyFiltered(isNull(), isNull(), isNull(),
                    isNull(), eq(from), eq(to), any());
        }

        @Test
        @DisplayName("should filter by userId")
        void shouldFilterByUserId() {
            Page<AuditLogEntity> page = new PageImpl<>(List.of(auditEntry));
            when(auditLogRepository.findFullyFiltered(isNull(), isNull(), isNull(),
                    eq(1L), isNull(), isNull(), any(Pageable.class))).thenReturn(page);
            when(auditMapper.toDto(auditEntry)).thenReturn(auditDto);

            service.getAuditLogs(null, null, null, 1L, null, null, 0, 20);

            verify(auditLogRepository).findFullyFiltered(isNull(), isNull(), isNull(),
                    eq(1L), isNull(), isNull(), any());
        }

        @Test
        @DisplayName("should cap page size at 100")
        void shouldCapPageSize() {
            Page<AuditLogEntity> page = new PageImpl<>(List.of());
            when(auditLogRepository.findFullyFiltered(any(), any(), any(), any(), any(), any(),
                    any(Pageable.class))).thenReturn(page);

            service.getAuditLogs(null, null, null, null, null, null, 0, 500);

            verify(auditLogRepository).findFullyFiltered(any(), any(), any(), any(), any(), any(),
                    argThat((Pageable p) -> p.getPageSize() == 100));
        }

        @Test
        @DisplayName("should return paginated results")
        void shouldReturnPaginatedResults() {
            AuditLogEntity entry2 = AuditLogEntity.builder()
                    .id(2L).entityType("category").entityId(1L).action("update")
                    .createdAt(OffsetDateTime.now()).build();
            AuditLogDto dto2 = AuditLogDto.builder()
                    .id(2L).entityType("category").entityId(1L).action("update").build();

            Page<AuditLogEntity> page = new PageImpl<>(List.of(auditEntry, entry2));
            when(auditLogRepository.findFullyFiltered(any(), any(), any(), any(), any(), any(),
                    any(Pageable.class))).thenReturn(page);
            when(auditMapper.toDto(auditEntry)).thenReturn(auditDto);
            when(auditMapper.toDto(entry2)).thenReturn(dto2);

            PageResponse<AuditLogDto> result = service.getAuditLogs(
                    null, null, null, null, null, null, 0, 20);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
        }
    }
}

