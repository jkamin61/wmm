package com.org.wmm.content.items.service;

import com.org.wmm.common.dto.PageResponse;
import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.content.items.dto.ItemDetailDto;
import com.org.wmm.content.items.dto.ItemSummaryDto;
import com.org.wmm.content.items.entity.ItemEntity;
import com.org.wmm.content.items.mapper.ItemMapper;
import com.org.wmm.content.items.repository.ItemRepository;
import com.org.wmm.content.topics.entity.TopicEntity;
import com.org.wmm.content.topics.repository.TopicRepository;
import com.org.wmm.languages.service.LanguageQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemQueryServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private LanguageQueryService languageQueryService;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemQueryService service;

    private TopicEntity singleMaltTopic;
    private ItemEntity taliskerItem;
    private ItemEntity lagavulinItem;

    @BeforeEach
    void setUp() {
        singleMaltTopic = TopicEntity.builder()
                .id(1L).slug("single-malt").isActive(true).status("published").build();

        taliskerItem = ItemEntity.builder()
                .id(1L).slug("talisker-10").status("published")
                .abv(new BigDecimal("45.80")).isFeatured(true)
                .publishedAt(OffsetDateTime.now().minusDays(1))
                .translations(new ArrayList<>()).images(new ArrayList<>())
                .build();

        lagavulinItem = ItemEntity.builder()
                .id(2L).slug("lagavulin-16").status("published")
                .abv(new BigDecimal("43.00")).isFeatured(false)
                .publishedAt(OffsetDateTime.now().minusDays(2))
                .translations(new ArrayList<>()).images(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("getItemsByTopicSlug")
    class GetItemsByTopicSlug {

        @Test
        @DisplayName("should return paginated items for valid topic")
        void shouldReturnPaginatedItems() {
            when(topicRepository.findBySlugAndIsActiveTrue("single-malt"))
                    .thenReturn(Optional.of(singleMaltTopic));
            when(languageQueryService.resolveLanguageId("pl")).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            Page<ItemEntity> page = new PageImpl<>(
                    List.of(taliskerItem, lagavulinItem),
                    Pageable.ofSize(10), 2);
            when(itemRepository.findPublishedByTopicId(eq(1L), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(page);

            ItemSummaryDto taliskerDto = ItemSummaryDto.builder()
                    .id(1L).slug("talisker-10").title("Talisker 10").featured(true).build();
            ItemSummaryDto lagavulinDto = ItemSummaryDto.builder()
                    .id(2L).slug("lagavulin-16").title("Lagavulin 16").featured(false).build();

            when(itemMapper.toSummaryDto(taliskerItem, 1L, 1L)).thenReturn(taliskerDto);
            when(itemMapper.toSummaryDto(lagavulinItem, 1L, 1L)).thenReturn(lagavulinDto);

            PageResponse<ItemSummaryDto> result = service.getItemsByTopicSlug(
                    "single-malt", null, null, "pl", 0, 10, "newest");

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getSlug()).isEqualTo("talisker-10");
            assertThat(result.getContent().get(1).getSlug()).isEqualTo("lagavulin-16");
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when topic not found")
        void shouldThrowWhenTopicNotFound() {
            when(topicRepository.findBySlugAndIsActiveTrue("nonexistent"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getItemsByTopicSlug(
                    "nonexistent", null, null, "pl", 0, 10, "newest"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Topic")
                    .hasMessageContaining("nonexistent");

            verify(itemRepository, never()).findPublishedByTopicId(anyLong(), any(), any(), any());
        }

        @Test
        @DisplayName("should return empty page when topic has no published items")
        void shouldReturnEmptyPageWhenNoItems() {
            when(topicRepository.findBySlugAndIsActiveTrue("single-malt"))
                    .thenReturn(Optional.of(singleMaltTopic));
            when(languageQueryService.resolveLanguageId("pl")).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            Page<ItemEntity> emptyPage = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);
            when(itemRepository.findPublishedByTopicId(eq(1L), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(emptyPage);

            PageResponse<ItemSummaryDto> result = service.getItemsByTopicSlug(
                    "single-malt", null, null, "pl", 0, 10, "newest");

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("should pass featured filter to repository")
        void shouldPassFeaturedFilter() {
            when(topicRepository.findBySlugAndIsActiveTrue("single-malt"))
                    .thenReturn(Optional.of(singleMaltTopic));
            when(languageQueryService.resolveLanguageId("pl")).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            Page<ItemEntity> page = new PageImpl<>(List.of(taliskerItem), Pageable.ofSize(10), 1);
            when(itemRepository.findPublishedByTopicId(eq(1L), isNull(), eq(true), any(Pageable.class)))
                    .thenReturn(page);
            when(itemMapper.toSummaryDto(any(), eq(1L), eq(1L)))
                    .thenReturn(ItemSummaryDto.builder().id(1L).slug("talisker-10").build());

            PageResponse<ItemSummaryDto> result = service.getItemsByTopicSlug(
                    "single-malt", null, true, "pl", 0, 10, "newest");

            assertThat(result.getContent()).hasSize(1);
            verify(itemRepository).findPublishedByTopicId(eq(1L), isNull(), eq(true), any(Pageable.class));
        }

        @Test
        @DisplayName("should sort by publishedAt DESC for 'newest'")
        void shouldSortByNewest() {
            when(topicRepository.findBySlugAndIsActiveTrue("single-malt"))
                    .thenReturn(Optional.of(singleMaltTopic));
            when(languageQueryService.resolveLanguageId("pl")).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            Page<ItemEntity> page = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);
            when(itemRepository.findPublishedByTopicId(anyLong(), any(), any(), any(Pageable.class)))
                    .thenReturn(page);

            service.getItemsByTopicSlug("single-malt", null, null, "pl", 0, 10, "newest");

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(itemRepository).findPublishedByTopicId(anyLong(), any(), any(), pageableCaptor.capture());

            Sort sort = pageableCaptor.getValue().getSort();
            assertThat(sort.getOrderFor("publishedAt")).isNotNull();
            assertThat(sort.getOrderFor("publishedAt").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("should sort by publishedAt ASC for 'oldest'")
        void shouldSortByOldest() {
            when(topicRepository.findBySlugAndIsActiveTrue("single-malt"))
                    .thenReturn(Optional.of(singleMaltTopic));
            when(languageQueryService.resolveLanguageId("pl")).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            Page<ItemEntity> page = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);
            when(itemRepository.findPublishedByTopicId(anyLong(), any(), any(), any(Pageable.class)))
                    .thenReturn(page);

            service.getItemsByTopicSlug("single-malt", null, null, "pl", 0, 10, "oldest");

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(itemRepository).findPublishedByTopicId(anyLong(), any(), any(), pageableCaptor.capture());

            Sort sort = pageableCaptor.getValue().getSort();
            assertThat(sort.getOrderFor("publishedAt")).isNotNull();
            assertThat(sort.getOrderFor("publishedAt").getDirection()).isEqualTo(Sort.Direction.ASC);
        }

        @Test
        @DisplayName("should sort by score DESC then publishedAt DESC for 'score'")
        void shouldSortByScore() {
            when(topicRepository.findBySlugAndIsActiveTrue("single-malt"))
                    .thenReturn(Optional.of(singleMaltTopic));
            when(languageQueryService.resolveLanguageId("pl")).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            Page<ItemEntity> page = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);
            when(itemRepository.findPublishedByTopicId(anyLong(), any(), any(), any(Pageable.class)))
                    .thenReturn(page);

            service.getItemsByTopicSlug("single-malt", null, null, "pl", 0, 10, "score");

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(itemRepository).findPublishedByTopicId(anyLong(), any(), any(), pageableCaptor.capture());

            Sort sort = pageableCaptor.getValue().getSort();
            List<Sort.Order> orders = sort.toList();
            assertThat(orders).hasSizeGreaterThanOrEqualTo(2);
            assertThat(orders.get(0).getProperty()).isEqualTo("tastingNote.overallScore");
            assertThat(orders.get(0).getDirection()).isEqualTo(Sort.Direction.DESC);
            assertThat(orders.get(1).getProperty()).isEqualTo("publishedAt");
            assertThat(orders.get(1).getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("should default to 'newest' sort for unknown sort value")
        void shouldDefaultToNewestForUnknownSort() {
            when(topicRepository.findBySlugAndIsActiveTrue("single-malt"))
                    .thenReturn(Optional.of(singleMaltTopic));
            when(languageQueryService.resolveLanguageId("pl")).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            Page<ItemEntity> page = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);
            when(itemRepository.findPublishedByTopicId(anyLong(), any(), any(), any(Pageable.class)))
                    .thenReturn(page);

            service.getItemsByTopicSlug("single-malt", null, null, "pl", 0, 10, "invalid");

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(itemRepository).findPublishedByTopicId(anyLong(), any(), any(), pageableCaptor.capture());

            Sort sort = pageableCaptor.getValue().getSort();
            assertThat(sort.getOrderFor("publishedAt")).isNotNull();
            assertThat(sort.getOrderFor("publishedAt").getDirection()).isEqualTo(Sort.Direction.DESC);
        }
    }

    @Nested
    @DisplayName("getItemBySlug")
    class GetItemBySlug {

        @Test
        @DisplayName("should return item detail for published item")
        void shouldReturnItemDetail() {
            when(itemRepository.findPublishedBySlugWithDetails("talisker-10"))
                    .thenReturn(Optional.of(taliskerItem));
            when(languageQueryService.resolveLanguageId("pl")).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            ItemDetailDto expectedDto = ItemDetailDto.builder()
                    .id(1L).slug("talisker-10").title("Talisker 10 Year Old")
                    .abv(new BigDecimal("45.80")).build();
            when(itemMapper.toDetailDto(taliskerItem, 1L, 1L)).thenReturn(expectedDto);

            ItemDetailDto result = service.getItemBySlug("talisker-10", "pl");

            assertThat(result.getSlug()).isEqualTo("talisker-10");
            assertThat(result.getTitle()).isEqualTo("Talisker 10 Year Old");
            assertThat(result.getAbv()).isEqualTo(new BigDecimal("45.80"));

            verify(itemRepository).findPublishedBySlugWithDetails("talisker-10");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when item not found")
        void shouldThrowWhenItemNotFound() {
            when(itemRepository.findPublishedBySlugWithDetails("nonexistent"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getItemBySlug("nonexistent", "pl"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Item")
                    .hasMessageContaining("nonexistent");
        }

        @Test
        @DisplayName("should resolve language for item detail")
        void shouldResolveLanguage() {
            when(itemRepository.findPublishedBySlugWithDetails("talisker-10"))
                    .thenReturn(Optional.of(taliskerItem));
            when(languageQueryService.resolveLanguageId("en")).thenReturn(2L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);
            when(itemMapper.toDetailDto(taliskerItem, 2L, 1L))
                    .thenReturn(ItemDetailDto.builder().id(1L).slug("talisker-10").build());

            service.getItemBySlug("talisker-10", "en");

            verify(languageQueryService).resolveLanguageId("en");
            verify(itemMapper).toDetailDto(taliskerItem, 2L, 1L);
        }
    }
}

