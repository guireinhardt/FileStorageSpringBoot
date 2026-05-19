package com.guireinhardt.FileStorage.repository;

import com.guireinhardt.FileStorage.entity.KeywordEntity;
import com.guireinhardt.FileStorage.entity.SearchLog;
import com.guireinhardt.FileStorage.entity.SubkeywordEntity;
import com.guireinhardt.FileStorage.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    List<SearchLog> findByUserAndOccurredAtBetween(UsersEntity user, LocalDate startDate, LocalDate endDate);

    @Query("SELECT sl.query AS term, COUNT(sl) AS total FROM SearchLog sl " +
            "WHERE sl.occurredAt BETWEEN :start AND :end " +
            "GROUP BY sl.query ORDER BY total DESC")
    List<Object[]> countSearchTermsBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);


    // ✅ Busca logs dentro de um intervalo de datas
    List<SearchLog> findByOccurredAtBetween(LocalDate start, LocalDate end);

    List<SearchLog> findByUser(UsersEntity user);

    List<SearchLog> findByKeywordAndOccurredAtBetween(KeywordEntity keyword, LocalDate startDate, LocalDate endDate);

    List<SearchLog> findByKeyword(KeywordEntity keyword);

    List<SearchLog> findBySubkeyword(SubkeywordEntity subkeyword);

    // ✅ Novo método para contar acessos por dia
    @Query("""
        select sl.occurredAt as day, count(sl) as total
        from SearchLog sl
        where sl.occurredAt between :start and :end
        group by sl.occurredAt
        order by sl.occurredAt
    """)
    List<Object[]> countByDayBetween(@Param("start") LocalDate startDate, @Param("end") LocalDate endDate);
    @Query("""
    select coalesce(k.palavra, sl.query) as term, count(sl) as total
    from SearchLog sl
    left join sl.keyword k
    where sl.occurredAt between :start and :end
    and (k.palavra is not null or sl.query is not null)
    group by coalesce(k.palavra, sl.query)
    order by total desc
""")
    List<Object[]> countSearchesByKeywordBetween(@Param("start") LocalDate start,
                                                 @Param("end") LocalDate end);



    // ✅ Para saber se já existe log da keyword naquele dia (para incrementar contagem)
    Optional<SearchLog> findByKeywordAndOccurredAt(KeywordEntity keyword, LocalDate occurredAt);

    // ✅ NOVO: separa log por keyword + subkeyword + dia
    Optional<SearchLog> findByKeywordAndSubkeywordAndOccurredAt(KeywordEntity keyword,
                                                                SubkeywordEntity subkeyword,
                                                                LocalDate occurredAt);


}
