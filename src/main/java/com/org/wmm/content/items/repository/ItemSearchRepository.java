package com.org.wmm.content.items.repository;

import com.org.wmm.content.items.entity.ItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for public search and filtered listing of published items.
 * Uses PostgreSQL full-text search (FTS) on item_translations.
 */
@Repository
public interface ItemSearchRepository extends JpaRepository<ItemEntity, Long> {

    /**
     * Full-text search on item_translations (title + description) with comprehensive filters.
     * Uses native PostgreSQL to_tsvector / plainto_tsquery / ts_rank.
     */
    @Query(value = """
            SELECT DISTINCT i.* FROM items i
            JOIN item_translations it ON it.item_id = i.id AND it.language_id = :langId
            LEFT JOIN tasting_notes tn ON tn.item_id = i.id
            WHERE i.status = 'published'
              AND to_tsvector('simple', coalesce(it.title, '') || ' ' || coalesce(it.description, ''))
                  @@ plainto_tsquery('simple', :query)
              AND (:categoryId IS NULL OR i.category_id = :categoryId)
              AND (:topicId IS NULL OR i.topic_id = :topicId)
              AND (:subtopicId IS NULL OR i.subtopic_id = :subtopicId)
              AND (:featured IS NULL OR i.is_featured = :featured)
              AND (:scoreMin IS NULL OR tn.overall_score >= :scoreMin)
              AND (:scoreMax IS NULL OR tn.overall_score <= :scoreMax)
            ORDER BY ts_rank(
                to_tsvector('simple', coalesce(it.title, '') || ' ' || coalesce(it.description, '')),
                plainto_tsquery('simple', :query)
            ) DESC, i.published_at DESC
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT i.id) FROM items i
                    JOIN item_translations it ON it.item_id = i.id AND it.language_id = :langId
                    LEFT JOIN tasting_notes tn ON tn.item_id = i.id
                    WHERE i.status = 'published'
                      AND to_tsvector('simple', coalesce(it.title, '') || ' ' || coalesce(it.description, ''))
                          @@ plainto_tsquery('simple', :query)
                      AND (:categoryId IS NULL OR i.category_id = :categoryId)
                      AND (:topicId IS NULL OR i.topic_id = :topicId)
                      AND (:subtopicId IS NULL OR i.subtopic_id = :subtopicId)
                      AND (:featured IS NULL OR i.is_featured = :featured)
                      AND (:scoreMin IS NULL OR tn.overall_score >= :scoreMin)
                      AND (:scoreMax IS NULL OR tn.overall_score <= :scoreMax)
                    """,
            nativeQuery = true)
    Page<ItemEntity> searchFts(
            @Param("query") String query,
            @Param("langId") Long langId,
            @Param("categoryId") Long categoryId,
            @Param("topicId") Long topicId,
            @Param("subtopicId") Long subtopicId,
            @Param("featured") Boolean featured,
            @Param("scoreMin") BigDecimal scoreMin,
            @Param("scoreMax") BigDecimal scoreMax,
            Pageable pageable
    );

