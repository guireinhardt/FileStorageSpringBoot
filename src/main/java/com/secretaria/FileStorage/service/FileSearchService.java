package com.secretaria.FileStorage.service;

import com.secretaria.FileStorage.config.FileStorageConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileSearchService {

    private final String fileStorageLocation;

    public FileSearchService(FileStorageConfig fileStorageConfig) {
        this.fileStorageLocation = fileStorageConfig.getUploadDir();
    }

    public List<String> searchFiles(String query) throws IOException {
        // Converte a consulta em minúsculas para comparação
        String lowerCaseQuery = query.toLowerCase();

        // Usa Files.walk para percorrer o diretório e suas subpastas
        try (Stream<Path> paths = Files.walk(Paths.get(fileStorageLocation))) {
            return paths
                    .filter(Files::isRegularFile) // Filtra apenas arquivos regulares
                    .filter(path -> path.getFileName().toString().toLowerCase().contains(lowerCaseQuery)) // Filtra com base na consulta
                    .map(Path::getFileName) // Obtém apenas o nome do arquivo
                    .map(Path::toString) // Converte para String
                    .collect(Collectors.toList());
        }
    }
}