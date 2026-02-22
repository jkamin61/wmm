package com.org.wmm.tasting.repository;

import com.org.wmm.tasting.entity.FinishFlavorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinishFlavorRepository extends JpaRepository<FinishFlavorEntity, Long> {

    @Query("SELECT ff FROM FinishFlavorEntity ff LEFT JOIN FETCH ff.flavor WHERE ff.tastingNote.id = :noteId ORDER BY ff.displayOrder ASC")
    List<FinishFlavorEntity> findByTastingNoteIdWithFlavor(@Param("noteId") Long noteId);

    void deleteAllByTastingNoteId(Long tastingNoteId);
}

