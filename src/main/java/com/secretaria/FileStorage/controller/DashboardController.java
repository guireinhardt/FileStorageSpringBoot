package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard/active-users")
    public String getActiveUsersDashboard(@RequestParam(required = false) String period, Model model) {
        LocalDateTime endDate = LocalDateTime.now();
        int days = period != null ? Integer.parseInt(period) : 7;
        LocalDateTime startDate = endDate.minusDays(days);

        // Total de usuários ativos no período
        long totalActiveUsers = dashboardService.getActiveUsers(startDate, endDate).size();

        // Usuários ativos por dia
        Map<String, Long> activeUsersByDay = dashboardService.getActiveUsersByDay(startDate, endDate);

        // Converter para lista de objetos simples para Thymeleaf
        List<DailyUserData> dailyUsers = activeUsersByDay.entrySet().stream()
                .map(entry -> new DailyUserData(entry.getKey(), entry.getValue()))
                .toList();

        model.addAttribute("totalActiveUsers", totalActiveUsers);
        model.addAttribute("dailyUsers", dailyUsers);
        model.addAttribute("period", days);

        return "dashboard/active-users"; // Nome do template Thymeleaf
    }

    // Classe interna para o template
    public static class DailyUserData {
        private String date;
        private Long count;

        public DailyUserData(String date, Long count) {
            this.date = date;
            this.count = count;
        }

        public String getDate() { return date; }
        public Long getCount() { return count; }
    }


}