    /**
     * FTS + flavor filter. Matches items whose tasting note has at least one of the given flavor slugs
     * in any flavor section (aroma, taste, finish).
     */
    @Query(value = """
            SELECT DISTINCT i.* FROM items i
            JOIN item_translations it ON it.item_id = i.id AND it.language_id = :langId
            LEFT JOIN tasting_notes tn ON tn.item_id = i.id
            LEFT JOIN aroma_flavors af ON af.tasting_note_id = tn.id
            LEFT JOIN taste_flavors tf ON tf.tasting_note_id = tn.id
            LEFT JOIN finish_flavors ff ON ff.tasting_note_id = tn.id
            LEFT JOIN flavors fa ON fa.id = af.flavor_id
            LEFT JOIN flavors ft ON ft.id = tf.flavor_id
            LEFT JOIN flavors ffi ON ffi.id = ff.flavor_id
            WHERE i.status = 'published'
              AND to_tsvector('simple', coalesce(it.title, '') || ' ' || coalesce(it.description, ''))
                  @@ plainto_tsquery('simple', :query)
              AND (:categoryId IS NULL OR i.category_id = :categoryId)
              AND (:topicId IS NULL OR i.topic_id = :topicId)
              AND (:subtopicId IS NULL OR i.subtopic_id = :subtopicId)
              AND (:featured IS NULL OR i.is_featured = :featured)
              AND (:scoreMin IS NULL OR tn.overall_score >= :scoreMin)
              AND (:scoreMax IS NULL OR tn.overall_score <= :scoreMax)
              AND (fa.slug IN (:flavorSlugs) OR ft.slug IN (:flavorSlugs) OR ffi.slug IN (:flavorSlugs))
            ORDER BY ts_rank(
                to_tsvector('simple', coalesce(it.title, '') || ' ' || coalesce(it.description, '')),
                plainto_tsquery('simple', :query)
            ) DESC, i.published_at DESC
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT i.id) FROM items i
                    JOIN item_translations it ON it.item_id = i.id AND it.language_id = :langId
                    LEFT JOIN tasting_notes tn ON tn.item_id = i.id
                    LEFT JOIN aroma_flavors af ON af.tasting_note_id = tn.id
                    LEFT JOIN taste_flavors tf ON tf.tasting_note_id = tn.id
                    LEFT JOIN finish_flavors ff ON ff.tasting_note_id = tn.id
                    LEFT JOIN flavors fa ON fa.id = af.flavor_id
                    LEFT JOIN flavors ft ON ft.id = tf.flavor_id
                    LEFT JOIN flavors ffi ON ffi.id = ff.flavor_id
                    WHERE i.status = 'published'
                      AND to_tsvector('simple', coalesce(it.title, '') || ' ' || coalesce(it.description, ''))
                          @@ plainto_tsquery('simple', :query)
                      AND (:categoryId IS NULL OR i.category_id = :categoryId)
                      AND (:topicId IS NULL OR i.topic_id = :topicId)
                      AND (:subtopicId IS NULL OR i.subtopic_id = :subtopicId)
                      AND (:featured IS NULL OR i.is_featured = :featured)
                      AND (:scoreMin IS NULL OR tn.overall_score >= :scoreMin)
                      AND (:scoreMax IS NULL OR tn.overall_score <= :scoreMax)
                      AND (fa.slug IN (:flavorSlugs) OR ft.slug IN (:flavorSlugs) OR ffi.slug IN (:flavorSlugs))
                    """,
            nativeQuery = true)
    Page<ItemEntity> searchFtsWithFlavors(
            @Param("query") String query,
            @Param("langId") Long langId,
            @Param("categoryId") Long categoryId,
            @Param("topicId") Long topicId,
            @Param("subtopicId") Long subtopicId,
            @Param("featured") Boolean featured,
            @Param("scoreMin") BigDecimal scoreMin,
            @Param("scoreMax") BigDecimal scoreMax,
            @Param("flavorSlugs") List<String> flavorSlugs,
            Pageable pageable
    );

    /**
     * Browse/filter without FTS query (no search term, just filters).
     */
    @Query(value = """
            SELECT DISTINCT i.* FROM items i
            LEFT JOIN tasting_notes tn ON tn.item_id = i.id
            WHERE i.status = 'published'
              AND (:categoryId IS NULL OR i.category_id = :categoryId)
              AND (:topicId IS NULL OR i.topic_id = :topicId)
              AND (:subtopicId IS NULL OR i.subtopic_id = :subtopicId)
              AND (:featured IS NULL OR i.is_featured = :featured)
              AND (:scoreMin IS NULL OR tn.overall_score >= :scoreMin)
              AND (:scoreMax IS NULL OR tn.overall_score <= :scoreMax)
            ORDER BY i.published_at DESC
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT i.id) FROM items i
                    LEFT JOIN tasting_notes tn ON tn.item_id = i.id
                    WHERE i.status = 'published'
                      AND (:categoryId IS NULL OR i.category_id = :categoryId)
                      AND (:topicId IS NULL OR i.topic_id = :topicId)
                      AND (:subtopicId IS NULL OR i.subtopic_id = :subtopicId)
                      AND (:featured IS NULL OR i.is_featured = :featured)
                      AND (:scoreMin IS NULL OR tn.overall_score >= :scoreMin)
                      AND (:scoreMax IS NULL OR tn.overall_score <= :scoreMax)
                    """,
            nativeQuery = true)
    Page<ItemEntity> browseFiltered(
            @Param("categoryId") Long categoryId,
            @Param("topicId") Long topicId,
            @Param("subtopicId") Long subtopicId,
            @Param("featured") Boolean featured,
            @Param("scoreMin") BigDecimal scoreMin,
            @Param("scoreMax") BigDecimal scoreMax,
            Pageable pageable
    );

