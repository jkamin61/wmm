package com.org.wmm.content.topics.service;

import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.content.categories.entity.CategoryEntity;
import com.org.wmm.content.categories.repository.CategoryRepository;
import com.org.wmm.content.topics.dto.TopicDto;
import com.org.wmm.content.topics.entity.TopicEntity;
import com.org.wmm.content.topics.mapper.TopicMapper;
import com.org.wmm.content.topics.repository.TopicRepository;
import com.org.wmm.languages.service.LanguageQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicQueryService {

    private final TopicRepository topicRepository;
    private final CategoryRepository categoryRepository;
    private final LanguageQueryService languageQueryService;
    private final TopicMapper topicMapper;

    /**
     * Returns active published topics for a given category slug.
     */
    @Transactional(readOnly = true)
    public List<TopicDto> getTopicsByCategorySlug(String categorySlug, String lang) {
        CategoryEntity category = categoryRepository.findBySlugAndIsActiveTrue(categorySlug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", categorySlug));

        Long languageId = languageQueryService.resolveLanguageId(lang);
        Long defaultLangId = languageQueryService.getDefaultLanguageId();

        List<TopicEntity> topics = topicRepository
                .findActivePublishedByCategoryIdWithTranslations(category.getId());

        return topics.stream()
                .map(t -> topicMapper.toDto(t, languageId, defaultLangId))
                .collect(Collectors.toList());
    }
}

