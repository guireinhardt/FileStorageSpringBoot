package com.secretaria.FileStorage.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityService {
    public List<String> getAllCities() {
        return List.of(
                "São Paulo", "Campinas", "Santos", "São José dos Campos",
                "Ribeirão Preto", "Sorocaba", "Bauru", "São Carlos"
                // Adicione mais cidades conforme necessário
        );
    }
}
