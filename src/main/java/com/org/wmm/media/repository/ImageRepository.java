package com.org.wmm.media.repository;

import com.org.wmm.content.items.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {

    @Query("SELECT i FROM ImageEntity i LEFT JOIN FETCH i.imageTranslations WHERE i.id = :id")
    Optional<ImageEntity> findByIdWithTranslations(@Param("id") Long id);

    @Query("SELECT i FROM ImageEntity i WHERE i.item.id = :itemId ORDER BY i.displayOrder ASC")
    List<ImageEntity> findByItemIdOrdered(@Param("itemId") Long itemId);

    @Query("SELECT i FROM ImageEntity i LEFT JOIN FETCH i.imageTranslations WHERE i.item.id = :itemId ORDER BY i.displayOrder ASC")
    List<ImageEntity> findByItemIdWithTranslations(@Param("itemId") Long itemId);

    @Query("SELECT COUNT(i) FROM ImageEntity i WHERE i.item.id = :itemId")
    int countByItemId(@Param("itemId") Long itemId);

    @Query("SELECT COALESCE(MAX(i.displayOrder), -1) FROM ImageEntity i WHERE i.item.id = :itemId")
    int findMaxDisplayOrderByItemId(@Param("itemId") Long itemId);

    /**
     * Unsets primary flag for all images of an item (used before setting a new primary).
     */
    @Modifying
    @Query("UPDATE ImageEntity i SET i.isPrimary = false WHERE i.item.id = :itemId AND i.isPrimary = true")
    void clearPrimaryForItem(@Param("itemId") Long itemId);

    @Query("SELECT i FROM ImageEntity i WHERE i.item.id = :itemId AND i.isPrimary = true")
    Optional<ImageEntity> findPrimaryByItemId(@Param("itemId") Long itemId);
}

