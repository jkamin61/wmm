package com.org.wmm.content.items.entity;

import com.org.wmm.tasting.entity.AromaFlavorEntity;
import com.org.wmm.tasting.entity.FinishFlavorEntity;
import com.org.wmm.tasting.entity.TasteFlavorEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasting_notes")
public class TastingNoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, unique = true)
    private ItemEntity item;

    @Column(name = "overall_score", precision = 4, scale = 2)
    private BigDecimal overallScore;

    @Column(name = "aroma_score", precision = 4, scale = 2)
    private BigDecimal aromaScore;

    @Column(name = "taste_score", precision = 4, scale = 2)
    private BigDecimal tasteScore;

    @Column(name = "finish_score", precision = 4, scale = 2)
    private BigDecimal finishScore;

    @Column(name = "intensity")
    private Short intensity;

    @Column(name = "tasting_date")
    private LocalDate tastingDate;

    @Size(max = 150)
    @Column(name = "tasted_by", length = 150)
    private String tastedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "tastingNote", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TastingNoteTranslationEntity> translations = new ArrayList<>();

    @OneToMany(mappedBy = "tastingNote", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<AromaFlavorEntity> aromaFlavors = new ArrayList<>();

    @OneToMany(mappedBy = "tastingNote", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<TasteFlavorEntity> tasteFlavors = new ArrayList<>();

    @OneToMany(mappedBy = "tastingNote", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<FinishFlavorEntity> finishFlavors = new ArrayList<>();
}

