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


}
