package com.guireinhardt.FileStorage.service;

import com.guireinhardt.FileStorage.dto.SearchDayCountDTO;
import com.guireinhardt.FileStorage.dto.SearchTermCountDTO;
import com.guireinhardt.FileStorage.entity.SearchLog;
import com.guireinhardt.FileStorage.entity.UsersEntity;
import com.guireinhardt.FileStorage.repository.SearchLogRepository;
import com.guireinhardt.FileStorage.repository.UsersRepository;
import com.guireinhardt.FileStorage.dto.ChartDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    @Autowired
    private SearchLogRepository searchLogRepository;
    @Autowired
    private UsersRepository usersRepository;

    //Usuários ativos total sem intervalo de datas
    public long getActiveUsersCount() {
        return usersRepository.countActiveUsers(); // ou countActiveUsers()
    }
    // Usuários ativos dentro de um intervalo de datas
    public List<UsersEntity> getActiveUsers(LocalDate startDate, LocalDate endDate) {
        // Ajustando a consulta para buscar por LocalDate
        List<SearchLog> logs = searchLogRepository.findByOccurredAtBetween(startDate, endDate);

        // Filtrando os logs para obter usuários distintos
        return logs.stream()
                .map(SearchLog::getUser)  // Extrai o usuário de cada log
                .distinct()  // Garante que apenas usuários únicos sejam retornados
                .toList();  // Converte o Stream em uma lista
    }

    // Agrupa os usuários por dia e conta a quantidade de acessos por dia
    public Map<String, Long> getActiveUsersByDay(LocalDate startDate, LocalDate endDate) {
        List<SearchLog> logs = searchLogRepository.findByOccurredAtBetween(startDate, endDate);

        return logs.stream()
                .filter(log -> log.getUser() != null) // Certificando-se de que o log tem um usuário
                .collect(Collectors.groupingBy(
                        log -> log.getOccurredAt().toString(),  // Agrupando por data (usando toLocalDate para garantir que a data seja sem hora)
                        Collectors.mapping(SearchLog::getUser, Collectors.toSet()) // Contando os usuários distintos por dia
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (long) e.getValue().size() // Contagem de usuários distintos por dia
                ));
    }

    // Retorna o relatório das palavras mais pesquisadas
    public Map<String, Long> getMostSearchedKeywords(LocalDate startDate, LocalDate endDate) {
        List<SearchLog> logs = searchLogRepository.findByOccurredAtBetween(startDate, endDate);

        return logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getKeyword().getPalavra(),  // Agrupa pelas palavras-chave
                        Collectors.counting()  // Conta a quantidade de vezes que cada palavra foi pesquisada
                ));
    }

    // Relatório de acessos diários
    public ChartDTO getAccessesByDay(LocalDate start, LocalDate end) {
        // Buscar os dados agregados por dia
        List<Object[]> rows = searchLogRepository.countByDayBetween(start, end);

        // Criando um mapa para armazenar as contagens de acessos por dia
        Map<LocalDate, Long> accessesByDay = new LinkedHashMap<>();
        for (Object[] row : rows) {
            LocalDate day = (LocalDate) row[0];  // A data do acesso
            Long count = (Long) row[1];  // A contagem de acessos
            accessesByDay.put(day, count);
        }

        // Preenchendo os dias faltantes com zero
        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            labels.add(date.toString());  // Adiciona a data como rótulo
            counts.add(accessesByDay.getOrDefault(date, 0L));  // Se não houver registros, coloca 0
        }

        return new ChartDTO(labels, counts);  // Retorna os dados prontos para o frontend
    }

    // Relatório de acessos mensais
    public Map<String, Long> getAccessesByMonth(LocalDate startDate, LocalDate endDate) {
        List<SearchLog> logs = searchLogRepository.findByOccurredAtBetween(startDate, endDate);

        return logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getOccurredAt().getMonth().toString(), // Agrupando por mês (mês por nome)
                        Collectors.counting() // Contando a quantidade de acessos por mês
                ));
    }

    // Função fictícia para obter a região a partir do IP
    private String getRegionFromIp(String ipAddress) {
        // Integre aqui com uma API externa para geolocalização
        return "Brasil";  // Exemplo de retorno estático
    }

    // Contagem de buscas por palavra-chave
    public Map<LocalDate, Long> getSearchCountByDay(LocalDate startDate, LocalDate endDate) {
        List<SearchLog> logs = searchLogRepository.findByOccurredAtBetween(startDate, endDate);

        return logs.stream()
                .collect(Collectors.groupingBy(
                        SearchLog::getOccurredAt,  // Agrupa pela data de pesquisa
                        Collectors.counting()      // Conta quantas vezes cada data apareceu
                ));
    }
    public Map<String, Long> getSearchCountByKeyword(LocalDate start, LocalDate end) {
        // Busca logs de busca no intervalo de datas
        List<SearchLog> logs = searchLogRepository.findByOccurredAtBetween(start, end);

        // Agrupa os logs por palavra-chave e conta a quantidade de buscas para cada uma
        return logs.stream()
                .filter(log -> log.getKeyword() != null)  // Garante que estamos contando apenas palavras-chave válidas
                .collect(Collectors.groupingBy(
                        log -> log.getKeyword().getPalavra(),  // Agrupando pela palavra-chave
                        Collectors.counting()                  // Contando quantas vezes cada palavra-chave apareceu
                ));
    }

    public List<SearchDayCountDTO> getSearchesByDay(LocalDate start, LocalDate end) {
        List<Object[]> rows = searchLogRepository.countByDayBetween(start, end);
        return rows.stream()
                .map(r -> new SearchDayCountDTO(
                        (LocalDate) r[0],
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }

    // 📌 Top termos (coalesce(keyword.palavra, query))
    public List<SearchTermCountDTO> getTopTerms(LocalDate start, LocalDate end, int limit) {
        List<Object[]> rows = searchLogRepository.countSearchesByKeywordBetween(start, end);
        return rows.stream()
                .map(r -> new SearchTermCountDTO(
                        (String) r[0],
                        ((Number) r[1]).longValue()
                ))
                .sorted(Comparator.comparingLong(SearchTermCountDTO::getTotal).reversed())
                .limit(limit)
                .toList();
    }

    // 📌 Top termos usando query pura (se quiser analisar só `sl.query`)
    public List<SearchTermCountDTO> getTopRawQueries(LocalDate start, LocalDate end, int limit) {
        List<Object[]> rows = searchLogRepository.countSearchTermsBetween(start, end);
        return rows.stream()
                .map(r -> new SearchTermCountDTO(
                        (String) r[0],
                        ((Number) r[1]).longValue()
                ))
                .sorted(Comparator.comparingLong(SearchTermCountDTO::getTotal).reversed())
                .limit(limit)
                .toList();
    }

    // 📌 Resumo geral (pode ser usado no “cards” do dashboard)
    public Map<String, Object> getSummary(LocalDate start, LocalDate end) {
        List<SearchDayCountDTO> byDay = getSearchesByDay(start, end);
        long totalSearches = byDay.stream().mapToLong(SearchDayCountDTO::getTotal).sum();

        long activeUsers = usersRepository.countActiveUsers();
        long totalUsers = usersRepository.count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalSearches", totalSearches);
        summary.put("daysWithSearches", byDay.size());
        summary.put("activeUsers", activeUsers);
        summary.put("totalUsers", totalUsers);

        return summary;
    }





}
