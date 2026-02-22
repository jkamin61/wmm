package com.org.wmm.content.topics.service;

import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.content.categories.entity.CategoryEntity;
import com.org.wmm.content.categories.repository.CategoryRepository;
import com.org.wmm.content.topics.dto.TopicDto;
import com.org.wmm.content.topics.entity.TopicEntity;
import com.org.wmm.content.topics.entity.TopicTranslationEntity;
import com.org.wmm.content.topics.mapper.TopicMapper;
import com.org.wmm.content.topics.repository.TopicRepository;
import com.org.wmm.languages.entity.LanguageEntity;
import com.org.wmm.languages.service.LanguageQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicQueryServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private LanguageQueryService languageQueryService;

    @Mock
    private TopicMapper topicMapper;

    @InjectMocks
    private TopicQueryService service;

    private CategoryEntity whiskyCategory;
    private TopicEntity singleMaltTopic;
    private TopicEntity bourbonTopic;
    private LanguageEntity polish;

    @BeforeEach
    void setUp() {
        polish = LanguageEntity.builder().id(1L).code("pl").isDefault(true).isActive(true).build();

        whiskyCategory = CategoryEntity.builder()
                .id(1L).slug("whisky").isActive(true).status("published").build();

        TopicTranslationEntity singleMaltTransPl = TopicTranslationEntity.builder()
                .id(1L).language(polish).title("Single Malt").subtitle("Szkocka")
                .description("Whisky z jednej destylarni").build();

        singleMaltTopic = TopicEntity.builder()
                .id(1L).slug("single-malt").icon("glass").category(whiskyCategory)
                .displayOrder(1).isActive(true).status("published")
                .translations(new ArrayList<>(List.of(singleMaltTransPl)))
                .build();

        bourbonTopic = TopicEntity.builder()
                .id(2L).slug("bourbon").icon("barrel").category(whiskyCategory)
                .displayOrder(2).isActive(true).status("published")
                .translations(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("getTopicsByCategorySlug")
    class GetTopicsByCategorySlug {

        @Test
        @DisplayName("should return topics for valid active category")
        void shouldReturnTopicsForValidCategory() {
            when(categoryRepository.findBySlugAndIsActiveTrue("whisky"))
                    .thenReturn(Optional.of(whiskyCategory));
            when(languageQueryService.resolveLanguageId("pl")).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);
            when(topicRepository.findActivePublishedByCategoryIdWithTranslations(1L))
                    .thenReturn(List.of(singleMaltTopic, bourbonTopic));

            TopicDto singleMaltDto = TopicDto.builder()
                    .id(1L).slug("single-malt").title("Single Malt").subtitle("Szkocka").build();
            TopicDto bourbonDto = TopicDto.builder()
                    .id(2L).slug("bourbon").title("bourbon").build();

            when(topicMapper.toDto(singleMaltTopic, 1L, 1L)).thenReturn(singleMaltDto);
            when(topicMapper.toDto(bourbonTopic, 1L, 1L)).thenReturn(bourbonDto);

            List<TopicDto> result = service.getTopicsByCategorySlug("whisky", "pl");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getSlug()).isEqualTo("single-malt");
            assertThat(result.get(0).getTitle()).isEqualTo("Single Malt");
            assertThat(result.get(1).getSlug()).isEqualTo("bourbon");

            verify(categoryRepository).findBySlugAndIsActiveTrue("whisky");
            verify(topicRepository).findActivePublishedByCategoryIdWithTranslations(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when category not found")
        void shouldThrowWhenCategoryNotFound() {
            when(categoryRepository.findBySlugAndIsActiveTrue("nonexistent"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getTopicsByCategorySlug("nonexistent", "pl"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Category")
                    .hasMessageContaining("nonexistent");

            verify(topicRepository, never()).findActivePublishedByCategoryIdWithTranslations(anyLong());
        }

        @Test
        @DisplayName("should return empty list when category has no published topics")
        void shouldReturnEmptyWhenNoTopics() {
            when(categoryRepository.findBySlugAndIsActiveTrue("whisky"))
                    .thenReturn(Optional.of(whiskyCategory));
            when(languageQueryService.resolveLanguageId("pl")).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);
            when(topicRepository.findActivePublishedByCategoryIdWithTranslations(1L))
                    .thenReturn(List.of());

            List<TopicDto> result = service.getTopicsByCategorySlug("whisky", "pl");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should resolve language before fetching topics")
        void shouldResolveLanguageFirst() {
            when(categoryRepository.findBySlugAndIsActiveTrue("whisky"))
                    .thenReturn(Optional.of(whiskyCategory));
            when(languageQueryService.resolveLanguageId("en")).thenReturn(2L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);
            when(topicRepository.findActivePublishedByCategoryIdWithTranslations(1L))
                    .thenReturn(List.of());

            service.getTopicsByCategorySlug("whisky", "en");

            verify(languageQueryService).resolveLanguageId("en");
            verify(languageQueryService).getDefaultLanguageId();
        }

        @Test
        @DisplayName("should pass null lang to language service when not provided")
        void shouldHandleNullLang() {
            when(categoryRepository.findBySlugAndIsActiveTrue("whisky"))
                    .thenReturn(Optional.of(whiskyCategory));
            when(languageQueryService.resolveLanguageId(null)).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);
            when(topicRepository.findActivePublishedByCategoryIdWithTranslations(1L))
                    .thenReturn(List.of());

            service.getTopicsByCategorySlug("whisky", null);

            verify(languageQueryService).resolveLanguageId(null);
        }
    }
}

