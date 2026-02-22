package com.org.wmm.tasting.service;

import com.org.wmm.audit.service.AuditService;
import com.org.wmm.common.error.BadRequestException;
import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.content.items.entity.ItemEntity;
import com.org.wmm.content.items.entity.TastingNoteEntity;
import com.org.wmm.content.items.entity.TastingNoteTranslationEntity;
import com.org.wmm.content.items.repository.ItemRepository;
import com.org.wmm.flavors.entity.FlavorEntity;
import com.org.wmm.flavors.repository.FlavorRepository;
import com.org.wmm.languages.entity.LanguageEntity;
import com.org.wmm.languages.service.LanguageQueryService;
import com.org.wmm.tasting.dto.*;
import com.org.wmm.tasting.entity.AromaFlavorEntity;
import com.org.wmm.tasting.entity.FinishFlavorEntity;
import com.org.wmm.tasting.entity.TasteFlavorEntity;
import com.org.wmm.tasting.mapper.TastingNoteMapper;
import com.org.wmm.tasting.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TastingNoteAdminService {

    private final TastingNoteRepository tastingNoteRepository;
    private final AromaFlavorRepository aromaFlavorRepository;
    private final TasteFlavorRepository tasteFlavorRepository;
    private final FinishFlavorRepository finishFlavorRepository;
    private final FlavorRepository flavorRepository;
    private final ItemRepository itemRepository;
    private final LanguageQueryService languageQueryService;
    private final TastingNoteMapper tastingNoteMapper;
    private final AuditService auditService;

    private static final int MAX_FLAVORS_PER_SECTION = 15;

    // ─── GET ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public TastingNoteDto getTastingNote(Long itemId) {
        assertItemExists(itemId);
        TastingNoteEntity note = tastingNoteRepository.findByItemIdWithTranslations(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("TastingNote", "itemId", itemId));

        return buildFullDto(note);
    }

    // ─── UPSERT TASTING NOTE ──────────────────────────────────────

    @Transactional
    public TastingNoteDto upsertTastingNote(Long itemId, UpsertTastingNoteRequest request) {
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        TastingNoteEntity note = tastingNoteRepository.findByItemId(itemId)
                .orElse(null);

        boolean isCreate = (note == null);

        if (isCreate) {
            note = TastingNoteEntity.builder()
                    .item(item)
                    .build();
        }

        // Update scores
        if (request.getOverallScore() != null) note.setOverallScore(request.getOverallScore());
        if (request.getAromaScore() != null) note.setAromaScore(request.getAromaScore());
        if (request.getTasteScore() != null) note.setTasteScore(request.getTasteScore());
        if (request.getFinishScore() != null) note.setFinishScore(request.getFinishScore());
        if (request.getIntensity() != null) note.setIntensity(request.getIntensity());
        if (request.getTastingDate() != null) note.setTastingDate(request.getTastingDate());
        if (request.getTastedBy() != null) note.setTastedBy(request.getTastedBy());

        TastingNoteEntity saved = tastingNoteRepository.save(note);
        log.info("{} tasting note for item {} (noteId={})",
                isCreate ? "Created" : "Updated", itemId, saved.getId());

        if (isCreate) {
            auditService.logCreate("tasting_note", saved.getId(), "{\"itemId\":" + itemId + "}");
        } else {
            auditService.logUpdate("tasting_note", saved.getId(), null, "{\"itemId\":" + itemId + "}");
        }

        return buildFullDto(
                tastingNoteRepository.findByItemIdWithTranslations(itemId).orElse(saved));
    }

    // ─── TRANSLATIONS ──────────────────────────────────────────────

    @Transactional
    public TastingNoteDto updateTranslations(Long itemId, UpdateTastingNoteTranslationsRequest request) {
        assertItemExists(itemId);
        TastingNoteEntity note = tastingNoteRepository.findByItemIdWithTranslations(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("TastingNote", "itemId", itemId));

        for (TastingNoteTranslationRequest tr : request.getTranslations()) {
            LanguageEntity language = languageQueryService.resolveLanguage(tr.getLanguageCode());

            TastingNoteTranslationEntity existing = note.getTranslations().stream()
                    .filter(t -> t.getLanguage().getId().equals(language.getId()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setAromaNotes(tr.getAromaNotes());
                existing.setTasteNotes(tr.getTasteNotes());
                existing.setFinishNotes(tr.getFinishNotes());
                existing.setOverallImpression(tr.getOverallImpression());
            } else {
                TastingNoteTranslationEntity newTr = TastingNoteTranslationEntity.builder()
                        .tastingNote(note)
                        .language(language)
                        .aromaNotes(tr.getAromaNotes())
                        .tasteNotes(tr.getTasteNotes())
                        .finishNotes(tr.getFinishNotes())
                        .overallImpression(tr.getOverallImpression())
                        .build();
                note.getTranslations().add(newTr);
            }
        }

        TastingNoteEntity saved = tastingNoteRepository.save(note);
        log.info("Updated tasting note translations for item {}", itemId);
        auditService.logUpdate("tasting_note", saved.getId(), null, "{\"action\":\"update_translations\"}");

        return buildFullDto(
                tastingNoteRepository.findByItemIdWithTranslations(itemId).orElse(saved));
    }

    // ─── FLAVOR PROFILE ────────────────────────────────────────────

    @Transactional
    public TastingNoteDto updateFlavorProfile(Long itemId, UpdateFlavorProfileRequest request) {
        assertItemExists(itemId);
        TastingNoteEntity note = tastingNoteRepository.findByItemId(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("TastingNote", "itemId", itemId));

        // Process each section if present in the request
        if (request.getAroma() != null) {
            validateFlavorSection("aroma", request.getAroma());
            replaceAromaFlavors(note, request.getAroma());
        }

        if (request.getTaste() != null) {
            validateFlavorSection("taste", request.getTaste());
            replaceTasteFlavors(note, request.getTaste());
        }

        if (request.getFinish() != null) {
            validateFlavorSection("finish", request.getFinish());
            replaceFinishFlavors(note, request.getFinish());
        }

        log.info("Updated flavor profile for item {} (noteId={})", itemId, note.getId());
        auditService.logUpdate("tasting_note", note.getId(), null, "{\"action\":\"update_flavor_profile\"}");

        return buildFullDto(
                tastingNoteRepository.findByItemIdWithTranslations(itemId).orElse(note));
    }

    // ─── PRIVATE HELPERS ───────────────────────────────────────────

    private TastingNoteDto buildFullDto(TastingNoteEntity note) {
        // Fetch flavor profiles separately to avoid Hibernate MultipleBagFetchException
        List<AromaFlavorEntity> aromas = aromaFlavorRepository.findByTastingNoteIdWithFlavor(note.getId());
        List<TasteFlavorEntity> tastes = tasteFlavorRepository.findByTastingNoteIdWithFlavor(note.getId());
        List<FinishFlavorEntity> finishes = finishFlavorRepository.findByTastingNoteIdWithFlavor(note.getId());

        return tastingNoteMapper.toDto(note, aromas, tastes, finishes);
    }

    private void validateFlavorSection(String sectionName, List<FlavorProfileEntry> entries) {
        if (entries.size() > MAX_FLAVORS_PER_SECTION) {
            throw new BadRequestException("Maximum " + MAX_FLAVORS_PER_SECTION +
                    " flavors allowed per section (" + sectionName + "). Got " + entries.size());
        }

        // Check for duplicate flavors within section
        Set<Long> flavorIds = new HashSet<>();
        for (FlavorProfileEntry entry : entries) {
            if (!flavorIds.add(entry.getFlavorId())) {
                throw new BadRequestException("Duplicate flavor ID " + entry.getFlavorId() +
                        " in " + sectionName + " section");
            }
        }

        // Validate all flavor IDs exist
        for (FlavorProfileEntry entry : entries) {
            if (!flavorRepository.existsById(entry.getFlavorId())) {
                throw new ResourceNotFoundException("Flavor", "id", entry.getFlavorId());
            }
        }
    }

    private void replaceAromaFlavors(TastingNoteEntity note, List<FlavorProfileEntry> entries) {
        aromaFlavorRepository.deleteAllByTastingNoteId(note.getId());
        aromaFlavorRepository.flush();

        for (FlavorProfileEntry entry : entries) {
            FlavorEntity flavor = flavorRepository.getReferenceById(entry.getFlavorId());
            AromaFlavorEntity entity = new AromaFlavorEntity();
            entity.setTastingNote(note);
            entity.setFlavor(flavor);
            entity.setIntensity(entry.getIntensity());
            entity.setDisplayOrder(entry.getOrder());
            aromaFlavorRepository.save(entity);
        }
    }

    private void replaceTasteFlavors(TastingNoteEntity note, List<FlavorProfileEntry> entries) {
        tasteFlavorRepository.deleteAllByTastingNoteId(note.getId());
        tasteFlavorRepository.flush();

        for (FlavorProfileEntry entry : entries) {
            FlavorEntity flavor = flavorRepository.getReferenceById(entry.getFlavorId());
            TasteFlavorEntity entity = new TasteFlavorEntity();
            entity.setTastingNote(note);
            entity.setFlavor(flavor);
            entity.setIntensity(entry.getIntensity());
            entity.setDisplayOrder(entry.getOrder());
            tasteFlavorRepository.save(entity);
        }
    }

    private void replaceFinishFlavors(TastingNoteEntity note, List<FlavorProfileEntry> entries) {
        finishFlavorRepository.deleteAllByTastingNoteId(note.getId());
        finishFlavorRepository.flush();

        for (FlavorProfileEntry entry : entries) {
            FlavorEntity flavor = flavorRepository.getReferenceById(entry.getFlavorId());
            FinishFlavorEntity entity = new FinishFlavorEntity();
            entity.setTastingNote(note);
            entity.setFlavor(flavor);
            entity.setIntensity(entry.getIntensity());
            entity.setDisplayOrder(entry.getOrder());
            finishFlavorRepository.save(entity);
        }
    }

    private void assertItemExists(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Item", "id", itemId);
        }
    }
}


