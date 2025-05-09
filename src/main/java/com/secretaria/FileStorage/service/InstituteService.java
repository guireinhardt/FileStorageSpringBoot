package com.secretaria.FileStorage.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstituteService {
    public List<String> getAllInstitutes() {
        return List.of(
                "APTA REGIONAL",
                "IAC",
                "IB",
                "IEA",
                "IP",
                "ITAL",
                "IZ"
        );
    }
}

