package com.org.wmm.content.items.entity;

import com.org.wmm.languages.entity.LanguageEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasting_note_translations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tasting_note_id", "language_id"})
})
public class TastingNoteTranslationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tasting_note_id", nullable = false)
    private TastingNoteEntity tastingNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private LanguageEntity language;

    @Column(name = "aroma_notes", columnDefinition = "TEXT")
    private String aromaNotes;

    @Column(name = "taste_notes", columnDefinition = "TEXT")
    private String tasteNotes;

    @Column(name = "finish_notes", columnDefinition = "TEXT")
    private String finishNotes;

    @Column(name = "overall_impression", columnDefinition = "TEXT")
    private String overallImpression;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}

