package com.org.wmm.content.categories.service;

import com.org.wmm.audit.service.AuditService;
import com.org.wmm.common.constants.StatusConstants;
import com.org.wmm.common.error.BadRequestException;
import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.content.categories.dto.AdminCategoryDto;
import com.org.wmm.content.categories.dto.CreateCategoryRequest;
import com.org.wmm.content.categories.dto.UpdateCategoryRequest;
import com.org.wmm.content.categories.entity.CategoryEntity;
import com.org.wmm.content.categories.entity.CategoryTranslationEntity;
import com.org.wmm.content.categories.mapper.CategoryMapper;
import com.org.wmm.content.categories.repository.CategoryRepository;
import com.org.wmm.common.dto.TranslationRequest;
import com.org.wmm.languages.entity.LanguageEntity;
import com.org.wmm.languages.service.LanguageQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryAdminService {

    private final CategoryRepository categoryRepository;
    private final LanguageQueryService languageQueryService;
    private final CategoryMapper categoryMapper;
    private final AuditService auditService;

    /**
     * Returns all categories with translations for admin (all statuses).
     */
    @Transactional(readOnly = true)
    public List<AdminCategoryDto> getAllCategories() {
        return categoryRepository.findAllWithTranslations().stream()
                .map(categoryMapper::toAdminDto)
                .collect(Collectors.toList());
    }

    /**
     * Returns a single category by ID with all translations.
     */
    @Transactional(readOnly = true)
    public AdminCategoryDto getCategoryById(Long id) {
        CategoryEntity category = categoryRepository.findByIdWithTranslations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return categoryMapper.toAdminDto(category);
    }

    /**
     * Creates a new category with translations.
     */
    @Transactional
    public AdminCategoryDto createCategory(CreateCategoryRequest request) {
        // Validate slug uniqueness
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Category with slug '" + request.getSlug() + "' already exists");
        }

        CategoryEntity category = CategoryEntity.builder()
                .slug(request.getSlug().trim().toLowerCase())
                .icon(request.getIcon())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(true)
                .status(StatusConstants.DRAFT)
                .build();

        // Add translations
        if (request.getTranslations() != null) {
            for (TranslationRequest tr : request.getTranslations()) {
                LanguageEntity language = languageQueryService.resolveLanguage(tr.getLanguageCode());
                CategoryTranslationEntity translation = CategoryTranslationEntity.builder()
                        .category(category)
                        .language(language)
                        .title(tr.getTitle())
                        .description(tr.getDescription())
                        .metaTitle(tr.getMetaTitle())
                        .metaDescription(tr.getMetaDescription())
                        .build();
                category.getTranslations().add(translation);
            }
        }

        CategoryEntity saved = categoryRepository.save(category);
        log.info("Created category: {} (id={})", saved.getSlug(), saved.getId());

        auditService.logCreate("category", saved.getId(), "{\"slug\":\"" + saved.getSlug() + "\"}");

        return categoryMapper.toAdminDto(
                categoryRepository.findByIdWithTranslations(saved.getId()).orElse(saved));
    }

    /**
     * Updates an existing category (partial update).
     */
    @Transactional
    public AdminCategoryDto updateCategory(Long id, UpdateCategoryRequest request) {
        CategoryEntity category = categoryRepository.findByIdWithTranslations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Validate slug uniqueness if changed
        if (request.getSlug() != null && !request.getSlug().equals(category.getSlug())) {
            if (categoryRepository.existsBySlug(request.getSlug())) {
                throw new BadRequestException("Category with slug '" + request.getSlug() + "' already exists");
            }
            category.setSlug(request.getSlug().trim().toLowerCase());
        }

        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        // Update translations
        if (request.getTranslations() != null && !request.getTranslations().isEmpty()) {
            updateTranslations(category, request.getTranslations());
        }

        CategoryEntity saved = categoryRepository.save(category);
        log.info("Updated category: {} (id={})", saved.getSlug(), saved.getId());

        auditService.logUpdate("category", saved.getId(), null, "{\"slug\":\"" + saved.getSlug() + "\"}");

        return categoryMapper.toAdminDto(
                categoryRepository.findByIdWithTranslations(saved.getId()).orElse(saved));
    }

    /**
     * Soft-deletes a category (sets isActive = false).
     */
    @Transactional
    public void deleteCategory(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.setIsActive(false);
        category.setStatus(StatusConstants.ARCHIVED);
        categoryRepository.save(category);

        log.info("Soft-deleted category: {} (id={})", category.getSlug(), id);
        auditService.logDelete("category", id);
    }

    /**
     * Publishes a category. Requires at least a default-language translation with title.
     */
    @Transactional
    public AdminCategoryDto publishCategory(Long id) {
        CategoryEntity category = categoryRepository.findByIdWithTranslations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (StatusConstants.PUBLISHED.equals(category.getStatus())) {
            throw new BadRequestException("Category is already published");
        }

        // Validate: must have translation in default language
        validateDefaultLanguageTranslation(category);

        category.setStatus(StatusConstants.PUBLISHED);
        category.setPublishedAt(OffsetDateTime.now());

        CategoryEntity saved = categoryRepository.save(category);
        log.info("Published category: {} (id={})", saved.getSlug(), id);

        auditService.logPublish("category", id);

        return categoryMapper.toAdminDto(
                categoryRepository.findByIdWithTranslations(saved.getId()).orElse(saved));
    }

    /**
     * Unpublishes a category (reverts to draft).
     */
    @Transactional
    public AdminCategoryDto unpublishCategory(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (!StatusConstants.PUBLISHED.equals(category.getStatus())) {
            throw new BadRequestException("Category is not currently published");
        }

        category.setStatus(StatusConstants.DRAFT);
        category.setPublishedAt(null);

        CategoryEntity saved = categoryRepository.save(category);
        log.info("Unpublished category: {} (id={})", saved.getSlug(), id);

        auditService.logUnpublish("category", id);

        return categoryMapper.toAdminDto(
                categoryRepository.findByIdWithTranslations(saved.getId()).orElse(saved));
    }

    /**
     * Archives a category.
     */
    @Transactional
    public AdminCategoryDto archiveCategory(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (StatusConstants.ARCHIVED.equals(category.getStatus())) {
            throw new BadRequestException("Category is already archived");
        }

        category.setStatus(StatusConstants.ARCHIVED);

        CategoryEntity saved = categoryRepository.save(category);
        log.info("Archived category: {} (id={})", saved.getSlug(), id);

        auditService.logArchive("category", id);

        return categoryMapper.toAdminDto(
                categoryRepository.findByIdWithTranslations(saved.getId()).orElse(saved));
    }

    // ─── PRIVATE HELPERS ──────────────────────────────────────────

    private void updateTranslations(CategoryEntity category, List<TranslationRequest> translationRequests) {
        for (TranslationRequest tr : translationRequests) {
            LanguageEntity language = languageQueryService.resolveLanguage(tr.getLanguageCode());

            // Find existing translation for this language
            CategoryTranslationEntity existing = category.getTranslations().stream()
                    .filter(t -> t.getLanguage().getId().equals(language.getId()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setTitle(tr.getTitle());
                existing.setDescription(tr.getDescription());
                existing.setMetaTitle(tr.getMetaTitle());
                existing.setMetaDescription(tr.getMetaDescription());
            } else {
                CategoryTranslationEntity newTranslation = CategoryTranslationEntity.builder()
                        .category(category)
                        .language(language)
                        .title(tr.getTitle())
                        .description(tr.getDescription())
                        .metaTitle(tr.getMetaTitle())
                        .metaDescription(tr.getMetaDescription())
                        .build();
                category.getTranslations().add(newTranslation);
            }
        }
    }

    private void validateDefaultLanguageTranslation(CategoryEntity category) {
        Long defaultLangId = languageQueryService.getDefaultLanguageId();

        boolean hasDefaultTranslation = category.getTranslations().stream()
                .anyMatch(t -> t.getLanguage().getId().equals(defaultLangId)
                        && t.getTitle() != null && !t.getTitle().isBlank());

        if (!hasDefaultTranslation) {
            throw new BadRequestException("Cannot publish: translation in default language with title is required");
        }
    }
}

