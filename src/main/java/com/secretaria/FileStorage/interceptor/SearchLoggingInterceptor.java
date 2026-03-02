package com.secretaria.FileStorage.interceptor;

import com.secretaria.FileStorage.infra.security.TokenService;
import com.secretaria.FileStorage.service.SearchLoggingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class SearchLoggingInterceptor implements HandlerInterceptor {

    private final SearchLoggingService searchLoggingService;
    private final TokenService tokenService;

    public SearchLoggingInterceptor(SearchLoggingService searchLoggingService, TokenService tokenService) {
        this.searchLoggingService = searchLoggingService;
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        String uri = req.getRequestURI();
        boolean isSearch = "GET".equalsIgnoreCase(req.getMethod())
                && (uri.equals("/search") || uri.startsWith("/banco-de-imagens/search"));

        if (!isSearch) return true;

        String query     = nvl(req.getParameter("query"));
        String keyword   = nvl(req.getParameter("keyword"));
        String institute = nvl(req.getParameter("institute"));
        String city      = nvl(req.getParameter("city"));
        String startDate = nvl(req.getParameter("startDate"));
        String endDate   = nvl(req.getParameter("endDate"));

        String[] subsArr = req.getParameterValues("subkeywords");
        List<String> subkeywords = (subsArr == null) ? List.of() : Arrays.stream(subsArr)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .toList();

        boolean hasFilters =
                !query.isBlank() || !keyword.isBlank() || !institute.isBlank() || !city.isBlank()
                        || !subkeywords.isEmpty() || !startDate.isBlank() || !endDate.isBlank();

        if (!hasFilters) return true; // não loga abertura em branco

        String username = extractUsername(req); // pode ser null
        String composedQuery = composeQuery(query, keyword, subkeywords, institute, city, startDate, endDate);

        try {
            // AGORA: username, keyword, subkeywords, query
            searchLoggingService.log(username, keyword, subkeywords, composedQuery);
        } catch (Exception ignored) {}

        return true;
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    private String extractUsername(HttpServletRequest req) {
        if (req.getCookies() == null) return null;
        return Arrays.stream(req.getCookies())
                .filter(c -> "authToken".equals(c.getName()))
                .map(c -> tokenService.validateToken(c.getValue()))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    private String composeQuery(String q, String kw, List<String> subs,
                                String inst, String city, String start, String end) {
        StringBuilder sb = new StringBuilder();
        if (!q.isBlank())     sb.append("q=").append(q).append(" ");
        if (!kw.isBlank())    sb.append("kw=").append(kw).append(" ");
        if (!subs.isEmpty())  sb.append("subs=").append(String.join("|", subs)).append(" ");
        if (!inst.isBlank())  sb.append("inst=").append(inst).append(" ");
        if (!city.isBlank())  sb.append("city=").append(city).append(" ");
        if (!start.isBlank()) sb.append("from=").append(start).append(" ");
        if (!end.isBlank())   sb.append("to=").append(end);
        return sb.toString().trim();
    }
}


