package com.secretaria.FileStorage.service;

import com.secretaria.FileStorage.entity.SearchLog;
import com.secretaria.FileStorage.entity.UsersEntity;
import com.secretaria.FileStorage.repository.SearchLogRepository;
import com.secretaria.FileStorage.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public List<UsersEntity> getActiveUsers(LocalDate startDate, LocalDate endDate) {
        // Ajustando a consulta para buscar por LocalDate
        List<SearchLog> logs = searchLogRepository.findByOccurredAtBetween(startDate, endDate);

        // Filtrando os logs para obter usuários distintos
        return logs.stream()
                .map(SearchLog::getUser)  // Extrai o usuário de cada log
                .distinct()  // Garante que apenas usuários únicos sejam retornados
                .toList();  // Converte o Stream em uma lista
    }

    //
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
    public Map<String, Long> getAccessesByDay(LocalDate startDate, LocalDate endDate) {
        List<SearchLog> logs = searchLogRepository.findByOccurredAtBetween(startDate, endDate);

        return logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getOccurredAt().toString(), // Agrupando por data (formato "yyyy-MM-dd")
                        Collectors.counting() // Contando a quantidade de acessos por data
                ));
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
}
