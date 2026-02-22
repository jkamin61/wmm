package com.org.wmm.content.subtopics.service;

import com.org.wmm.audit.service.AuditService;
import com.org.wmm.common.dto.TranslationRequest;
import com.org.wmm.common.error.BadRequestException;
import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.content.subtopics.dto.AdminSubtopicDto;
import com.org.wmm.content.subtopics.dto.CreateSubtopicRequest;
import com.org.wmm.content.subtopics.entity.SubtopicEntity;
import com.org.wmm.content.subtopics.entity.SubtopicTranslationEntity;
import com.org.wmm.content.subtopics.mapper.SubtopicMapper;
import com.org.wmm.content.subtopics.repository.SubtopicRepository;
import com.org.wmm.content.topics.entity.TopicEntity;
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
class SubtopicAdminServiceTest {

    @Mock
    private SubtopicRepository subtopicRepository;
    @Mock
    private TopicRepository topicRepository;
    @Mock
    private LanguageQueryService languageQueryService;
    @Mock
    private SubtopicMapper subtopicMapper;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private SubtopicAdminService service;

    private LanguageEntity polish;
    private TopicEntity topic;
    private SubtopicEntity subtopic;
    private AdminSubtopicDto adminDto;

    @BeforeEach
    void setUp() {
        polish = LanguageEntity.builder().id(1L).code("pl").name("Polish").isDefault(true).isActive(true).build();

        topic = TopicEntity.builder().id(1L).slug("single-malt").isActive(true).status("published").build();

        subtopic = SubtopicEntity.builder()
                .id(1L).slug("islay").icon("map-pin")
                .topic(topic).displayOrder(1).isActive(true).status("draft")
                .translations(new ArrayList<>())
                .build();

        adminDto = AdminSubtopicDto.builder()
                .id(1L).slug("islay").topicId(1L)
                .status("draft").translations(List.of())
                .build();
    }

    @Nested
    @DisplayName("createSubtopic")
    class CreateSubtopic {

        @Test
        @DisplayName("should create subtopic successfully")
        void shouldCreateSubtopic() {
            CreateSubtopicRequest request = CreateSubtopicRequest.builder()
                    .topicId(1L).slug("islay").icon("map-pin").displayOrder(1)
                    .translations(List.of(
                            TranslationRequest.builder().languageCode("pl").title("Islay").build()
                    )).build();

            when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));
            when(subtopicRepository.existsBySlug("islay")).thenReturn(false);
            when(languageQueryService.resolveLanguage("pl")).thenReturn(polish);
            when(subtopicRepository.save(any(SubtopicEntity.class))).thenReturn(subtopic);
            when(subtopicRepository.findByIdWithTranslations(1L)).thenReturn(Optional.of(subtopic));
            when(subtopicMapper.toAdminDto(any(SubtopicEntity.class))).thenReturn(adminDto);

            AdminSubtopicDto result = service.createSubtopic(request);

            assertThat(result).isNotNull();
            assertThat(result.getSlug()).isEqualTo("islay");
            verify(subtopicRepository).save(any(SubtopicEntity.class));
            verify(auditService).logCreate(eq("subtopic"), eq(1L), anyString());
        }

        @Test
        @DisplayName("should throw when topic not found")
        void shouldThrowWhenTopicNotFound() {
            CreateSubtopicRequest request = CreateSubtopicRequest.builder()
                    .topicId(99L).slug("test")
                    .translations(List.of(TranslationRequest.builder().languageCode("pl").title("T").build()))
                    .build();

            when(topicRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createSubtopic(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when slug already exists")
        void shouldThrowWhenSlugExists() {
            CreateSubtopicRequest request = CreateSubtopicRequest.builder()
                    .topicId(1L).slug("islay")
                    .translations(List.of(TranslationRequest.builder().languageCode("pl").title("T").build()))
                    .build();

            when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));
            when(subtopicRepository.existsBySlug("islay")).thenReturn(true);

            assertThatThrownBy(() -> service.createSubtopic(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("publishSubtopic")
    class PublishSubtopic {

        @Test
        @DisplayName("should publish subtopic with default translation")
        void shouldPublish() {
            SubtopicTranslationEntity translation = SubtopicTranslationEntity.builder()
                    .id(1L).language(polish).title("Islay").build();
            subtopic.getTranslations().add(translation);

            when(subtopicRepository.findByIdWithTranslations(1L)).thenReturn(Optional.of(subtopic));
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);
            when(subtopicRepository.save(any(SubtopicEntity.class))).thenReturn(subtopic);
            when(subtopicMapper.toAdminDto(any(SubtopicEntity.class))).thenReturn(adminDto);

            service.publishSubtopic(1L);

            verify(auditService).logPublish("subtopic", 1L);
        }

        @Test
        @DisplayName("should throw when no default translation")
        void shouldThrowWhenNoDefaultTranslation() {
            when(subtopicRepository.findByIdWithTranslations(1L)).thenReturn(Optional.of(subtopic));
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            assertThatThrownBy(() -> service.publishSubtopic(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("default language");
        }
    }

    @Nested
    @DisplayName("deleteSubtopic")
    class DeleteSubtopic {

        @Test
        @DisplayName("should soft-delete subtopic")
        void shouldSoftDelete() {
            when(subtopicRepository.findById(1L)).thenReturn(Optional.of(subtopic));
            when(subtopicRepository.save(any(SubtopicEntity.class))).thenReturn(subtopic);

            service.deleteSubtopic(1L);

            assertThat(subtopic.getIsActive()).isFalse();
            assertThat(subtopic.getStatus()).isEqualTo("archived");
            verify(auditService).logDelete("subtopic", 1L);
        }
    }
}

