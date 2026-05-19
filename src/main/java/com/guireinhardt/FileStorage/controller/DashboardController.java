package com.guireinhardt.FileStorage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guireinhardt.FileStorage.service.DashboardService;
import com.guireinhardt.FileStorage.dto.ChartDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // Rota para acessar o relatório de acessos por dia
    @GetMapping("/dashboard/accesses-by-day")
    public String getAccessesByDay(@RequestParam(required = false) Integer period, Model model) throws Exception {
        int days = (period == null || period <= 0) ? 7 : period;
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1); // Inclui o dia final

        // Chama o método do service para pegar os dados de acessos por dia
        ChartDTO chartDto = dashboardService.getAccessesByDay(start, end);

        // Converte os dados para JSON e passa para o modelo Thymeleaf
        ObjectMapper objectMapper = new ObjectMapper();
        model.addAttribute("chartJson", objectMapper.writeValueAsString(chartDto));  // Dados para o frontend
        model.addAttribute("period", days);  // Período (ex: 7, 30, 60 dias)

        return "dashboard/accesses-by-day";  // Retorna o template Thymeleaf
    }

    // Endpoint para API (JSON) que responde os dados quando requisitado por fetch
    @GetMapping("/api/dashboard/accesses-by-day")
    @ResponseBody
    public ChartDTO getAccessesByDayApi(@RequestParam Integer period) {
        int days = (period == null || period <= 0) ? 7 : period;
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1);
        return dashboardService.getAccessesByDay(start, end);  // Retorna os dados em formato JSON
    }

    // No seu controller
    @GetMapping("/dashboard/export-accesses")
    public ResponseEntity<Resource> exportAccesses(@RequestParam Integer period) {
        int days = (period == null || period <= 0) ? 7 : period;
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1); // Inclui o dia final

        // Chama o serviço para pegar os dados de acessos
        ChartDTO chartDto = dashboardService.getAccessesByDay(start, end);

        // Criação do CSV com os dados
        String csv = "Data,Acessos\n";
        for (int i = 0; i < chartDto.getLabels().size(); i++) {
            csv += chartDto.getLabels().get(i) + "," + chartDto.getCounts().get(i) + "\n";
        }

        // Criação do recurso (arquivo CSV)
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(csv.getBytes()));

        // Definir o nome do arquivo para download
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=acessos.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    // Página (HTML) — relatório de palavras mais buscadas
    @GetMapping("/dashboard/search-count-by-keyword")
    public String getSearchCountByKeyword(@RequestParam(required = false) Integer period, Model model) {
        int days = (period == null || period <= 0) ? 7 : period;
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1);

        // Agora estamos chamando o método correto no service
        Map<String, Long> searchCountByKeyword = dashboardService.getSearchCountByKeyword(start, end);

        model.addAttribute("searchCountByKeyword", searchCountByKeyword);
        model.addAttribute("period", days);
        return "dashboard/search-count-by-keyword";  // Retorna o template Thymeleaf
    }


    @GetMapping("/api/dashboard/search-count-by-keyword")
    @ResponseBody
    public Map<String, Long> getSearchCountByKeywordApi(@RequestParam(required = false) Integer period) {
        int days = (period == null || period <= 0) ? 7 : period;
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1);
        return dashboardService.getSearchCountByKeyword(start, end);
    }

    @GetMapping("/dashboard/export-search-count")
    public ResponseEntity<Resource> exportSearchCount(@RequestParam(required = false) Integer period) {
        int days = (period == null || period <= 0) ? 7 : period;
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1);

        Map<String, Long> data = dashboardService.getSearchCountByKeyword(start, end);

        // Criação do CSV
        StringBuilder csv = new StringBuilder("Termo,Quantidade de buscas\n");
        data.forEach((k, v) -> csv.append(k).append(",").append(v).append("\n"));

        // Criação do recurso (arquivo CSV)
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(csv.toString().getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=search_count.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }
    // Controller
    @GetMapping("/dashboard")
    public String getDashboard(@RequestParam(required = false) Integer period, Model model) throws Exception {
        int days = (period == null || period <= 0) ? 7 : period;
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1); // Inclui o dia final

        // Coleta os dados
        ChartDTO chartDtoAccesses = dashboardService.getAccessesByDay(start, end);
        Map<String, Long> searchCountByKeyword = dashboardService.getSearchCountByKeyword(start, end);

        // Log para verificar dados no backend
        System.out.println(chartDtoAccesses);
        System.out.println(searchCountByKeyword);

        // Passa os dados para o modelo
        ObjectMapper objectMapper = new ObjectMapper();
        model.addAttribute("chartJsonAccesses", objectMapper.writeValueAsString(chartDtoAccesses));
        model.addAttribute("searchCountByKeyword", searchCountByKeyword);
        model.addAttribute("period", days);

        return "dashboard/dashboard";  // Retorna o template Thymeleaf
    }




    @GetMapping("/dashboard/export")
    public ResponseEntity<Resource> exportDashboardData(@RequestParam(required = false) Integer period) {
        int days = (period == null || period <= 0) ? 7 : period;
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1); // Inclui o dia final

        // Coleta os dados de acessos por dia
        ChartDTO chartDtoAccesses = dashboardService.getAccessesByDay(start, end);

        // Coleta os dados de palavras mais buscadas
        Map<String, Long> searchCountByKeyword = dashboardService.getSearchCountByKeyword(start, end);

        // Criação do CSV para acessos
        StringBuilder csv = new StringBuilder("Data,Acessos\n");
        for (int i = 0; i < chartDtoAccesses.getLabels().size(); i++) {
            csv.append(chartDtoAccesses.getLabels().get(i)).append(",").append(chartDtoAccesses.getCounts().get(i)).append("\n");
        }

        // Criação do CSV para palavras buscadas
        csv.append("\nTermo,Quantidade de buscas\n");
        searchCountByKeyword.forEach((k, v) -> csv.append(k).append(",").append(v).append("\n"));

        // Criação do recurso (arquivo CSV)
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(csv.toString().getBytes()));

        // Definir o nome do arquivo para download
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dashboard_data.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }



    // --- Helpers ---

    private Map<String, Long> nonNullMap(Map<String, Long> map) {
        return map != null ? map : Map.of();
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        boolean needQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String escaped = s.replace("\"", "\"\""); // Escape quotes
        return needQuotes ? "\"" + escaped + "\"" : escaped;
    }
}





