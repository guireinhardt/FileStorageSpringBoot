package com.guireinhardt.FileStorage.repository;

import com.guireinhardt.FileStorage.entity.KeywordEntity;
import com.guireinhardt.FileStorage.entity.SubkeywordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubkeywordRepository extends JpaRepository<SubkeywordEntity, Long> {
    List<SubkeywordEntity> findByKeyword(KeywordEntity keyword);

    // ✅ Novo método necessário para buscar a subkeyword pelo texto + keyword
    Optional<SubkeywordEntity> findByPalavraAndKeyword(String palavra, KeywordEntity keyword);
}
