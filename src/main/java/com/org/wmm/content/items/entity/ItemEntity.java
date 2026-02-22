package com.org.wmm.content.items.entity;

import com.org.wmm.content.categories.entity.CategoryEntity;
import com.org.wmm.content.subtopics.entity.SubtopicEntity;
import com.org.wmm.content.topics.entity.TopicEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "items")
public class ItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private TopicEntity topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtopic_id")
    private SubtopicEntity subtopic;

    @Column(name = "partner_id")
    private Long partnerId;

    @Size(max = 200)
    @NotNull
    @Column(name = "slug", nullable = false, unique = true, length = 200)
    private String slug;

    @Column(name = "abv", precision = 5, scale = 2)
    private BigDecimal abv;

    @Column(name = "vintage")
    private Integer vintage;

    @Column(name = "volume_ml")
    private Integer volumeMl;

    @Column(name = "price_pln", precision = 10, scale = 2)
    private BigDecimal pricePln;

    @NotNull
    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Size(max = 20)
    @NotNull
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "draft";

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemTranslationEntity> translations = new ArrayList<>();

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<ImageEntity> images = new ArrayList<>();

    @OneToOne(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private TastingNoteEntity tastingNote;
}

