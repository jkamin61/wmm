package com.org.wmm.content.items.entity;

import com.org.wmm.languages.entity.LanguageEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "item_translations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"item_id", "language_id"})
})
public class ItemTranslationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemEntity item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private LanguageEntity language;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @Size(max = 255)
    @Column(name = "subtitle")
    private String subtitle;

    @Column(name = "excerpt", columnDefinition = "TEXT")
    private String excerpt;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Size(max = 255)
    @Column(name = "meta_title")
    private String metaTitle;

    @Size(max = 500)
    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Size(max = 500)
    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}

