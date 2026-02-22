package com.org.wmm.content.topics.service;

import com.org.wmm.audit.service.AuditService;
import com.org.wmm.common.dto.TranslationRequest;
import com.org.wmm.common.error.BadRequestException;
import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.content.categories.entity.CategoryEntity;
import com.org.wmm.content.categories.repository.CategoryRepository;
import com.org.wmm.content.topics.dto.AdminTopicDto;
import com.org.wmm.content.topics.dto.CreateTopicRequest;
import com.org.wmm.content.topics.dto.UpdateTopicRequest;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicAdminServiceTest {

    @Mock
    private TopicRepository topicRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private LanguageQueryService languageQueryService;
    @Mock
    private TopicMapper topicMapper;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private TopicAdminService service;

    private LanguageEntity polish;
    private CategoryEntity category;
    private TopicEntity topic;
    private AdminTopicDto adminDto;

    @BeforeEach
    void setUp() {
        polish = LanguageEntity.builder().id(1L).code("pl").name("Polish").isDefault(true).isActive(true).build();

        category = CategoryEntity.builder()
                .id(1L).slug("whisky").isActive(true).status("published")
                .build();

        topic = TopicEntity.builder()
                .id(1L).slug("single-malt").icon("glass")
                .category(category).displayOrder(1).isActive(true).status("draft")
                .translations(new ArrayList<>())
                .build();

        adminDto = AdminTopicDto.builder()
                .id(1L).slug("single-malt").categoryId(1L)
                .status("draft").translations(List.of())
                .build();
    }

    @Nested
    @DisplayName("createTopic")
    class CreateTopic {

        @Test
        @DisplayName("should create topic successfully")
        void shouldCreateTopic() {
            CreateTopicRequest request = CreateTopicRequest.builder()
                    .categoryId(1L).slug("single-malt").icon("glass").displayOrder(1)
                    .translations(List.of(
                            TranslationRequest.builder().languageCode("pl").title("Single Malt").build()
                    )).build();

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(topicRepository.existsBySlug("single-malt")).thenReturn(false);
            when(languageQueryService.resolveLanguage("pl")).thenReturn(polish);
            when(topicRepository.save(any(TopicEntity.class))).thenReturn(topic);
            when(topicRepository.findByIdWithTranslations(1L)).thenReturn(Optional.of(topic));
            when(topicMapper.toAdminDto(any(TopicEntity.class))).thenReturn(adminDto);

            AdminTopicDto result = service.createTopic(request);

            assertThat(result).isNotNull();
            assertThat(result.getSlug()).isEqualTo("single-malt");
            verify(topicRepository).save(any(TopicEntity.class));
            verify(auditService).logCreate(eq("topic"), eq(1L), anyString());
        }

        @Test
        @DisplayName("should throw when category not found")
        void shouldThrowWhenCategoryNotFound() {
            CreateTopicRequest request = CreateTopicRequest.builder()
                    .categoryId(99L).slug("test")
                    .translations(List.of(TranslationRequest.builder().languageCode("pl").title("T").build()))
                    .build();

            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createTopic(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when slug already exists")
        void shouldThrowWhenSlugExists() {
            CreateTopicRequest request = CreateTopicRequest.builder()
                    .categoryId(1L).slug("single-malt")
                    .translations(List.of(TranslationRequest.builder().languageCode("pl").title("T").build()))
                    .build();

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(topicRepository.existsBySlug("single-malt")).thenReturn(true);

            assertThatThrownBy(() -> service.createTopic(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("publishTopic")
    class PublishTopic {

        @Test
        @DisplayName("should publish topic with default translation")
        void shouldPublishTopic() {
            TopicTranslationEntity translation = TopicTranslationEntity.builder()
                    .id(1L).language(polish).title("Single Malt").build();
            topic.getTranslations().add(translation);

            when(topicRepository.findByIdWithTranslations(1L)).thenReturn(Optional.of(topic));
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);
            when(topicRepository.save(any(TopicEntity.class))).thenReturn(topic);
            when(topicMapper.toAdminDto(any(TopicEntity.class))).thenReturn(adminDto);

            service.publishTopic(1L);

            verify(auditService).logPublish("topic", 1L);
        }

        @Test
        @DisplayName("should throw when no default translation")
        void shouldThrowWhenNoDefaultTranslation() {
            when(topicRepository.findByIdWithTranslations(1L)).thenReturn(Optional.of(topic));
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            assertThatThrownBy(() -> service.publishTopic(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("default language");
        }
    }

    @Nested
    @DisplayName("deleteTopic")
    class DeleteTopic {

        @Test
        @DisplayName("should soft-delete topic")
        void shouldSoftDelete() {
            when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));
            when(topicRepository.save(any(TopicEntity.class))).thenReturn(topic);

            service.deleteTopic(1L);

            assertThat(topic.getIsActive()).isFalse();
            assertThat(topic.getStatus()).isEqualTo("archived");
            verify(auditService).logDelete("topic", 1L);
        }
    }
}

