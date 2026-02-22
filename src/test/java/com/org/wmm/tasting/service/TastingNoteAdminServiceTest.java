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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TastingNoteAdminServiceTest {

    @Mock
    private TastingNoteRepository tastingNoteRepository;
    @Mock
    private AromaFlavorRepository aromaFlavorRepository;
    @Mock
    private TasteFlavorRepository tasteFlavorRepository;
    @Mock
    private FinishFlavorRepository finishFlavorRepository;
    @Mock
    private FlavorRepository flavorRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private LanguageQueryService languageQueryService;
    @Mock
    private TastingNoteMapper tastingNoteMapper;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private TastingNoteAdminService service;

    private ItemEntity item;
    private TastingNoteEntity note;
    private LanguageEntity polish;
    private TastingNoteDto noteDto;

    @BeforeEach
    void setUp() {
        item = ItemEntity.builder().id(1L).slug("talisker-10").build();

        note = TastingNoteEntity.builder()
                .id(10L)
                .item(item)
                .overallScore(new BigDecimal("87.50"))
                .aromaScore(new BigDecimal("88.00"))
                .tasteScore(new BigDecimal("86.00"))
                .finishScore(new BigDecimal("85.00"))
                .intensity((short) 2)
                .tastingDate(LocalDate.of(2026, 2, 15))
                .translations(new ArrayList<>())
                .aromaFlavors(new ArrayList<>())
                .tasteFlavors(new ArrayList<>())
                .finishFlavors(new ArrayList<>())
                .build();

        polish = LanguageEntity.builder().id(1L).code("pl").name("Polish").isDefault(true).build();

        noteDto = TastingNoteDto.builder()
                .id(10L).itemId(1L)
                .overallScore(new BigDecimal("87.50"))
                .flavorProfile(TastingNoteDto.FlavorProfileDto.builder()
                        .aroma(List.of()).taste(List.of()).finish(List.of()).build())
                .translations(List.of())
                .build();
    }

    @Nested
    @DisplayName("upsertTastingNote")
    class UpsertTastingNote {

        @Test
        @DisplayName("should create new tasting note when none exists")
        void shouldCreateNew() {
            UpsertTastingNoteRequest request = UpsertTastingNoteRequest.builder()
                    .overallScore(new BigDecimal("87.50"))
                    .aromaScore(new BigDecimal("88.00"))
                    .intensity((short) 2)
                    .tastingDate(LocalDate.of(2026, 2, 15))
                    .build();

            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(tastingNoteRepository.findByItemId(1L)).thenReturn(Optional.empty());
            when(tastingNoteRepository.save(any(TastingNoteEntity.class))).thenReturn(note);
            when(tastingNoteRepository.findByItemIdWithTranslations(1L)).thenReturn(Optional.of(note));
            when(aromaFlavorRepository.findByTastingNoteIdWithFlavor(10L)).thenReturn(List.of());
            when(tasteFlavorRepository.findByTastingNoteIdWithFlavor(10L)).thenReturn(List.of());
            when(finishFlavorRepository.findByTastingNoteIdWithFlavor(10L)).thenReturn(List.of());
            when(tastingNoteMapper.toDto(any(), anyList(), anyList(), anyList())).thenReturn(noteDto);

            TastingNoteDto result = service.upsertTastingNote(1L, request);

            assertThat(result).isNotNull();
            verify(tastingNoteRepository).save(any(TastingNoteEntity.class));
            verify(auditService).logCreate(eq("tasting_note"), eq(10L), anyString());
        }

        @Test
        @DisplayName("should update existing tasting note")
        void shouldUpdateExisting() {
            UpsertTastingNoteRequest request = UpsertTastingNoteRequest.builder()
                    .overallScore(new BigDecimal("90.00"))
                    .build();

            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(tastingNoteRepository.findByItemId(1L)).thenReturn(Optional.of(note));
            when(tastingNoteRepository.save(any(TastingNoteEntity.class))).thenReturn(note);
            when(tastingNoteRepository.findByItemIdWithTranslations(1L)).thenReturn(Optional.of(note));
            when(aromaFlavorRepository.findByTastingNoteIdWithFlavor(10L)).thenReturn(List.of());
            when(tasteFlavorRepository.findByTastingNoteIdWithFlavor(10L)).thenReturn(List.of());
            when(finishFlavorRepository.findByTastingNoteIdWithFlavor(10L)).thenReturn(List.of());
            when(tastingNoteMapper.toDto(any(), anyList(), anyList(), anyList())).thenReturn(noteDto);

            service.upsertTastingNote(1L, request);

            verify(auditService).logUpdate(eq("tasting_note"), eq(10L), isNull(), anyString());
        }

        @Test
        @DisplayName("should throw when item not found")
        void shouldThrowWhenItemNotFound() {
            when(itemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.upsertTastingNote(99L, new UpsertTastingNoteRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateTranslations")
    class UpdateTranslations {

        @Test
        @DisplayName("should add new translation")
        void shouldAddTranslation() {
            when(itemRepository.existsById(1L)).thenReturn(true);
            when(tastingNoteRepository.findByItemIdWithTranslations(1L)).thenReturn(Optional.of(note));
            when(languageQueryService.resolveLanguage("pl")).thenReturn(polish);
            when(tastingNoteRepository.save(any(TastingNoteEntity.class))).thenReturn(note);
            when(aromaFlavorRepository.findByTastingNoteIdWithFlavor(10L)).thenReturn(List.of());
            when(tasteFlavorRepository.findByTastingNoteIdWithFlavor(10L)).thenReturn(List.of());
            when(finishFlavorRepository.findByTastingNoteIdWithFlavor(10L)).thenReturn(List.of());
            when(tastingNoteMapper.toDto(any(), anyList(), anyList(), anyList())).thenReturn(noteDto);

            UpdateTastingNoteTranslationsRequest request = UpdateTastingNoteTranslationsRequest.builder()
                    .translations(List.of(
                            TastingNoteTranslationRequest.builder()
                                    .languageCode("pl")
                                    .aromaNotes("Dym, morze")
                                    .tasteNotes("Pieprz, cytrusy")
                                    .finishNotes("Długi, dymny")
                                    .overallImpression("Świetna whisky")
                                    .build()
                    )).build();

            service.updateTranslations(1L, request);

            assertThat(note.getTranslations()).hasSize(1);
            verify(tastingNoteRepository).save(any(TastingNoteEntity.class));
        }

        @Test
        @DisplayName("should throw when tasting note not found")
        void shouldThrowWhenNoteNotFound() {
            when(itemRepository.existsById(1L)).thenReturn(true);
            when(tastingNoteRepository.findByItemIdWithTranslations(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateTranslations(1L,
                    UpdateTastingNoteTranslationsRequest.builder()
                            .translations(List.of(TastingNoteTranslationRequest.builder()
                                    .languageCode("pl").build()))
                            .build()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateFlavorProfile")
    class UpdateFlavorProfile {

        @Test
        @DisplayName("should replace aroma flavors")
        void shouldReplaceAromaFlavors() {
            FlavorEntity smokeFlavor = FlavorEntity.builder().id(1L).slug("smoke").build();

            when(itemRepository.existsById(1L)).thenReturn(true);
            when(tastingNoteRepository.findByItemId(1L)).thenReturn(Optional.of(note));
            when(flavorRepository.existsById(1L)).thenReturn(true);
            when(flavorRepository.getReferenceById(1L)).thenReturn(smokeFlavor);
            when(aromaFlavorRepository.save(any(AromaFlavorEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(tastingNoteRepository.findByItemIdWithTranslations(1L)).thenReturn(Optional.of(note));
            when(aromaFlavorRepository.findByTastingNoteIdWithFlavor(10L)).thenReturn(List.of());
            when(tasteFlavorRepository.findByTastingNoteIdWithFlavor(10L)).thenReturn(List.of());
            when(finishFlavorRepository.findByTastingNoteIdWithFlavor(10L)).thenReturn(List.of());
            when(tastingNoteMapper.toDto(any(), anyList(), anyList(), anyList())).thenReturn(noteDto);

            UpdateFlavorProfileRequest request = UpdateFlavorProfileRequest.builder()
                    .aroma(List.of(
                            FlavorProfileEntry.builder().flavorId(1L).intensity((short) 3).order(0).build()
                    )).build();

            service.updateFlavorProfile(1L, request);

            verify(aromaFlavorRepository).deleteAllByTastingNoteId(10L);
            verify(aromaFlavorRepository).save(any(AromaFlavorEntity.class));
            // taste and finish should NOT be touched
            verify(tasteFlavorRepository, never()).deleteAllByTastingNoteId(anyLong());
            verify(finishFlavorRepository, never()).deleteAllByTastingNoteId(anyLong());
        }

        @Test
        @DisplayName("should reject duplicate flavor in section")
        void shouldRejectDuplicateFlavor() {
            when(itemRepository.existsById(1L)).thenReturn(true);
            when(tastingNoteRepository.findByItemId(1L)).thenReturn(Optional.of(note));

            UpdateFlavorProfileRequest request = UpdateFlavorProfileRequest.builder()
                    .aroma(List.of(
                            FlavorProfileEntry.builder().flavorId(1L).intensity((short) 2).order(0).build(),
                            FlavorProfileEntry.builder().flavorId(1L).intensity((short) 3).order(1).build()
                    )).build();

            assertThatThrownBy(() -> service.updateFlavorProfile(1L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Duplicate flavor");
        }

        @Test
        @DisplayName("should reject too many flavors in section")
        void shouldRejectTooManyFlavors() {
            when(itemRepository.existsById(1L)).thenReturn(true);
            when(tastingNoteRepository.findByItemId(1L)).thenReturn(Optional.of(note));

            List<FlavorProfileEntry> tooMany = new ArrayList<>();
            for (int i = 0; i < 16; i++) {
                tooMany.add(FlavorProfileEntry.builder()
                        .flavorId((long) (i + 1)).intensity((short) 1).order(i).build());
            }

            UpdateFlavorProfileRequest request = UpdateFlavorProfileRequest.builder()
                    .aroma(tooMany).build();

            assertThatThrownBy(() -> service.updateFlavorProfile(1L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Maximum");
        }

        @Test
        @DisplayName("should throw when flavor does not exist")
        void shouldThrowWhenFlavorNotFound() {
            when(itemRepository.existsById(1L)).thenReturn(true);
            when(tastingNoteRepository.findByItemId(1L)).thenReturn(Optional.of(note));
            when(flavorRepository.existsById(999L)).thenReturn(false);

            UpdateFlavorProfileRequest request = UpdateFlavorProfileRequest.builder()
                    .taste(List.of(
                            FlavorProfileEntry.builder().flavorId(999L).intensity((short) 1).order(0).build()
                    )).build();

            assertThatThrownBy(() -> service.updateFlavorProfile(1L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should clear section when empty list provided")
        void shouldClearSectionWithEmptyList() {
            when(itemRepository.existsById(1L)).thenReturn(true);
            when(tastingNoteRepository.findByItemId(1L)).thenReturn(Optional.of(note));
            when(tastingNoteRepository.findByItemIdWithTranslations(1L)).thenReturn(Optional.of(note));
            when(aromaFlavorRepository.findByTastingNoteIdWithFlavor(10L)).thenReturn(List.of());
            when(tasteFlavorRepository.findByTastingNoteIdWithFlavor(10L)).thenReturn(List.of());
            when(finishFlavorRepository.findByTastingNoteIdWithFlavor(10L)).thenReturn(List.of());
            when(tastingNoteMapper.toDto(any(), anyList(), anyList(), anyList())).thenReturn(noteDto);

            UpdateFlavorProfileRequest request = UpdateFlavorProfileRequest.builder()
                    .finish(List.of())
                    .build();

            service.updateFlavorProfile(1L, request);

            verify(finishFlavorRepository).deleteAllByTastingNoteId(10L);
            verify(finishFlavorRepository, never()).save(any());
        }
    }
}

