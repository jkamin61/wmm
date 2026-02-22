package com.org.wmm.tasting.repository;

import com.org.wmm.content.items.entity.TastingNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TastingNoteRepository extends JpaRepository<TastingNoteEntity, Long> {

    @Query("SELECT tn FROM TastingNoteEntity tn " +
            "LEFT JOIN FETCH tn.translations " +
            "LEFT JOIN FETCH tn.aromaFlavors af LEFT JOIN FETCH af.flavor " +
            "WHERE tn.item.id = :itemId")
    Optional<TastingNoteEntity> findByItemIdWithAroma(@Param("itemId") Long itemId);

    /**
     * Full fetch with all three flavor lists — call separately due to Hibernate bag issue.
     */
    @Query("SELECT tn FROM TastingNoteEntity tn " +
            "LEFT JOIN FETCH tn.translations " +
            "WHERE tn.item.id = :itemId")
    Optional<TastingNoteEntity> findByItemIdWithTranslations(@Param("itemId") Long itemId);

    Optional<TastingNoteEntity> findByItemId(Long itemId);

    boolean existsByItemId(Long itemId);
}

