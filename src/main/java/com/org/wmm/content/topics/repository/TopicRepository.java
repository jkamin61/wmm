package com.org.wmm.content.topics.repository;

import com.org.wmm.content.topics.entity.TopicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<TopicEntity, Long> {

    @Query("SELECT DISTINCT t FROM TopicEntity t " +
            "LEFT JOIN FETCH t.translations tr " +
            "WHERE t.category.id = :categoryId AND t.isActive = true AND t.status = 'published' " +
            "ORDER BY t.displayOrder ASC")
    List<TopicEntity> findActivePublishedByCategoryIdWithTranslations(@Param("categoryId") Long categoryId);

    @Query("SELECT t FROM TopicEntity t " +
            "LEFT JOIN FETCH t.translations tr " +
            "WHERE t.slug = :slug AND t.isActive = true")
    Optional<TopicEntity> findBySlugActiveWithTranslations(@Param("slug") String slug);

    Optional<TopicEntity> findBySlugAndIsActiveTrue(String slug);
}

