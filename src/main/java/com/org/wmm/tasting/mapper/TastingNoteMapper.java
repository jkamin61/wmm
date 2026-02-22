package com.org.wmm.tasting.mapper;

import com.org.wmm.content.items.entity.TastingNoteEntity;
import com.org.wmm.content.items.entity.TastingNoteTranslationEntity;
import com.org.wmm.tasting.dto.TastingNoteDto;
import com.org.wmm.tasting.entity.AromaFlavorEntity;
import com.org.wmm.tasting.entity.BaseFlavorProfileEntity;
import com.org.wmm.tasting.entity.FinishFlavorEntity;
import com.org.wmm.tasting.entity.TasteFlavorEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TastingNoteMapper {

    public TastingNoteDto toDto(TastingNoteEntity entity,
                                List<AromaFlavorEntity> aromaFlavors,
                                List<TasteFlavorEntity> tasteFlavors,
                                List<FinishFlavorEntity> finishFlavors) {

        List<TastingNoteDto.TranslationDto> translations = entity.getTranslations() != null
                ? entity.getTranslations().stream()
                .map(this::toTranslationDto)
                .collect(Collectors.toList())
                : List.of();

        TastingNoteDto.FlavorProfileDto flavorProfile = TastingNoteDto.FlavorProfileDto.builder()
                .aroma(mapFlavorEntries(aromaFlavors))
                .taste(mapFlavorEntries(tasteFlavors))
                .finish(mapFlavorEntries(finishFlavors))
                .build();

        return TastingNoteDto.builder()
                .id(entity.getId())
                .itemId(entity.getItem() != null ? entity.getItem().getId() : null)
                .overallScore(entity.getOverallScore())
                .aromaScore(entity.getAromaScore())
                .tasteScore(entity.getTasteScore())
                .finishScore(entity.getFinishScore())
                .intensity(entity.getIntensity())
                .tastingDate(entity.getTastingDate())
                .tastedBy(entity.getTastedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .translations(translations)
                .flavorProfile(flavorProfile)
                .build();
    }

    private TastingNoteDto.TranslationDto toTranslationDto(TastingNoteTranslationEntity entity) {
        return TastingNoteDto.TranslationDto.builder()
                .id(entity.getId())
                .languageCode(entity.getLanguage().getCode())
                .languageName(entity.getLanguage().getName())
                .aromaNotes(entity.getAromaNotes())
                .tasteNotes(entity.getTasteNotes())
                .finishNotes(entity.getFinishNotes())
                .overallImpression(entity.getOverallImpression())
                .build();
    }

    private <T extends BaseFlavorProfileEntity> List<TastingNoteDto.FlavorEntryDto> mapFlavorEntries(List<T> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(e -> TastingNoteDto.FlavorEntryDto.builder()
                        .flavorId(e.getFlavor().getId())
                        .flavorSlug(e.getFlavor().getSlug())
                        .flavorColor(e.getFlavor().getColor())
                        .flavorIcon(e.getFlavor().getIcon())
                        .intensity(e.getIntensity())
                        .displayOrder(e.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());
    }
}

