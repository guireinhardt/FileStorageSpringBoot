package com.secretaria.FileStorage.service;

import com.secretaria.FileStorage.entity.SearchLog;
import com.secretaria.FileStorage.entity.UsersEntity;
import com.secretaria.FileStorage.repository.SearchLogRepository;
import com.secretaria.FileStorage.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    public List<UsersEntity> getActiveUsers(LocalDateTime startDate, LocalDateTime endDate) {
        List<SearchLog> logs = searchLogRepository.findByOccurredAtBetween(startDate, endDate);
        return logs.stream()
                .map(SearchLog::getUser)
                .distinct()
                .toList();
    }
    //
    public Map<String, Long> getActiveUsersByDay(LocalDateTime startDate, LocalDateTime endDate) {
        List<SearchLog> logs = searchLogRepository.findByOccurredAtBetween(startDate, endDate);

        return logs.stream()
                .filter(log -> log.getUser() != null)
                .collect(Collectors.groupingBy(
                        log -> log.getOccurredAt().toLocalDate().toString(), // agrupa por dia
                        Collectors.mapping(SearchLog::getUser, Collectors.toSet()) // usuários distintos por dia
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (long) e.getValue().size() // contar usuários distintos por dia
                ));
    }


    public Map<String, Long> getMostSearchedKeywords(LocalDateTime startDate, LocalDateTime endDate) {
        List<SearchLog> logs = searchLogRepository.findByOccurredAtBetween(startDate, endDate);

        return logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getKeyword().getPalavra(),
                        Collectors.counting()
                ));
    }

    // Função fictícia para obter a região a partir do IP
    private String getRegionFromIp(String ipAddress) {
        // Integre aqui com uma API externa para geolocalização
        return "Brasil";  // Exemplo de retorno estático
    }

    // Relatório de acessos diários
    public Map<String, Long> getAccessesByDay(LocalDateTime startDate, LocalDateTime endDate) {
        List<SearchLog> logs = searchLogRepository.findByOccurredAtBetween(startDate, endDate);

        return logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getOccurredAt().toLocalDate().toString(),  // Agrupando por dia
                        Collectors.counting()
                ));
    }

    // Relatório de acessos mensais
    public Map<String, Long> getAccessesByMonth(LocalDateTime startDate, LocalDateTime endDate) {
        List<SearchLog> logs = searchLogRepository.findByOccurredAtBetween(startDate, endDate);

        return logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getOccurredAt().getMonth().toString(),  // Agrupando por mês
                        Collectors.counting()
                ));
    }

}
