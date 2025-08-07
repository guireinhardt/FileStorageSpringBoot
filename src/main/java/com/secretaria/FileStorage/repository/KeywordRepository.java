package com.secretaria.FileStorage.repository;

import com.secretaria.FileStorage.entity.KeywordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeywordRepository  extends JpaRepository<KeywordEntity, Long> {
    // Método para buscar a KeywordEntity pelo valor da palavra (campo "palavra")
    KeywordEntity findByPalavra(String palavra);
}
