package com.org.wmm.languages.repository;

import com.org.wmm.languages.entity.LanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<LanguageEntity, Long> {

    List<LanguageEntity> findByIsActiveTrueOrderByDisplayOrderAsc();

    Optional<LanguageEntity> findByCode(String code);

    Optional<LanguageEntity> findByIsDefaultTrue();
}

