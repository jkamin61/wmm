package com.org.wmm.content.subtopics.service;

import com.org.wmm.audit.service.AuditService;
import com.org.wmm.common.constants.StatusConstants;
import com.org.wmm.common.dto.TranslationRequest;
import com.org.wmm.common.error.BadRequestException;
import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.content.subtopics.dto.AdminSubtopicDto;
import com.org.wmm.content.subtopics.dto.CreateSubtopicRequest;
import com.org.wmm.content.subtopics.dto.UpdateSubtopicRequest;
import com.org.wmm.content.subtopics.entity.SubtopicEntity;
import com.org.wmm.content.subtopics.entity.SubtopicTranslationEntity;
import com.org.wmm.content.subtopics.mapper.SubtopicMapper;
import com.org.wmm.content.subtopics.repository.SubtopicRepository;
import com.org.wmm.content.topics.entity.TopicEntity;
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
public class SubtopicAdminService {

    private final SubtopicRepository subtopicRepository;
    private final TopicRepository topicRepository;
    private final LanguageQueryService languageQueryService;
    private final SubtopicMapper subtopicMapper;
    private final AuditService auditService;

    /**
     * Returns all subtopics, optionally filtered by topic.
     */
    @Transactional(readOnly = true)
    public List<AdminSubtopicDto> getAllSubtopics(Long topicId) {
        List<SubtopicEntity> subtopics;
        if (topicId != null) {
            subtopics = subtopicRepository.findAllByTopicIdWithTranslations(topicId);
        } else {
            subtopics = subtopicRepository.findAllWithTranslations();
        }
        return subtopics.stream()
                .map(subtopicMapper::toAdminDto)
                .collect(Collectors.toList());
    }

    /**
     * Returns a single subtopic by ID.
     */
    @Transactional(readOnly = true)
    public AdminSubtopicDto getSubtopicById(Long id) {
        SubtopicEntity subtopic = subtopicRepository.findByIdWithTranslations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subtopic", "id", id));
        return subtopicMapper.toAdminDto(subtopic);
    }

