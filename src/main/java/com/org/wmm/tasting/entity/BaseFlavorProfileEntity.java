package com.org.wmm.tasting.entity;

import com.org.wmm.content.items.entity.TastingNoteEntity;
import com.org.wmm.flavors.entity.FlavorEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseFlavorProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tasting_note_id", nullable = false)
    private TastingNoteEntity tastingNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flavor_id", nullable = false)
    private FlavorEntity flavor;

    @NotNull
    @Column(name = "intensity", nullable = false)
    private Short intensity = 1;

    @NotNull
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}

