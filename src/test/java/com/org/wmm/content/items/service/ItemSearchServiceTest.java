package com.org.wmm.content.items.service;

import com.org.wmm.common.dto.PageResponse;
import com.org.wmm.content.items.dto.ItemSummaryDto;
import com.org.wmm.content.items.entity.ItemEntity;
import com.org.wmm.content.items.mapper.ItemMapper;
import com.org.wmm.content.items.repository.ItemSearchRepository;
import com.org.wmm.languages.service.LanguageQueryService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemSearchServiceTest {

    @Mock
    private ItemSearchRepository searchRepository;
    @Mock
    private LanguageQueryService languageQueryService;
    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemSearchService service;

    private ItemEntity item;
    private ItemSummaryDto itemDto;

    @BeforeEach
    void setUp() {
        item = ItemEntity.builder().id(1L).slug("talisker-10").status("published").build();
        itemDto = ItemSummaryDto.builder().id(1L).slug("talisker-10").title("Talisker 10").build();
    }

    @Nested
    @DisplayName("search")
    class Search {

        @Test
        @DisplayName("should perform FTS search with query")
        void shouldSearchWithQuery() {
            when(languageQueryService.resolveLanguageId("pl")).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            Page<ItemEntity> page = new PageImpl<>(List.of(item));
            when(searchRepository.searchFts(eq("talisker"), eq(1L),
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                    any(Pageable.class))).thenReturn(page);
            when(itemMapper.toSummaryDto(any(), eq(1L), eq(1L))).thenReturn(itemDto);

            PageResponse<ItemSummaryDto> result = service.search(
                    "talisker", "pl", null, null, null, null, null, null, null, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getSlug()).isEqualTo("talisker-10");
            verify(searchRepository).searchFts(eq("talisker"), eq(1L),
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any());
        }

        @Test
        @DisplayName("should browse without query (filter-only mode)")
        void shouldBrowseWithoutQuery() {
            when(languageQueryService.resolveLanguageId("pl")).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            Page<ItemEntity> page = new PageImpl<>(List.of(item));
            when(searchRepository.browseFiltered(isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull(), any(Pageable.class))).thenReturn(page);
            when(itemMapper.toSummaryDto(any(), eq(1L), eq(1L))).thenReturn(itemDto);

            PageResponse<ItemSummaryDto> result = service.search(
                    null, "pl", null, null, null, null, null, null, null, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            verify(searchRepository).browseFiltered(isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull(), any());
        }

        @Test
        @DisplayName("should blank query fallback to browse")
        void shouldFallbackOnBlankQuery() {
            when(languageQueryService.resolveLanguageId("en")).thenReturn(2L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            Page<ItemEntity> page = new PageImpl<>(List.of());
            when(searchRepository.browseFiltered(isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull(), any(Pageable.class))).thenReturn(page);

            PageResponse<ItemSummaryDto> result = service.search(
                    "   ", "en", null, null, null, null, null, null, null, 0, 10);

            assertThat(result.getContent()).isEmpty();
            verify(searchRepository).browseFiltered(any(), any(), any(), any(), any(), any(), any());
            verify(searchRepository, never()).searchFts(any(), any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("should apply FTS with flavor filter")
        void shouldSearchWithFlavors() {
            when(languageQueryService.resolveLanguageId("pl")).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            Page<ItemEntity> page = new PageImpl<>(List.of(item));
            when(searchRepository.searchFtsWithFlavors(eq("whisky"), eq(1L),
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                    eq(List.of("smoke", "peat")), any(Pageable.class))).thenReturn(page);
            when(itemMapper.toSummaryDto(any(), eq(1L), eq(1L))).thenReturn(itemDto);

            PageResponse<ItemSummaryDto> result = service.search(
                    "whisky", "pl", null, null, null, null, null, null,
                    List.of("smoke", "peat"), 0, 10);

            assertThat(result.getContent()).hasSize(1);
            verify(searchRepository).searchFtsWithFlavors(any(), any(), any(), any(), any(), any(),
                    any(), any(), eq(List.of("smoke", "peat")), any());
        }

        @Test
        @DisplayName("should browse with flavors (no query)")
        void shouldBrowseWithFlavors() {
            when(languageQueryService.resolveLanguageId("pl")).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            Page<ItemEntity> page = new PageImpl<>(List.of());
            when(searchRepository.browseFilteredWithFlavors(isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull(), eq(List.of("honey")), any(Pageable.class))).thenReturn(page);

            service.search(null, "pl", null, null, null, null, null, null,
                    List.of("honey"), 0, 10);

            verify(searchRepository).browseFilteredWithFlavors(any(), any(), any(), any(),
                    any(), any(), eq(List.of("honey")), any());
        }

        @Test
        @DisplayName("should apply category and score filters with FTS")
        void shouldApplyCategoryAndScoreFilters() {
            when(languageQueryService.resolveLanguageId("pl")).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            Page<ItemEntity> page = new PageImpl<>(List.of(item));
            BigDecimal min = new BigDecimal("80");
            BigDecimal max = new BigDecimal("95");

            when(searchRepository.searchFts(eq("malt"), eq(1L),
                    eq(1L), isNull(), isNull(), eq(true), eq(min), eq(max),
                    any(Pageable.class))).thenReturn(page);
            when(itemMapper.toSummaryDto(any(), eq(1L), eq(1L))).thenReturn(itemDto);

            PageResponse<ItemSummaryDto> result = service.search(
                    "malt", "pl", 1L, null, null, true, min, max, null, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            verify(searchRepository).searchFts(eq("malt"), eq(1L),
                    eq(1L), isNull(), isNull(), eq(true), eq(min), eq(max), any());
        }

        @Test
        @DisplayName("should cap page size at 100")
        void shouldCapPageSize() {
            when(languageQueryService.resolveLanguageId(null)).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            Page<ItemEntity> page = new PageImpl<>(List.of());
            when(searchRepository.browseFiltered(any(), any(), any(), any(), any(), any(),
                    any(Pageable.class))).thenReturn(page);

            service.search(null, null, null, null, null, null, null, null, null, 0, 500);

            verify(searchRepository).browseFiltered(any(), any(), any(), any(), any(), any(),
                    argThat((Pageable p) -> p.getPageSize() == 100));
        }
    }
}

