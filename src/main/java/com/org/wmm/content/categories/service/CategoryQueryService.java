package com.org.wmm.content.categories.service;

import com.org.wmm.content.categories.dto.CategoryMenuDto;
import com.org.wmm.content.categories.entity.CategoryEntity;
import com.org.wmm.content.categories.mapper.CategoryMapper;
import com.org.wmm.content.categories.repository.CategoryRepository;
import com.org.wmm.content.topics.entity.TopicEntity;
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
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;
    private final TopicRepository topicRepository;
    private final LanguageQueryService languageQueryService;
    private final CategoryMapper categoryMapper;

    /**
     * Returns the full menu: active published categories with their active published topics,
     * translated into the requested language (with fallback to default).
     */
    @Transactional(readOnly = true)
    public List<CategoryMenuDto> getMenu(String lang) {
        Long languageId = languageQueryService.resolveLanguageId(lang);
        Long defaultLangId = languageQueryService.getDefaultLanguageId();

        List<CategoryEntity> categories = categoryRepository.findActivePublishedWithTranslations();

        return categories.stream()
                .map(category -> {
                    List<TopicEntity> topics = topicRepository
                            .findActivePublishedByCategoryIdWithTranslations(category.getId());
                    return categoryMapper.toMenuDto(category, languageId, defaultLangId, topics);
                })
                .collect(Collectors.toList());
    }
}

