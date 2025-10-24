package com.secretaria.FileStorage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secretaria.FileStorage.service.DashboardService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // Rota para acessar o relatório de acessos por dia
    @GetMapping("/dashboard/accesses-by-day")
    public String getAccessesByDay(@RequestParam(required = false) String period, Model model) throws Exception {
        LocalDate endDate = LocalDate.now();
        int days = period != null ? Integer.parseInt(period) : 7;
        LocalDate startDate = endDate.minusDays(days);

        // Obter o relatório de acessos por dia
        Map<String, Long> accessesByDay = dashboardService.getAccessesByDay(startDate, endDate);

        // Converte o Map para JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonAccessesByDay = objectMapper.writeValueAsString(accessesByDay);

        // Passa o JSON convertido para o modelo
        model.addAttribute("accessesByDay", jsonAccessesByDay);
        model.addAttribute("period", days);

        return "dashboard/accesses-by-day"; // Nome do template Thymeleaf
    }

    // Classe interna para encapsular os dados diários
    @Getter
    public static class DailyAccessData {
        private String date;
        private Long count;

        public DailyAccessData(String date, Long count) {
            this.date = date;
            this.count = count;
        }

    }
}