    /**
     * Browse with flavor filter (no FTS query).
     */
    @Query(value = """
            SELECT DISTINCT i.* FROM items i
            LEFT JOIN tasting_notes tn ON tn.item_id = i.id
            LEFT JOIN aroma_flavors af ON af.tasting_note_id = tn.id
            LEFT JOIN taste_flavors tf ON tf.tasting_note_id = tn.id
            LEFT JOIN finish_flavors ff ON ff.tasting_note_id = tn.id
            LEFT JOIN flavors fa ON fa.id = af.flavor_id
            LEFT JOIN flavors ft ON ft.id = tf.flavor_id
            LEFT JOIN flavors ffi ON ffi.id = ff.flavor_id
            WHERE i.status = 'published'
              AND (:categoryId IS NULL OR i.category_id = :categoryId)
              AND (:topicId IS NULL OR i.topic_id = :topicId)
              AND (:subtopicId IS NULL OR i.subtopic_id = :subtopicId)
              AND (:featured IS NULL OR i.is_featured = :featured)
              AND (:scoreMin IS NULL OR tn.overall_score >= :scoreMin)
              AND (:scoreMax IS NULL OR tn.overall_score <= :scoreMax)
              AND (fa.slug IN (:flavorSlugs) OR ft.slug IN (:flavorSlugs) OR ffi.slug IN (:flavorSlugs))
            ORDER BY i.published_at DESC
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT i.id) FROM items i
                    LEFT JOIN tasting_notes tn ON tn.item_id = i.id
                    LEFT JOIN aroma_flavors af ON af.tasting_note_id = tn.id
                    LEFT JOIN taste_flavors tf ON tf.tasting_note_id = tn.id
                    LEFT JOIN finish_flavors ff ON ff.tasting_note_id = tn.id
                    LEFT JOIN flavors fa ON fa.id = af.flavor_id
                    LEFT JOIN flavors ft ON ft.id = tf.flavor_id
                    LEFT JOIN flavors ffi ON ffi.id = ff.flavor_id
                    WHERE i.status = 'published'
                      AND (:categoryId IS NULL OR i.category_id = :categoryId)
                      AND (:topicId IS NULL OR i.topic_id = :topicId)
                      AND (:subtopicId IS NULL OR i.subtopic_id = :subtopicId)
                      AND (:featured IS NULL OR i.is_featured = :featured)
                      AND (:scoreMin IS NULL OR tn.overall_score >= :scoreMin)
                      AND (:scoreMax IS NULL OR tn.overall_score <= :scoreMax)
                      AND (fa.slug IN (:flavorSlugs) OR ft.slug IN (:flavorSlugs) OR ffi.slug IN (:flavorSlugs))
                    """,
            nativeQuery = true)
    Page<ItemEntity> browseFilteredWithFlavors(
            @Param("categoryId") Long categoryId,
            @Param("topicId") Long topicId,
            @Param("subtopicId") Long subtopicId,
            @Param("featured") Boolean featured,
            @Param("scoreMin") BigDecimal scoreMin,
            @Param("scoreMax") BigDecimal scoreMax,
            @Param("flavorSlugs") List<String> flavorSlugs,
            Pageable pageable
    );
}

