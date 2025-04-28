package com.secretaria.FileStorage.service;

import com.secretaria.FileStorage.config.FileStorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileSearchService {

    private final String fileStorageLocation;


    public FileSearchService(FileStorageConfig fileStorageConfig) {
        this.fileStorageLocation = fileStorageConfig.getUploadDir();
    }

    public List<String> searchFiles(String query) throws IOException {
        String lowerCaseQuery = query.toLowerCase();

        try (Stream<Path> paths = Files.walk(Paths.get(fileStorageLocation))) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase();

                        // Verifica se o nome do arquivo contém a consulta
                        return fileName.contains(lowerCaseQuery);
                    })
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }

    public List<String> searchMediaFiles(String query) throws IOException {
        // Diretório onde as imagens e vídeos estão armazenados
        Path storageDir = Path.of(fileStorageLocation);

        // Buscar todos os arquivos de mídia e filtrar com base na consulta
        try (Stream<Path> paths = Files.list(storageDir)) {
            return paths
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(fileName -> fileName.toLowerCase().contains(query.toLowerCase())) // Filtra arquivos que contêm a consulta
                    .filter(this::isMediaFile) // Filtra apenas arquivos de mídia
                    .collect(Collectors.toList());
        }
    }
    // Método para buscar todos os arquivos de mídia
    // Método para buscar todos os arquivos de mídia no diretório fileStorageLocation
    public List<String> getAllMediaFiles() throws IOException {
        // Diretório onde as imagens e vídeos estão armazenados
        Path storageDir = Path.of(fileStorageLocation); // Usando diretamente o fileStorageLocation

        // Buscar todos os arquivos de mídia
        try (Stream<Path> paths = Files.list(storageDir)) {
            return paths
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(this::isMediaFile) // Filtra apenas arquivos de mídia
                    .collect(Collectors.toList());
        }
    }

    private boolean isMediaFile(String fileName) {
        Set<String> mediaExtensions = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "mp4", "avi", "mov", "wmv", "flv", "mkv");
        String fileExtension = getFileExtension(fileName).toLowerCase();
        return mediaExtensions.contains(fileExtension);
    }

    private String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot == -1 || lastIndexOfDot == 0) {
            return ""; // Sem extensão
        }
        return fileName.substring(lastIndexOfDot + 1);
    }
    public List<String> searchFilesWithFilters(String query, String date) throws IOException {
        String lowerCaseQuery = query.toLowerCase();
        String normalizedDate = (date != null) ? date.trim() : "";

        try (Stream<Path> paths = Files.walk(Paths.get(fileStorageLocation))) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase();

                        // Verifica se o nome do arquivo contém a consulta
                        boolean matchesQuery = fileName.contains(lowerCaseQuery);

                        // Verifica se o nome do arquivo contém a data, se foi fornecida
                        boolean matchesDate = true;
                        if (!normalizedDate.isEmpty()) {
                            matchesDate = fileName.contains(normalizedDate);
                        }

                        return matchesQuery && matchesDate;
                    })
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }


    /**
     * Método auxiliar para extrair a data do nome do arquivo.
     * Procura 8 dígitos seguidos (ex: 20250428) no nome do arquivo.
     */
    private String extractDateFromFileName(String fileName) {
        String onlyNumbers = fileName.replaceAll("[^0-9]", "");
        if (onlyNumbers.length() >= 8) {
            return onlyNumbers.substring(0, 8); // Pega os primeiros 8 dígitos
        }
        return null;
    }




}
