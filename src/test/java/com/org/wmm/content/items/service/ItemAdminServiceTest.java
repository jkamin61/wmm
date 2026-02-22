package com.org.wmm.content.items.service;

import com.org.wmm.audit.service.AuditService;
import com.org.wmm.common.dto.TranslationRequest;
import com.org.wmm.common.error.BadRequestException;
import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.content.categories.entity.CategoryEntity;
import com.org.wmm.content.categories.repository.CategoryRepository;
import com.org.wmm.content.items.dto.AdminItemDto;
import com.org.wmm.content.items.dto.CreateItemRequest;
import com.org.wmm.content.items.dto.UpdateItemRequest;
import com.org.wmm.content.items.entity.ItemEntity;
import com.org.wmm.content.items.entity.ItemTranslationEntity;
import com.org.wmm.content.items.mapper.ItemMapper;
import com.org.wmm.content.items.repository.ItemRepository;
import com.org.wmm.content.subtopics.entity.SubtopicEntity;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemAdminServiceTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TopicRepository topicRepository;
    @Mock
    private SubtopicRepository subtopicRepository;
    @Mock
    private LanguageQueryService languageQueryService;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private ItemAdminService service;

    private LanguageEntity polish;
    private CategoryEntity category;
    private TopicEntity topic;
    private SubtopicEntity subtopic;
    private ItemEntity item;
    private AdminItemDto adminDto;

    @BeforeEach
    void setUp() {
        polish = LanguageEntity.builder().id(1L).code("pl").name("Polish").isDefault(true).isActive(true).build();

        category = CategoryEntity.builder().id(1L).slug("whisky").isActive(true).status("published").build();
        topic = TopicEntity.builder().id(1L).slug("single-malt").category(category).isActive(true).status("published").build();
        subtopic = SubtopicEntity.builder().id(1L).slug("islay").topic(topic).isActive(true).status("published").build();

        item = ItemEntity.builder()
                .id(1L).slug("talisker-10")
                .category(category).topic(topic).subtopic(null)
                .abv(new BigDecimal("45.80")).isFeatured(false)
                .status("draft")
                .translations(new ArrayList<>())
                .images(new ArrayList<>())
                .build();

        adminDto = AdminItemDto.builder()
                .id(1L).slug("talisker-10").categoryId(1L).topicId(1L)
                .status("draft").translations(List.of())
                .build();
    }

    @Nested
    @DisplayName("createItem")
    class CreateItem {

        @Test
        @DisplayName("should create item successfully")
        void shouldCreateItem() {
            CreateItemRequest request = CreateItemRequest.builder()
                    .categoryId(1L).topicId(1L).slug("talisker-10")
                    .abv(new BigDecimal("45.80"))
                    .translations(List.of(
                            TranslationRequest.builder().languageCode("pl").title("Talisker 10").build()
                    )).build();

            when(itemRepository.existsBySlug("talisker-10")).thenReturn(false);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));
            when(languageQueryService.resolveLanguage("pl")).thenReturn(polish);
            when(itemRepository.save(any(ItemEntity.class))).thenReturn(item);
            when(itemRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(item));
            when(itemMapper.toAdminDto(any(ItemEntity.class))).thenReturn(adminDto);

            AdminItemDto result = service.createItem(request);

            assertThat(result).isNotNull();
            assertThat(result.getSlug()).isEqualTo("talisker-10");
            verify(itemRepository).save(any(ItemEntity.class));
            verify(auditService).logCreate(eq("item"), eq(1L), anyString());
        }

        @Test
        @DisplayName("should throw when slug already exists")
        void shouldThrowWhenSlugExists() {
            CreateItemRequest request = CreateItemRequest.builder()
                    .categoryId(1L).topicId(1L).slug("talisker-10")
                    .translations(List.of(TranslationRequest.builder().languageCode("pl").title("T").build()))
                    .build();

            when(itemRepository.existsBySlug("talisker-10")).thenReturn(true);

            assertThatThrownBy(() -> service.createItem(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("should throw when topic does not belong to category")
        void shouldThrowWhenTopicNotInCategory() {
            CategoryEntity otherCategory = CategoryEntity.builder().id(2L).slug("rum").build();
            TopicEntity topicInOtherCategory = TopicEntity.builder()
                    .id(2L).slug("dark-rum").category(otherCategory).build();

            CreateItemRequest request = CreateItemRequest.builder()
                    .categoryId(1L).topicId(2L).slug("test-item")
                    .translations(List.of(TranslationRequest.builder().languageCode("pl").title("T").build()))
                    .build();

            when(itemRepository.existsBySlug("test-item")).thenReturn(false);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(topicRepository.findById(2L)).thenReturn(Optional.of(topicInOtherCategory));

            assertThatThrownBy(() -> service.createItem(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("does not belong to category");
        }

        @Test
        @DisplayName("should throw when subtopic does not belong to topic")
        void shouldThrowWhenSubtopicNotInTopic() {
            TopicEntity otherTopic = TopicEntity.builder().id(2L).slug("blended").category(category).build();
            SubtopicEntity subtopicInOtherTopic = SubtopicEntity.builder()
                    .id(2L).slug("blended-region").topic(otherTopic).build();

            CreateItemRequest request = CreateItemRequest.builder()
                    .categoryId(1L).topicId(1L).subtopicId(2L).slug("test-item2")
                    .translations(List.of(TranslationRequest.builder().languageCode("pl").title("T").build()))
                    .build();

            when(itemRepository.existsBySlug("test-item2")).thenReturn(false);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));
            when(subtopicRepository.findById(2L)).thenReturn(Optional.of(subtopicInOtherTopic));

            assertThatThrownBy(() -> service.createItem(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("does not belong to topic");
        }
    }

    @Nested
    @DisplayName("publishItem")
    class PublishItem {

        @Test
        @DisplayName("should publish item with valid translations")
        void shouldPublishItem() {
            ItemTranslationEntity translation = ItemTranslationEntity.builder()
                    .id(1L).language(polish).title("Talisker 10").description("Description").build();
            item.getTranslations().add(translation);

            when(itemRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(item));
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);
            when(itemRepository.save(any(ItemEntity.class))).thenReturn(item);
            when(itemMapper.toAdminDto(any(ItemEntity.class))).thenReturn(adminDto);

            service.publishItem(1L);

            verify(auditService).logPublish("item", 1L);
        }

        @Test
        @DisplayName("should throw when already published")
        void shouldThrowWhenAlreadyPublished() {
            item.setStatus("published");
            when(itemRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(item));

            assertThatThrownBy(() -> service.publishItem(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already published");
        }

        @Test
        @DisplayName("should throw when no default-language title")
        void shouldThrowWhenNoDefaultTitle() {
            when(itemRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(item));
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            assertThatThrownBy(() -> service.publishItem(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("default language");
        }

        @Test
        @DisplayName("should throw when no description in default language")
        void shouldThrowWhenNoDescription() {
            ItemTranslationEntity translation = ItemTranslationEntity.builder()
                    .id(1L).language(polish).title("Talisker 10").description(null).build();
            item.getTranslations().add(translation);

            when(itemRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(item));
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            assertThatThrownBy(() -> service.publishItem(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("description");
        }
    }

    @Nested
    @DisplayName("deleteItem")
    class DeleteItem {

        @Test
        @DisplayName("should soft-delete item")
        void shouldSoftDelete() {
            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(itemRepository.save(any(ItemEntity.class))).thenReturn(item);

            service.deleteItem(1L);

            assertThat(item.getStatus()).isEqualTo("archived");
            verify(auditService).logDelete("item", 1L);
        }

        @Test
        @DisplayName("should throw when item not found")
        void shouldThrowWhenNotFound() {
            when(itemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteItem(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateItem")
    class UpdateItem {

        @Test
        @DisplayName("should reject duplicate slug on update")
        void shouldRejectDuplicateSlug() {
            UpdateItemRequest request = UpdateItemRequest.builder().slug("existing-slug").build();

            when(itemRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(item));
            when(itemRepository.existsBySlug("existing-slug")).thenReturn(true);

            assertThatThrownBy(() -> service.updateItem(1L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already exists");
        }
    }
}

