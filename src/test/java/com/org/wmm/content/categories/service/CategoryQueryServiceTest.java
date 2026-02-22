package com.org.wmm.content.categories.service;

import com.org.wmm.content.categories.dto.CategoryMenuDto;
import com.org.wmm.content.categories.entity.CategoryEntity;
import com.org.wmm.content.categories.entity.CategoryTranslationEntity;
import com.org.wmm.content.categories.mapper.CategoryMapper;
import com.org.wmm.content.categories.repository.CategoryRepository;
import com.org.wmm.content.topics.entity.TopicEntity;
import com.org.wmm.content.topics.entity.TopicTranslationEntity;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryQueryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private LanguageQueryService languageQueryService;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryQueryService service;

    private LanguageEntity polish;
    private LanguageEntity english;
    private CategoryEntity whiskyCategory;
    private TopicEntity singleMaltTopic;

    @BeforeEach
    void setUp() {
        polish = LanguageEntity.builder().id(1L).code("pl").name("Polish").isDefault(true).isActive(true).build();
        english = LanguageEntity.builder().id(2L).code("en").name("English").isDefault(false).isActive(true).build();

        CategoryTranslationEntity whiskyTransPl = CategoryTranslationEntity.builder()
                .id(1L).language(polish).title("Whisky").description("Świat whisky").build();
        CategoryTranslationEntity whiskyTransEn = CategoryTranslationEntity.builder()
                .id(2L).language(english).title("Whisky").description("World of whisky").build();

        whiskyCategory = CategoryEntity.builder()
                .id(1L).slug("whisky").icon("whiskey-glass")
                .displayOrder(1).isActive(true).status("published")
                .translations(new ArrayList<>(List.of(whiskyTransPl, whiskyTransEn)))
                .build();

        TopicTranslationEntity topicTransPl = TopicTranslationEntity.builder()
                .id(1L).language(polish).title("Single Malt").subtitle("Szkocka").build();

        singleMaltTopic = TopicEntity.builder()
                .id(1L).slug("single-malt").icon("glass")
                .category(whiskyCategory).displayOrder(1).isActive(true).status("published")
                .translations(new ArrayList<>(List.of(topicTransPl)))
                .build();
    }

    @Nested
    @DisplayName("getMenu")
    class GetMenu {

        @Test
        @DisplayName("should return menu with categories and topics")
        void shouldReturnMenuWithCategoriesAndTopics() {
            when(languageQueryService.resolveLanguageId("pl")).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);
            when(categoryRepository.findActivePublishedWithTranslations())
                    .thenReturn(List.of(whiskyCategory));
            when(topicRepository.findActivePublishedByCategoryIdWithTranslations(1L))
                    .thenReturn(List.of(singleMaltTopic));

            CategoryMenuDto expectedDto = CategoryMenuDto.builder()
                    .id(1L).slug("whisky").icon("whiskey-glass")
                    .title("Whisky").description("Świat whisky")
                    .topics(List.of(
                            CategoryMenuDto.TopicMenuDto.builder()
                                    .id(1L).slug("single-malt").icon("glass")
                                    .title("Single Malt").subtitle("Szkocka").build()
                    )).build();

            when(categoryMapper.toMenuDto(whiskyCategory, 1L, 1L, List.of(singleMaltTopic)))
                    .thenReturn(expectedDto);

            List<CategoryMenuDto> result = service.getMenu("pl");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSlug()).isEqualTo("whisky");
            assertThat(result.get(0).getTitle()).isEqualTo("Whisky");
            assertThat(result.get(0).getTopics()).hasSize(1);
            assertThat(result.get(0).getTopics().get(0).getSlug()).isEqualTo("single-malt");

            verify(categoryRepository).findActivePublishedWithTranslations();
            verify(topicRepository).findActivePublishedByCategoryIdWithTranslations(1L);
        }

        @Test
        @DisplayName("should return empty list when no published categories")
        void shouldReturnEmptyWhenNoCategories() {
            when(languageQueryService.resolveLanguageId("pl")).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);
            when(categoryRepository.findActivePublishedWithTranslations())
                    .thenReturn(List.of());

            List<CategoryMenuDto> result = service.getMenu("pl");

            assertThat(result).isEmpty();
            verify(topicRepository, never()).findActivePublishedByCategoryIdWithTranslations(anyLong());
        }

        @Test
        @DisplayName("should return category with empty topics when no published topics exist")
        void shouldReturnCategoryWithEmptyTopics() {
            when(languageQueryService.resolveLanguageId("en")).thenReturn(2L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);
            when(categoryRepository.findActivePublishedWithTranslations())
                    .thenReturn(List.of(whiskyCategory));
            when(topicRepository.findActivePublishedByCategoryIdWithTranslations(1L))
                    .thenReturn(List.of());

            CategoryMenuDto emptyTopicsDto = CategoryMenuDto.builder()
                    .id(1L).slug("whisky").title("Whisky")
                    .topics(List.of()).build();

            when(categoryMapper.toMenuDto(whiskyCategory, 2L, 1L, List.of()))
                    .thenReturn(emptyTopicsDto);

            List<CategoryMenuDto> result = service.getMenu("en");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTopics()).isEmpty();
        }

        @Test
        @DisplayName("should delegate language resolution to LanguageQueryService")
        void shouldDelegateLanguageResolution() {
            when(languageQueryService.resolveLanguageId(null)).thenReturn(1L);
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);
            when(categoryRepository.findActivePublishedWithTranslations()).thenReturn(List.of());

            service.getMenu(null);

            verify(languageQueryService).resolveLanguageId(null);
            verify(languageQueryService).getDefaultLanguageId();
        }
    }
}

