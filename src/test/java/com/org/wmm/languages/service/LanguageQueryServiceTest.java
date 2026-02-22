package com.org.wmm.languages.service;

import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.languages.dto.LanguageDto;
import com.org.wmm.languages.entity.LanguageEntity;
import com.org.wmm.languages.repository.LanguageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LanguageQueryServiceTest {

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private LanguageQueryService service;

    private LanguageEntity polish;
    private LanguageEntity english;
    private LanguageEntity inactiveGerman;

    @BeforeEach
    void setUp() {
        polish = LanguageEntity.builder()
                .id(1L)
                .code("pl")
                .name("Polish")
                .nativeName("Polski")
                .isDefault(true)
                .isActive(true)
                .displayOrder(1)
                .build();

        english = LanguageEntity.builder()
                .id(2L)
                .code("en")
                .name("English")
                .nativeName("English")
                .isDefault(false)
                .isActive(true)
                .displayOrder(2)
                .build();

        inactiveGerman = LanguageEntity.builder()
                .id(3L)
                .code("de")
                .name("German")
                .nativeName("Deutsch")
                .isDefault(false)
                .isActive(false)
                .displayOrder(3)
                .build();
    }

    @Nested
    @DisplayName("getActiveLanguages")
    class GetActiveLanguages {

        @Test
        @DisplayName("should return list of active languages as DTOs")
        void shouldReturnActiveLanguages() {
            when(languageRepository.findByIsActiveTrueOrderByDisplayOrderAsc())
                    .thenReturn(List.of(polish, english));

            List<LanguageDto> result = service.getActiveLanguages();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getCode()).isEqualTo("pl");
            assertThat(result.get(0).getName()).isEqualTo("Polish");
            assertThat(result.get(0).getNativeName()).isEqualTo("Polski");
            assertThat(result.get(0).isDefault()).isTrue();
            assertThat(result.get(1).getCode()).isEqualTo("en");
            assertThat(result.get(1).isDefault()).isFalse();
        }

        @Test
        @DisplayName("should return empty list when no active languages")
        void shouldReturnEmptyList() {
            when(languageRepository.findByIsActiveTrueOrderByDisplayOrderAsc())
                    .thenReturn(List.of());

            List<LanguageDto> result = service.getActiveLanguages();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getDefaultLanguage")
    class GetDefaultLanguage {

        @Test
        @DisplayName("should return default language")
        void shouldReturnDefaultLanguage() {
            when(languageRepository.findByIsDefaultTrue())
                    .thenReturn(Optional.of(polish));

            LanguageEntity result = service.getDefaultLanguage();

            assertThat(result.getCode()).isEqualTo("pl");
            assertThat(result.getIsDefault()).isTrue();
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when no default language")
        void shouldThrowWhenNoDefault() {
            when(languageRepository.findByIsDefaultTrue())
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getDefaultLanguage())
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Default language");
        }
    }

    @Nested
    @DisplayName("resolveLanguage")
    class ResolveLanguage {

        @Test
        @DisplayName("should return requested language when code is valid and active")
        void shouldReturnRequestedLanguage() {
            when(languageRepository.findByCode("en"))
                    .thenReturn(Optional.of(english));

            LanguageEntity result = service.resolveLanguage("en");

            assertThat(result.getCode()).isEqualTo("en");
            verify(languageRepository, never()).findByIsDefaultTrue();
        }

        @Test
        @DisplayName("should fallback to default when code is null")
        void shouldFallbackWhenCodeNull() {
            when(languageRepository.findByIsDefaultTrue())
                    .thenReturn(Optional.of(polish));

            LanguageEntity result = service.resolveLanguage(null);

            assertThat(result.getCode()).isEqualTo("pl");
        }

        @Test
        @DisplayName("should fallback to default when code is blank")
        void shouldFallbackWhenCodeBlank() {
            when(languageRepository.findByIsDefaultTrue())
                    .thenReturn(Optional.of(polish));

            LanguageEntity result = service.resolveLanguage("  ");

            assertThat(result.getCode()).isEqualTo("pl");
        }

        @Test
        @DisplayName("should fallback to default when code does not exist")
        void shouldFallbackWhenCodeNotFound() {
            when(languageRepository.findByCode("xyz"))
                    .thenReturn(Optional.empty());
            when(languageRepository.findByIsDefaultTrue())
                    .thenReturn(Optional.of(polish));

            LanguageEntity result = service.resolveLanguage("xyz");

            assertThat(result.getCode()).isEqualTo("pl");
        }

        @Test
        @DisplayName("should fallback to default when language is inactive")
        void shouldFallbackWhenLanguageInactive() {
            when(languageRepository.findByCode("de"))
                    .thenReturn(Optional.of(inactiveGerman));
            when(languageRepository.findByIsDefaultTrue())
                    .thenReturn(Optional.of(polish));

            LanguageEntity result = service.resolveLanguage("de");

            assertThat(result.getCode()).isEqualTo("pl");
        }

        @Test
        @DisplayName("should trim and lowercase the language code")
        void shouldTrimAndLowercase() {
            when(languageRepository.findByCode("en"))
                    .thenReturn(Optional.of(english));

            LanguageEntity result = service.resolveLanguage("  EN  ");

            assertThat(result.getCode()).isEqualTo("en");
            verify(languageRepository).findByCode("en");
        }
    }

    @Nested
    @DisplayName("resolveLanguageId")
    class ResolveLanguageId {

        @Test
        @DisplayName("should return language ID for valid code")
        void shouldReturnId() {
            when(languageRepository.findByCode("en"))
                    .thenReturn(Optional.of(english));

            Long result = service.resolveLanguageId("en");

            assertThat(result).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("getDefaultLanguageId")
    class GetDefaultLanguageId {

        @Test
        @DisplayName("should return default language ID")
        void shouldReturnDefaultId() {
            when(languageRepository.findByIsDefaultTrue())
                    .thenReturn(Optional.of(polish));

            Long result = service.getDefaultLanguageId();

            assertThat(result).isEqualTo(1L);
        }
    }
}

