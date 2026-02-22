package com.org.wmm.tasting.repository;

import com.org.wmm.tasting.entity.AromaFlavorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AromaFlavorRepository extends JpaRepository<AromaFlavorEntity, Long> {

    @Query("SELECT af FROM AromaFlavorEntity af LEFT JOIN FETCH af.flavor WHERE af.tastingNote.id = :noteId ORDER BY af.displayOrder ASC")
    List<AromaFlavorEntity> findByTastingNoteIdWithFlavor(@Param("noteId") Long noteId);

    void deleteAllByTastingNoteId(Long tastingNoteId);
}

