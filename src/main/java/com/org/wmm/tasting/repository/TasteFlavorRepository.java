package com.org.wmm.tasting.repository;

import com.org.wmm.tasting.entity.TasteFlavorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TasteFlavorRepository extends JpaRepository<TasteFlavorEntity, Long> {

    @Query("SELECT tf FROM TasteFlavorEntity tf LEFT JOIN FETCH tf.flavor WHERE tf.tastingNote.id = :noteId ORDER BY tf.displayOrder ASC")
    List<TasteFlavorEntity> findByTastingNoteIdWithFlavor(@Param("noteId") Long noteId);

    void deleteAllByTastingNoteId(Long tastingNoteId);
}

