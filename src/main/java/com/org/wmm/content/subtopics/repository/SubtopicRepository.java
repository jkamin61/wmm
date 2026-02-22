package com.org.wmm.content.subtopics.repository;

import com.org.wmm.content.subtopics.entity.SubtopicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubtopicRepository extends JpaRepository<SubtopicEntity, Long> {

    boolean existsBySlug(String slug);

    boolean existsByTopicIdAndSlug(Long topicId, String slug);

    @Query("SELECT s FROM SubtopicEntity s LEFT JOIN FETCH s.translations WHERE s.id = :id")
    Optional<SubtopicEntity> findByIdWithTranslations(@Param("id") Long id);

    @Query("SELECT DISTINCT s FROM SubtopicEntity s LEFT JOIN FETCH s.translations " +
            "WHERE s.topic.id = :topicId ORDER BY s.displayOrder ASC")
    List<SubtopicEntity> findAllByTopicIdWithTranslations(@Param("topicId") Long topicId);

    @Query("SELECT DISTINCT s FROM SubtopicEntity s LEFT JOIN FETCH s.translations ORDER BY s.displayOrder ASC")
    List<SubtopicEntity> findAllWithTranslations();

    Optional<SubtopicEntity> findBySlugAndIsActiveTrue(String slug);
}

