package com.org.wmm.content.items.service;

import com.org.wmm.common.dto.PageResponse;
import com.org.wmm.content.items.dto.ItemSummaryDto;
import com.org.wmm.content.items.entity.ItemEntity;
import com.org.wmm.content.items.mapper.ItemMapper;
import com.org.wmm.content.items.repository.ItemSearchRepository;
import com.org.wmm.languages.service.LanguageQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for public full-text search and filtered browsing of published items.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemSearchService {

    private final ItemSearchRepository searchRepository;
    private final LanguageQueryService languageQueryService;
    private final ItemMapper itemMapper;

    /**
     * Search published items using PostgreSQL full-text search with optional filters.
     *
     * @param query       search term (FTS); if blank → browse/filter mode only
     * @param lang        language code for translations
     * @param categoryId  optional category filter
     * @param topicId     optional topic filter
     * @param subtopicId  optional subtopic filter
     * @param featured    optional featured filter
     * @param scoreMin    optional minimum overall score
     * @param scoreMax    optional maximum overall score
     * @param flavorSlugs optional flavor slugs filter (matches in aroma, taste, or finish)
     * @param page        page number (0-based)
     * @param size        page size
     */
    @Transactional(readOnly = true)
    public PageResponse<ItemSummaryDto> search(String query, String lang,
                                               Long categoryId, Long topicId, Long subtopicId,
                                               Boolean featured,
                                               BigDecimal scoreMin, BigDecimal scoreMax,
                                               List<String> flavorSlugs,
                                               int page, int size) {

        Long languageId = languageQueryService.resolveLanguageId(lang);
        Long defaultLangId = languageQueryService.getDefaultLanguageId();

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));

        boolean hasQuery = query != null && !query.isBlank();
        boolean hasFlavors = flavorSlugs != null && !flavorSlugs.isEmpty();

        Page<ItemEntity> itemPage;

        if (hasQuery && hasFlavors) {
            itemPage = searchRepository.searchFtsWithFlavors(
                    query.trim(), languageId,
                    categoryId, topicId, subtopicId, featured,
                    scoreMin, scoreMax, flavorSlugs, pageable);
        } else if (hasQuery) {
            itemPage = searchRepository.searchFts(
                    query.trim(), languageId,
                    categoryId, topicId, subtopicId, featured,
                    scoreMin, scoreMax, pageable);
        } else if (hasFlavors) {
            itemPage = searchRepository.browseFilteredWithFlavors(
                    categoryId, topicId, subtopicId, featured,
                    scoreMin, scoreMax, flavorSlugs, pageable);
        } else {
            itemPage = searchRepository.browseFiltered(
                    categoryId, topicId, subtopicId, featured,
                    scoreMin, scoreMax, pageable);
        }

        log.debug("Search q='{}' lang={} → {} results (page {}/{})",
                query, lang, itemPage.getTotalElements(), page, itemPage.getTotalPages());

        List<ItemSummaryDto> content = itemPage.getContent().stream()
                .map(item -> itemMapper.toSummaryDto(item, languageId, defaultLangId))
                .collect(Collectors.toList());

        return PageResponse.<ItemSummaryDto>builder()
                .content(content)
                .page(itemPage.getNumber())
                .size(itemPage.getSize())
                .totalElements(itemPage.getTotalElements())
                .totalPages(itemPage.getTotalPages())
                .first(itemPage.isFirst())
                .last(itemPage.isLast())
                .build();
    }
}

