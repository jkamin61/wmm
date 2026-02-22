package com.org.wmm.content.topics.service;

import com.org.wmm.audit.service.AuditService;
import com.org.wmm.common.constants.StatusConstants;
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
public class TopicAdminService {

    private final TopicRepository topicRepository;
    private final CategoryRepository categoryRepository;
    private final LanguageQueryService languageQueryService;
    private final TopicMapper topicMapper;
    private final AuditService auditService;

    /**
     * Returns all topics, optionally filtered by category.
     */
    @Transactional(readOnly = true)
    public List<AdminTopicDto> getAllTopics(Long categoryId) {
        List<TopicEntity> topics;
        if (categoryId != null) {
            topics = topicRepository.findAllByCategoryIdWithTranslations(categoryId);
        } else {
            topics = topicRepository.findAllWithTranslations();
        }
        return topics.stream()
                .map(topicMapper::toAdminDto)
                .collect(Collectors.toList());
    }

    /**
     * Returns a single topic by ID.
     */
    @Transactional(readOnly = true)
    public AdminTopicDto getTopicById(Long id) {
        TopicEntity topic = topicRepository.findByIdWithTranslations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", id));
        return topicMapper.toAdminDto(topic);
    }

    /**
     * Creates a new topic.
     */
    @Transactional
    public AdminTopicDto createTopic(CreateTopicRequest request) {
        // Validate category exists
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        // Validate slug uniqueness
        if (topicRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Topic with slug '" + request.getSlug() + "' already exists");
        }

        TopicEntity topic = TopicEntity.builder()
                .category(category)
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
                TopicTranslationEntity translation = TopicTranslationEntity.builder()
                        .topic(topic)
                        .language(language)
                        .title(tr.getTitle())
                        .subtitle(tr.getSubtitle())
                        .description(tr.getDescription())
                        .metaTitle(tr.getMetaTitle())
                        .metaDescription(tr.getMetaDescription())
                        .build();
                topic.getTranslations().add(translation);
            }
        }

        TopicEntity saved = topicRepository.save(topic);
        log.info("Created topic: {} (id={}) in category {}", saved.getSlug(), saved.getId(), category.getSlug());

        auditService.logCreate("topic", saved.getId(),
                "{\"slug\":\"" + saved.getSlug() + "\",\"categoryId\":" + category.getId() + "}");

        return topicMapper.toAdminDto(
                topicRepository.findByIdWithTranslations(saved.getId()).orElse(saved));
    }

    /**
     * Updates an existing topic.
     */
    @Transactional
    public AdminTopicDto updateTopic(Long id, UpdateTopicRequest request) {
        TopicEntity topic = topicRepository.findByIdWithTranslations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", id));

        // Validate slug uniqueness if changed
        if (request.getSlug() != null && !request.getSlug().equals(topic.getSlug())) {
            if (topicRepository.existsBySlug(request.getSlug())) {
                throw new BadRequestException("Topic with slug '" + request.getSlug() + "' already exists");
            }
            topic.setSlug(request.getSlug().trim().toLowerCase());
        }

        if (request.getIcon() != null) {
            topic.setIcon(request.getIcon());
        }
        if (request.getDisplayOrder() != null) {
            topic.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            topic.setIsActive(request.getIsActive());
        }

        // Update translations
        if (request.getTranslations() != null && !request.getTranslations().isEmpty()) {
            updateTranslations(topic, request.getTranslations());
        }

        TopicEntity saved = topicRepository.save(topic);
        log.info("Updated topic: {} (id={})", saved.getSlug(), saved.getId());

        auditService.logUpdate("topic", saved.getId(), null, "{\"slug\":\"" + saved.getSlug() + "\"}");

        return topicMapper.toAdminDto(
                topicRepository.findByIdWithTranslations(saved.getId()).orElse(saved));
    }

    /**
     * Soft-deletes a topic.
     */
    @Transactional
    public void deleteTopic(Long id) {
        TopicEntity topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", id));

        topic.setIsActive(false);
        topic.setStatus(StatusConstants.ARCHIVED);
        topicRepository.save(topic);

        log.info("Soft-deleted topic: {} (id={})", topic.getSlug(), id);
        auditService.logDelete("topic", id);
    }

    /**
     * Publishes a topic.
     */
    @Transactional
    public AdminTopicDto publishTopic(Long id) {
        TopicEntity topic = topicRepository.findByIdWithTranslations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", id));

        if (StatusConstants.PUBLISHED.equals(topic.getStatus())) {
            throw new BadRequestException("Topic is already published");
        }

        validateDefaultLanguageTranslation(topic);

        topic.setStatus(StatusConstants.PUBLISHED);
        topic.setPublishedAt(OffsetDateTime.now());

        TopicEntity saved = topicRepository.save(topic);
        log.info("Published topic: {} (id={})", saved.getSlug(), id);

        auditService.logPublish("topic", id);

        return topicMapper.toAdminDto(
                topicRepository.findByIdWithTranslations(saved.getId()).orElse(saved));
    }

    /**
     * Unpublishes a topic.
     */
    @Transactional
    public AdminTopicDto unpublishTopic(Long id) {
        TopicEntity topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", id));

        if (!StatusConstants.PUBLISHED.equals(topic.getStatus())) {
            throw new BadRequestException("Topic is not currently published");
        }

        topic.setStatus(StatusConstants.DRAFT);
        topic.setPublishedAt(null);

        TopicEntity saved = topicRepository.save(topic);
        log.info("Unpublished topic: {} (id={})", saved.getSlug(), id);

        auditService.logUnpublish("topic", id);

        return topicMapper.toAdminDto(
                topicRepository.findByIdWithTranslations(saved.getId()).orElse(saved));
    }

    /**
     * Archives a topic.
     */
    @Transactional
    public AdminTopicDto archiveTopic(Long id) {
        TopicEntity topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", id));

        if (StatusConstants.ARCHIVED.equals(topic.getStatus())) {
            throw new BadRequestException("Topic is already archived");
        }

        topic.setStatus(StatusConstants.ARCHIVED);

        TopicEntity saved = topicRepository.save(topic);
        log.info("Archived topic: {} (id={})", saved.getSlug(), id);

        auditService.logArchive("topic", id);

        return topicMapper.toAdminDto(
                topicRepository.findByIdWithTranslations(saved.getId()).orElse(saved));
    }

    // ─── PRIVATE HELPERS ──────────────────────────────────────────

    private void updateTranslations(TopicEntity topic, List<TranslationRequest> translationRequests) {
        for (TranslationRequest tr : translationRequests) {
            LanguageEntity language = languageQueryService.resolveLanguage(tr.getLanguageCode());

            TopicTranslationEntity existing = topic.getTranslations().stream()
                    .filter(t -> t.getLanguage().getId().equals(language.getId()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setTitle(tr.getTitle());
                existing.setSubtitle(tr.getSubtitle());
                existing.setDescription(tr.getDescription());
                existing.setMetaTitle(tr.getMetaTitle());
                existing.setMetaDescription(tr.getMetaDescription());
            } else {
                TopicTranslationEntity newTranslation = TopicTranslationEntity.builder()
                        .topic(topic)
                        .language(language)
                        .title(tr.getTitle())
                        .subtitle(tr.getSubtitle())
                        .description(tr.getDescription())
                        .metaTitle(tr.getMetaTitle())
                        .metaDescription(tr.getMetaDescription())
                        .build();
                topic.getTranslations().add(newTranslation);
            }
        }
    }

    private void validateDefaultLanguageTranslation(TopicEntity topic) {
        Long defaultLangId = languageQueryService.getDefaultLanguageId();

        boolean hasDefaultTranslation = topic.getTranslations().stream()
                .anyMatch(t -> t.getLanguage().getId().equals(defaultLangId)
                        && t.getTitle() != null && !t.getTitle().isBlank());

        if (!hasDefaultTranslation) {
            throw new BadRequestException("Cannot publish: translation in default language with title is required");
        }
    }
}

