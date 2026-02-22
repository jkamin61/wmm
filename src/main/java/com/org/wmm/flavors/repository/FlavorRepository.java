package com.org.wmm.flavors.repository;

import com.org.wmm.flavors.entity.FlavorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlavorRepository extends JpaRepository<FlavorEntity, Long> {

    @Query("SELECT f FROM FlavorEntity f LEFT JOIN FETCH f.translations WHERE f.isActive = true ORDER BY f.displayOrder ASC")
    List<FlavorEntity> findAllActiveWithTranslations();

    @Query("SELECT f FROM FlavorEntity f LEFT JOIN FETCH f.translations WHERE f.id = :id")
    Optional<FlavorEntity> findByIdWithTranslations(@Param("id") Long id);

    boolean existsBySlug(String slug);
}

