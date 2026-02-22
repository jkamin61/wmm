package com.org.wmm.content.categories.repository;

import com.org.wmm.content.categories.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    @Query("SELECT DISTINCT c FROM CategoryEntity c " +
            "LEFT JOIN FETCH c.translations t " +
            "WHERE c.isActive = true AND c.status = 'published' " +
            "ORDER BY c.displayOrder ASC")
    List<CategoryEntity> findActivePublishedWithTranslations();

    Optional<CategoryEntity> findBySlugAndIsActiveTrue(String slug);

    Optional<CategoryEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT c FROM CategoryEntity c LEFT JOIN FETCH c.translations WHERE c.id = :id")
    Optional<CategoryEntity> findByIdWithTranslations(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM CategoryEntity c LEFT JOIN FETCH c.translations ORDER BY c.displayOrder ASC")
    List<CategoryEntity> findAllWithTranslations();
}

