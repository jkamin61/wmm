package com.org.wmm.content.items.service;

import com.org.wmm.audit.service.AuditService;
import com.org.wmm.common.constants.StatusConstants;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemAdminService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final TopicRepository topicRepository;
    private final SubtopicRepository subtopicRepository;
    private final LanguageQueryService languageQueryService;
    private final ItemMapper itemMapper;
    private final AuditService auditService;

    /**
     * Returns paginated items filtered by status, topic, and/or category.
     */
    @Transactional(readOnly = true)
    public Page<AdminItemDto> getAllItems(String status, Long topicId, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ItemEntity> itemPage = itemRepository.findAllFiltered(status, topicId, categoryId, pageable);

        return itemPage.map(itemMapper::toAdminDto);
    }

    /**
     * Returns a single item by ID with full details.
     */
    @Transactional(readOnly = true)
    public AdminItemDto getItemById(Long id) {
        ItemEntity item = itemRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
        return itemMapper.toAdminDto(item);
    }

    /**
     * Creates a new item with hierarchy validation.
     */
    @Transactional
    public AdminItemDto createItem(CreateItemRequest request) {
        // Validate slug uniqueness
        if (itemRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Item with slug '" + request.getSlug() + "' already exists");
        }

        // Validate category exists
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        // Validate topic exists
        TopicEntity topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", request.getTopicId()));

        // Validate hierarchy: topic belongs to category
        if (!topic.getCategory().getId().equals(category.getId())) {
            throw new BadRequestException("Topic '" + topic.getSlug() +
                    "' does not belong to category '" + category.getSlug() + "'");
        }

        // Validate subtopic if provided
        SubtopicEntity subtopic = null;
        if (request.getSubtopicId() != null) {
            subtopic = subtopicRepository.findById(request.getSubtopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subtopic", "id", request.getSubtopicId()));

            // Validate hierarchy: subtopic belongs to topic
            if (!subtopic.getTopic().getId().equals(topic.getId())) {
                throw new BadRequestException("Subtopic '" + subtopic.getSlug() +
                        "' does not belong to topic '" + topic.getSlug() + "'");
            }
        }

        ItemEntity item = ItemEntity.builder()
                .category(category)
                .topic(topic)
                .subtopic(subtopic)
                .partnerId(request.getPartnerId())
                .slug(request.getSlug().trim().toLowerCase())
                .abv(request.getAbv())
                .vintage(request.getVintage())
                .volumeMl(request.getVolumeMl())
                .pricePln(request.getPricePln())
                .isFeatured(Boolean.TRUE.equals(request.getIsFeatured()))
                .status(StatusConstants.DRAFT)
                .build();

        // Add translations
        if (request.getTranslations() != null) {
            for (TranslationRequest tr : request.getTranslations()) {
                LanguageEntity language = languageQueryService.resolveLanguage(tr.getLanguageCode());
                ItemTranslationEntity translation = ItemTranslationEntity.builder()
                        .item(item)
                        .language(language)
                        .title(tr.getTitle())
                        .subtitle(tr.getSubtitle())
                        .excerpt(tr.getExcerpt())
                        .description(tr.getDescription())
                        .metaTitle(tr.getMetaTitle())
                        .metaDescription(tr.getMetaDescription())
                        .metaKeywords(tr.getMetaKeywords())
                        .build();
                item.getTranslations().add(translation);
            }
        }

        ItemEntity saved = itemRepository.save(item);
        log.info("Created item: {} (id={}) in {}/{}", saved.getSlug(), saved.getId(),
                category.getSlug(), topic.getSlug());

        auditService.logCreate("item", saved.getId(),
                "{\"slug\":\"" + saved.getSlug() + "\",\"categoryId\":" + category.getId() +
                        ",\"topicId\":" + topic.getId() + "}");

        return itemMapper.toAdminDto(
                itemRepository.findByIdWithDetails(saved.getId()).orElse(saved));
    }

    /**
     * Updates an existing item (partial update).
     */
    @Transactional
    public AdminItemDto updateItem(Long id, UpdateItemRequest request) {
        ItemEntity item = itemRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        // Validate slug uniqueness if changed
        if (request.getSlug() != null && !request.getSlug().equals(item.getSlug())) {
            if (itemRepository.existsBySlug(request.getSlug())) {
                throw new BadRequestException("Item with slug '" + request.getSlug() + "' already exists");
            }
            item.setSlug(request.getSlug().trim().toLowerCase());
        }

        if (request.getAbv() != null) {
            item.setAbv(request.getAbv());
        }
        if (request.getVintage() != null) {
            item.setVintage(request.getVintage());
        }
        if (request.getVolumeMl() != null) {
            item.setVolumeMl(request.getVolumeMl());
        }
        if (request.getPricePln() != null) {
            item.setPricePln(request.getPricePln());
        }
        if (request.getIsFeatured() != null) {
            item.setIsFeatured(request.getIsFeatured());
        }
        if (request.getPartnerId() != null) {
            item.setPartnerId(request.getPartnerId());
        }

        // Validate and update subtopic if provided
        if (request.getSubtopicId() != null) {
            SubtopicEntity subtopic = subtopicRepository.findById(request.getSubtopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subtopic", "id", request.getSubtopicId()));

            // Validate hierarchy: subtopic belongs to the item's topic
            if (!subtopic.getTopic().getId().equals(item.getTopic().getId())) {
                throw new BadRequestException("Subtopic '" + subtopic.getSlug() +
                        "' does not belong to topic '" + item.getTopic().getSlug() + "'");
            }
            item.setSubtopic(subtopic);
        }

        // Update translations
        if (request.getTranslations() != null && !request.getTranslations().isEmpty()) {
            updateTranslations(item, request.getTranslations());
        }

        ItemEntity saved = itemRepository.save(item);
        log.info("Updated item: {} (id={})", saved.getSlug(), saved.getId());

        auditService.logUpdate("item", saved.getId(), null, "{\"slug\":\"" + saved.getSlug() + "\"}");

        return itemMapper.toAdminDto(
                itemRepository.findByIdWithDetails(saved.getId()).orElse(saved));
    }

    /**
     * Soft-deletes an item.
     */
    @Transactional
    public void deleteItem(Long id) {
        ItemEntity item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        item.setStatus(StatusConstants.ARCHIVED);
        itemRepository.save(item);

        log.info("Soft-deleted item: {} (id={})", item.getSlug(), id);
        auditService.logDelete("item", id);
    }

    /**
     * Publishes an item. Validates:
     * - default language translation with title exists
     * - hierarchy consistency
     */
    @Transactional
    public AdminItemDto publishItem(Long id) {
        ItemEntity item = itemRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        if (StatusConstants.PUBLISHED.equals(item.getStatus())) {
            throw new BadRequestException("Item is already published");
        }

        // Validate publish conditions
        validatePublishConditions(item);

        item.setStatus(StatusConstants.PUBLISHED);
        item.setPublishedAt(OffsetDateTime.now());

        ItemEntity saved = itemRepository.save(item);
        log.info("Published item: {} (id={})", saved.getSlug(), id);

        auditService.logPublish("item", id);

        return itemMapper.toAdminDto(
                itemRepository.findByIdWithDetails(saved.getId()).orElse(saved));
    }

    /**
     * Unpublishes an item (reverts to draft).
     */
    @Transactional
    public AdminItemDto unpublishItem(Long id) {
        ItemEntity item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        if (!StatusConstants.PUBLISHED.equals(item.getStatus())) {
            throw new BadRequestException("Item is not currently published");
        }

        item.setStatus(StatusConstants.DRAFT);
        item.setPublishedAt(null);

        ItemEntity saved = itemRepository.save(item);
        log.info("Unpublished item: {} (id={})", saved.getSlug(), id);

        auditService.logUnpublish("item", id);

        return itemMapper.toAdminDto(
                itemRepository.findByIdWithDetails(saved.getId()).orElse(saved));
    }

    /**
     * Archives an item.
     */
    @Transactional
    public AdminItemDto archiveItem(Long id) {
        ItemEntity item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        if (StatusConstants.ARCHIVED.equals(item.getStatus())) {
            throw new BadRequestException("Item is already archived");
        }

        item.setStatus(StatusConstants.ARCHIVED);

        ItemEntity saved = itemRepository.save(item);
        log.info("Archived item: {} (id={})", saved.getSlug(), id);

        auditService.logArchive("item", id);

        return itemMapper.toAdminDto(
                itemRepository.findByIdWithDetails(saved.getId()).orElse(saved));
    }

    // ─── PRIVATE HELPERS ──────────────────────────────────────────

    private void updateTranslations(ItemEntity item, List<TranslationRequest> translationRequests) {
        for (TranslationRequest tr : translationRequests) {
            LanguageEntity language = languageQueryService.resolveLanguage(tr.getLanguageCode());

            ItemTranslationEntity existing = item.getTranslations().stream()
                    .filter(t -> t.getLanguage().getId().equals(language.getId()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setTitle(tr.getTitle());
                existing.setSubtitle(tr.getSubtitle());
                existing.setExcerpt(tr.getExcerpt());
                existing.setDescription(tr.getDescription());
                existing.setMetaTitle(tr.getMetaTitle());
                existing.setMetaDescription(tr.getMetaDescription());
                existing.setMetaKeywords(tr.getMetaKeywords());
            } else {
                ItemTranslationEntity newTranslation = ItemTranslationEntity.builder()
                        .item(item)
                        .language(language)
                        .title(tr.getTitle())
                        .subtitle(tr.getSubtitle())
                        .excerpt(tr.getExcerpt())
                        .description(tr.getDescription())
                        .metaTitle(tr.getMetaTitle())
                        .metaDescription(tr.getMetaDescription())
                        .metaKeywords(tr.getMetaKeywords())
                        .build();
                item.getTranslations().add(newTranslation);
            }
        }
    }

    private void validatePublishConditions(ItemEntity item) {
        Long defaultLangId = languageQueryService.getDefaultLanguageId();

        // 1. Must have translation in default language with title
        boolean hasDefaultTranslation = item.getTranslations().stream()
                .anyMatch(t -> t.getLanguage().getId().equals(defaultLangId)
                        && t.getTitle() != null && !t.getTitle().isBlank());

        if (!hasDefaultTranslation) {
            throw new BadRequestException("Cannot publish: translation in default language with title is required");
        }

        // 2. Must have at least minimal description in default language
        boolean hasDescription = item.getTranslations().stream()
                .anyMatch(t -> t.getLanguage().getId().equals(defaultLangId)
                        && t.getDescription() != null && !t.getDescription().isBlank());

        if (!hasDescription) {
            throw new BadRequestException("Cannot publish: description in default language is required");
        }
    }
}