    /**
     * Creates a new subtopic.
     */
    @Transactional
    public AdminSubtopicDto createSubtopic(CreateSubtopicRequest request) {
        // Validate topic exists
        TopicEntity topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", request.getTopicId()));

        // Validate slug uniqueness
        if (subtopicRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Subtopic with slug '" + request.getSlug() + "' already exists");
        }

        SubtopicEntity subtopic = SubtopicEntity.builder()
                .topic(topic)
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
                SubtopicTranslationEntity translation = SubtopicTranslationEntity.builder()
                        .subtopic(subtopic)
                        .language(language)
                        .title(tr.getTitle())
                        .subtitle(tr.getSubtitle())
                        .description(tr.getDescription())
                        .metaTitle(tr.getMetaTitle())
                        .metaDescription(tr.getMetaDescription())
                        .build();
                subtopic.getTranslations().add(translation);
            }
        }

        SubtopicEntity saved = subtopicRepository.save(subtopic);
        log.info("Created subtopic: {} (id={}) in topic {}", saved.getSlug(), saved.getId(), topic.getSlug());

        auditService.logCreate("subtopic", saved.getId(),
                "{\"slug\":\"" + saved.getSlug() + "\",\"topicId\":" + topic.getId() + "}");

        return subtopicMapper.toAdminDto(
                subtopicRepository.findByIdWithTranslations(saved.getId()).orElse(saved));
    }

    /**
     * Updates an existing subtopic.
     */
    @Transactional
    public AdminSubtopicDto updateSubtopic(Long id, UpdateSubtopicRequest request) {
        SubtopicEntity subtopic = subtopicRepository.findByIdWithTranslations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subtopic", "id", id));

        // Validate slug uniqueness if changed
        if (request.getSlug() != null && !request.getSlug().equals(subtopic.getSlug())) {
            if (subtopicRepository.existsBySlug(request.getSlug())) {
                throw new BadRequestException("Subtopic with slug '" + request.getSlug() + "' already exists");
            }
            subtopic.setSlug(request.getSlug().trim().toLowerCase());
        }

        if (request.getIcon() != null) {
            subtopic.setIcon(request.getIcon());
        }
        if (request.getDisplayOrder() != null) {
            subtopic.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            subtopic.setIsActive(request.getIsActive());
        }

        // Update translations
        if (request.getTranslations() != null && !request.getTranslations().isEmpty()) {
            updateTranslations(subtopic, request.getTranslations());
        }

        SubtopicEntity saved = subtopicRepository.save(subtopic);
        log.info("Updated subtopic: {} (id={})", saved.getSlug(), saved.getId());

        auditService.logUpdate("subtopic", saved.getId(), null, "{\"slug\":\"" + saved.getSlug() + "\"}");

        return subtopicMapper.toAdminDto(
                subtopicRepository.findByIdWithTranslations(saved.getId()).orElse(saved));
    }

    /**
     * Soft-deletes a subtopic.
     */
    @Transactional
    public void deleteSubtopic(Long id) {
        SubtopicEntity subtopic = subtopicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subtopic", "id", id));

        subtopic.setIsActive(false);
        subtopic.setStatus(StatusConstants.ARCHIVED);
        subtopicRepository.save(subtopic);

        log.info("Soft-deleted subtopic: {} (id={})", subtopic.getSlug(), id);
        auditService.logDelete("subtopic", id);
    }

    /**
     * Publishes a subtopic.
     */
    @Transactional
    public AdminSubtopicDto publishSubtopic(Long id) {
        SubtopicEntity subtopic = subtopicRepository.findByIdWithTranslations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subtopic", "id", id));

        if (StatusConstants.PUBLISHED.equals(subtopic.getStatus())) {
            throw new BadRequestException("Subtopic is already published");
        }

        validateDefaultLanguageTranslation(subtopic);

        subtopic.setStatus(StatusConstants.PUBLISHED);
        subtopic.setPublishedAt(OffsetDateTime.now());

        SubtopicEntity saved = subtopicRepository.save(subtopic);
        log.info("Published subtopic: {} (id={})", saved.getSlug(), id);

        auditService.logPublish("subtopic", id);

        return subtopicMapper.toAdminDto(
                subtopicRepository.findByIdWithTranslations(saved.getId()).orElse(saved));
    }

    /**
     * Unpublishes a subtopic.
     */
    @Transactional
    public AdminSubtopicDto unpublishSubtopic(Long id) {
        SubtopicEntity subtopic = subtopicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subtopic", "id", id));

        if (!StatusConstants.PUBLISHED.equals(subtopic.getStatus())) {
            throw new BadRequestException("Subtopic is not currently published");
        }

        subtopic.setStatus(StatusConstants.DRAFT);
        subtopic.setPublishedAt(null);

        SubtopicEntity saved = subtopicRepository.save(subtopic);
        log.info("Unpublished subtopic: {} (id={})", saved.getSlug(), id);

        auditService.logUnpublish("subtopic", id);

        return subtopicMapper.toAdminDto(
                subtopicRepository.findByIdWithTranslations(saved.getId()).orElse(saved));
    }

    /**
     * Archives a subtopic.
     */
    @Transactional
    public AdminSubtopicDto archiveSubtopic(Long id) {
        SubtopicEntity subtopic = subtopicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subtopic", "id", id));

        if (StatusConstants.ARCHIVED.equals(subtopic.getStatus())) {
            throw new BadRequestException("Subtopic is already archived");
        }

        subtopic.setStatus(StatusConstants.ARCHIVED);

        SubtopicEntity saved = subtopicRepository.save(subtopic);
        log.info("Archived subtopic: {} (id={})", saved.getSlug(), id);

        auditService.logArchive("subtopic", id);

        return subtopicMapper.toAdminDto(
                subtopicRepository.findByIdWithTranslations(saved.getId()).orElse(saved));
    }

    // ─── PRIVATE HELPERS ──────────────────────────────────────────

    private void updateTranslations(SubtopicEntity subtopic, List<TranslationRequest> translationRequests) {
        for (TranslationRequest tr : translationRequests) {
            LanguageEntity language = languageQueryService.resolveLanguage(tr.getLanguageCode());

            SubtopicTranslationEntity existing = subtopic.getTranslations().stream()
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
                SubtopicTranslationEntity newTranslation = SubtopicTranslationEntity.builder()
                        .subtopic(subtopic)
                        .language(language)
                        .title(tr.getTitle())
                        .subtitle(tr.getSubtitle())
                        .description(tr.getDescription())
                        .metaTitle(tr.getMetaTitle())
                        .metaDescription(tr.getMetaDescription())
                        .build();
                subtopic.getTranslations().add(newTranslation);
            }
        }
    }

    private void validateDefaultLanguageTranslation(SubtopicEntity subtopic) {
        Long defaultLangId = languageQueryService.getDefaultLanguageId();

        boolean hasDefaultTranslation = subtopic.getTranslations().stream()
                .anyMatch(t -> t.getLanguage().getId().equals(defaultLangId)
                        && t.getTitle() != null && !t.getTitle().isBlank());

        if (!hasDefaultTranslation) {
            throw new BadRequestException("Cannot publish: translation in default language with title is required");
        }
    }
}

