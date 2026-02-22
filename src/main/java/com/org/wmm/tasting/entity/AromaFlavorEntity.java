package com.org.wmm.tasting.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "aroma_flavors", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tasting_note_id", "flavor_id"})
})
public class AromaFlavorEntity extends BaseFlavorProfileEntity {
}

