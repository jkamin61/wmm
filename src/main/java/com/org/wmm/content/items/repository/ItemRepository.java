package com.org.wmm.content.items.repository;

import com.org.wmm.content.items.entity.ItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    @Query("SELECT i FROM ItemEntity i " +
            "WHERE i.topic.id = :topicId AND i.status = 'published' " +
            "AND (:subtopicId IS NULL OR i.subtopic.id = :subtopicId) " +
            "AND (:featured IS NULL OR i.isFeatured = :featured)")
    Page<ItemEntity> findPublishedByTopicId(
            @Param("topicId") Long topicId,
            @Param("subtopicId") Long subtopicId,
            @Param("featured") Boolean featured,
            Pageable pageable
    );

    @Query("SELECT i FROM ItemEntity i " +
            "LEFT JOIN FETCH i.translations " +
            "LEFT JOIN FETCH i.images " +
            "LEFT JOIN FETCH i.tastingNote tn " +
            "LEFT JOIN FETCH tn.translations " +
            "LEFT JOIN FETCH i.category " +
            "LEFT JOIN FETCH i.topic " +
            "LEFT JOIN FETCH i.subtopic " +
            "WHERE i.slug = :slug AND i.status = 'published'")
    Optional<ItemEntity> findPublishedBySlugWithDetails(@Param("slug") String slug);

    @Query("SELECT i FROM ItemEntity i " +
            "LEFT JOIN FETCH i.translations " +
            "WHERE i.slug = :slug AND i.status = 'published'")
    Optional<ItemEntity> findPublishedBySlug(@Param("slug") String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT i FROM ItemEntity i " +
            "LEFT JOIN FETCH i.translations " +
            "LEFT JOIN FETCH i.images " +
            "LEFT JOIN FETCH i.category " +
            "LEFT JOIN FETCH i.topic " +
            "LEFT JOIN FETCH i.subtopic " +
            "WHERE i.id = :id")
    Optional<ItemEntity> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT i FROM ItemEntity i " +
            "WHERE (:status IS NULL OR i.status = :status) " +
            "AND (:topicId IS NULL OR i.topic.id = :topicId) " +
            "AND (:categoryId IS NULL OR i.category.id = :categoryId)")
    Page<ItemEntity> findAllFiltered(
            @Param("status") String status,
            @Param("topicId") Long topicId,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );
}

