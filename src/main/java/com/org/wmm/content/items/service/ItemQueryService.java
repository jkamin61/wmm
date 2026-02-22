package com.org.wmm.content.items.service;

import com.org.wmm.common.dto.PageResponse;
import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.content.items.dto.ItemDetailDto;
import com.org.wmm.content.items.dto.ItemSummaryDto;
import com.org.wmm.content.items.entity.ItemEntity;
import com.org.wmm.content.items.mapper.ItemMapper;
import com.org.wmm.content.items.repository.ItemRepository;
import com.org.wmm.content.topics.entity.TopicEntity;
import com.org.wmm.content.topics.repository.TopicRepository;
import com.org.wmm.languages.service.LanguageQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemQueryService {

    private final ItemRepository itemRepository;
    private final TopicRepository topicRepository;
    private final LanguageQueryService languageQueryService;
    private final ItemMapper itemMapper;

    /**
     * Returns paginated list of published items for a given topic.
     *
     * @param topicSlug    the topic slug
     * @param subtopicSlug optional subtopic slug filter (currently unused, reserved)
     * @param featured     optional featured filter
     * @param lang         language code
     * @param page         page number (0-based)
     * @param size         page size
     * @param sort         sort option: "newest", "oldest", "score"
     */
    @Transactional(readOnly = true)
    public PageResponse<ItemSummaryDto> getItemsByTopicSlug(String topicSlug, String subtopicSlug,
                                                            Boolean featured, String lang,
                                                            int page, int size, String sort) {
        TopicEntity topic = topicRepository.findBySlugAndIsActiveTrue(topicSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "slug", topicSlug));

        Long languageId = languageQueryService.resolveLanguageId(lang);
        Long defaultLangId = languageQueryService.getDefaultLanguageId();

        Pageable pageable = createPageable(page, size, sort);

        Page<ItemEntity> itemPage = itemRepository.findPublishedByTopicId(
                topic.getId(), null, featured, pageable);

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

    /**
     * Returns full detail of a published item by slug.
     */
    @Transactional(readOnly = true)
    public ItemDetailDto getItemBySlug(String slug, String lang) {
        ItemEntity item = itemRepository.findPublishedBySlugWithDetails(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "slug", slug));

        Long languageId = languageQueryService.resolveLanguageId(lang);
        Long defaultLangId = languageQueryService.getDefaultLanguageId();

        return itemMapper.toDetailDto(item, languageId, defaultLangId);
    }

    private Pageable createPageable(int page, int size, String sort) {
        Sort sortOrder;
        if ("oldest" .equalsIgnoreCase(sort)) {
            sortOrder = Sort.by(Sort.Direction.ASC, "publishedAt");
        } else if ("score" .equalsIgnoreCase(sort)) {
            sortOrder = Sort.by(Sort.Direction.DESC, "tastingNote.overallScore")
                    .and(Sort.by(Sort.Direction.DESC, "publishedAt"));
        } else {
            // default: newest
            sortOrder = Sort.by(Sort.Direction.DESC, "publishedAt");
        }
        return PageRequest.of(page, size, sortOrder);
    }
}

