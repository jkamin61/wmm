package com.org.wmm.languages.service;

import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.languages.dto.LanguageDto;
import com.org.wmm.languages.entity.LanguageEntity;
import com.org.wmm.languages.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageQueryService {

    private final LanguageRepository languageRepository;

    /**
     * Returns all active languages ordered by display_order.
     */
    public List<LanguageDto> getActiveLanguages() {
        return languageRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Returns the default language entity.
     */
    public LanguageEntity getDefaultLanguage() {
        return languageRepository.findByIsDefaultTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Default language not configured"));
    }

    /**
     * Resolves the language entity: if code is provided and valid, use it; otherwise fallback to default.
     */
    public LanguageEntity resolveLanguage(String langCode) {
        if (langCode != null && !langCode.isBlank()) {
            return languageRepository.findByCode(langCode.trim().toLowerCase())
                    .filter(LanguageEntity::getIsActive)
                    .orElseGet(this::getDefaultLanguage);
        }
        return getDefaultLanguage();
    }

    /**
     * Returns the language ID for the given code, falling back to default if not found.
     */
    public Long resolveLanguageId(String langCode) {
        return resolveLanguage(langCode).getId();
    }

    /**
     * Returns the default language ID.
     */
    public Long getDefaultLanguageId() {
        return getDefaultLanguage().getId();
    }

    private LanguageDto toDto(LanguageEntity entity) {
        return LanguageDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .nativeName(entity.getNativeName())
                .isDefault(Boolean.TRUE.equals(entity.getIsDefault()))
                .build();
    }
}

