package com.secretaria.FileStorage.repository;

import com.secretaria.FileStorage.entity.KeywordEntity;
import com.secretaria.FileStorage.entity.SubkeywordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubkeywordRepository extends JpaRepository<SubkeywordEntity, Long> {
    List<SubkeywordEntity> findByKeyword(KeywordEntity keyword);
}
