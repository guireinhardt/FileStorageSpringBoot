package com.secretaria.FileStorage.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeywordService {
    private final Map<String, List<String>> keywordMap = new HashMap<>();
    private final Map<String, String> displayMap = new HashMap<>();

    @PostConstruct
    public void init() {
        keywordMap.put("maquinas", List.of("Tratores","Colheitadeira"));
        keywordMap.put("plantacao", List.of("Cafézal","Cacauau"));
        keywordMap.put("", List.of("",""));
        keywordMap.put("", List.of("",""));
        keywordMap.put("", List.of("",""));
        keywordMap.put("", List.of("",""));
        keywordMap.put("", List.of("",""));

        //mapeamento da chave normalizada para nome com acento para a exibição

        displayMap.put("maquinas", "Máquinas");
        displayMap.put("plantacao", "Plantação");

    }
    // Getters para expor os mapas, se quiser
    public Map<String, List<String>> getKeywordMap() {
        return keywordMap;
    }

    public Map<String, String> getDisplayMap() {
        return displayMap;
    }
}
