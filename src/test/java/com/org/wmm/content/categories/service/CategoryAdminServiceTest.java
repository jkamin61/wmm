package com.org.wmm.content.categories.service;

import com.org.wmm.audit.service.AuditService;
import com.org.wmm.common.dto.TranslationRequest;
import com.org.wmm.common.error.BadRequestException;
import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.content.categories.dto.AdminCategoryDto;
import com.org.wmm.content.categories.dto.CreateCategoryRequest;
import com.org.wmm.content.categories.dto.UpdateCategoryRequest;
import com.org.wmm.content.categories.entity.CategoryEntity;
import com.org.wmm.content.categories.entity.CategoryTranslationEntity;
import com.org.wmm.content.categories.mapper.CategoryMapper;
import com.org.wmm.content.categories.repository.CategoryRepository;
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
class CategoryAdminServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private LanguageQueryService languageQueryService;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private CategoryAdminService service;

    private LanguageEntity polish;
    private CategoryEntity whiskyCategory;
    private AdminCategoryDto adminDto;

    @BeforeEach
    void setUp() {
        polish = LanguageEntity.builder().id(1L).code("pl").name("Polish").isDefault(true).isActive(true).build();

        whiskyCategory = CategoryEntity.builder()
                .id(1L).slug("whisky").icon("whiskey-glass")
                .displayOrder(1).isActive(true).status("draft")
                .translations(new ArrayList<>())
                .build();

        adminDto = AdminCategoryDto.builder()
                .id(1L).slug("whisky").icon("whiskey-glass")
                .displayOrder(1).isActive(true).status("draft")
                .translations(List.of())
                .build();
    }

    @Nested
    @DisplayName("createCategory")
    class CreateCategory {

        @Test
        @DisplayName("should create category successfully")
        void shouldCreateCategory() {
            CreateCategoryRequest request = CreateCategoryRequest.builder()
                    .slug("whisky").icon("whiskey-glass").displayOrder(1)
                    .translations(List.of(
                            TranslationRequest.builder().languageCode("pl").title("Whisky").build()
                    )).build();

            when(categoryRepository.existsBySlug("whisky")).thenReturn(false);
            when(languageQueryService.resolveLanguage("pl")).thenReturn(polish);
            when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(whiskyCategory);
            when(categoryRepository.findByIdWithTranslations(1L)).thenReturn(Optional.of(whiskyCategory));
            when(categoryMapper.toAdminDto(any(CategoryEntity.class))).thenReturn(adminDto);

            AdminCategoryDto result = service.createCategory(request);

            assertThat(result).isNotNull();
            assertThat(result.getSlug()).isEqualTo("whisky");
            verify(categoryRepository).save(any(CategoryEntity.class));
            verify(auditService).logCreate(eq("category"), eq(1L), anyString());
        }

        @Test
        @DisplayName("should throw when slug already exists")
        void shouldThrowWhenSlugExists() {
            CreateCategoryRequest request = CreateCategoryRequest.builder()
                    .slug("whisky")
                    .translations(List.of(
                            TranslationRequest.builder().languageCode("pl").title("Whisky").build()
                    )).build();

            when(categoryRepository.existsBySlug("whisky")).thenReturn(true);

            assertThatThrownBy(() -> service.createCategory(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("updateCategory")
    class UpdateCategory {

        @Test
        @DisplayName("should update category fields")
        void shouldUpdateCategory() {
            UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                    .icon("new-icon").displayOrder(5).build();

            when(categoryRepository.findByIdWithTranslations(1L)).thenReturn(Optional.of(whiskyCategory));
            when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(whiskyCategory);
            when(categoryMapper.toAdminDto(any(CategoryEntity.class))).thenReturn(adminDto);

            AdminCategoryDto result = service.updateCategory(1L, request);

            assertThat(result).isNotNull();
            verify(categoryRepository).save(any(CategoryEntity.class));
            verify(auditService).logUpdate(eq("category"), eq(1L), isNull(), anyString());
        }

        @Test
        @DisplayName("should throw when category not found")
        void shouldThrowWhenNotFound() {
            when(categoryRepository.findByIdWithTranslations(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateCategory(99L, new UpdateCategoryRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should reject duplicate slug on update")
        void shouldRejectDuplicateSlug() {
            UpdateCategoryRequest request = UpdateCategoryRequest.builder().slug("existing-slug").build();

            when(categoryRepository.findByIdWithTranslations(1L)).thenReturn(Optional.of(whiskyCategory));
            when(categoryRepository.existsBySlug("existing-slug")).thenReturn(true);

            assertThatThrownBy(() -> service.updateCategory(1L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("publishCategory")
    class PublishCategory {

        @Test
        @DisplayName("should publish category with default translation")
        void shouldPublishCategory() {
            CategoryTranslationEntity translation = CategoryTranslationEntity.builder()
                    .id(1L).language(polish).title("Whisky").build();
            whiskyCategory.getTranslations().add(translation);

            when(categoryRepository.findByIdWithTranslations(1L)).thenReturn(Optional.of(whiskyCategory));
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);
            when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(whiskyCategory);
            when(categoryMapper.toAdminDto(any(CategoryEntity.class))).thenReturn(adminDto);

            AdminCategoryDto result = service.publishCategory(1L);

            assertThat(result).isNotNull();
            verify(auditService).logPublish("category", 1L);
        }

        @Test
        @DisplayName("should throw when already published")
        void shouldThrowWhenAlreadyPublished() {
            whiskyCategory.setStatus("published");

            when(categoryRepository.findByIdWithTranslations(1L)).thenReturn(Optional.of(whiskyCategory));

            assertThatThrownBy(() -> service.publishCategory(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already published");
        }

        @Test
        @DisplayName("should throw when no default language translation")
        void shouldThrowWhenNoDefaultTranslation() {
            when(categoryRepository.findByIdWithTranslations(1L)).thenReturn(Optional.of(whiskyCategory));
            when(languageQueryService.getDefaultLanguageId()).thenReturn(1L);

            assertThatThrownBy(() -> service.publishCategory(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("default language");
        }
    }

    @Nested
    @DisplayName("deleteCategory")
    class DeleteCategory {

        @Test
        @DisplayName("should soft-delete category")
        void shouldSoftDelete() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(whiskyCategory));
            when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(whiskyCategory);

            service.deleteCategory(1L);

            assertThat(whiskyCategory.getIsActive()).isFalse();
            assertThat(whiskyCategory.getStatus()).isEqualTo("archived");
            verify(auditService).logDelete("category", 1L);
        }
    }

    @Nested
    @DisplayName("archiveCategory")
    class ArchiveCategory {

        @Test
        @DisplayName("should archive category")
        void shouldArchive() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(whiskyCategory));
            when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(whiskyCategory);
            when(categoryRepository.findByIdWithTranslations(1L)).thenReturn(Optional.of(whiskyCategory));
            when(categoryMapper.toAdminDto(any(CategoryEntity.class))).thenReturn(adminDto);

            service.archiveCategory(1L);

            verify(auditService).logArchive("category", 1L);
        }

        @Test
        @DisplayName("should throw when already archived")
        void shouldThrowWhenAlreadyArchived() {
            whiskyCategory.setStatus("archived");
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(whiskyCategory));

            assertThatThrownBy(() -> service.archiveCategory(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already archived");
        }
    }
}

