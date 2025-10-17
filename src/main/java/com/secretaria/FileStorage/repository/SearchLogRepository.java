package com.secretaria.FileStorage.repository;

import com.secretaria.FileStorage.entity.KeywordEntity;
import com.secretaria.FileStorage.entity.SearchLog;
import com.secretaria.FileStorage.entity.SubkeywordEntity;
import com.secretaria.FileStorage.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDate;
import java.util.List;

public interface SearchLogRepository  extends JpaRepository<SearchLog, Long> {
    List<SearchLog> findByUserAndOccurredAtBetween(UsersEntity user, LocalDate startDate, LocalDate endDate);
    List<SearchLog> findByOccurredAtBetween(LocalDate start, LocalDate end);
    List<SearchLog> findByUser(UsersEntity user);
    List<SearchLog> findByKeywordAndOccurredAtBetween(KeywordEntity keyword, LocalDate startDate, LocalDate endDate);
    List<SearchLog> findByKeyword(KeywordEntity keyword);
    List<SearchLog> findBySubkeyword(SubkeywordEntity subkeyword);
}
