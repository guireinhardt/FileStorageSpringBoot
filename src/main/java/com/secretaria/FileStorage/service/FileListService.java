package com.secretaria.FileStorage.service;

import com.secretaria.FileStorage.config.FileStorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileListService {

    private final Path rootLocation;

    @Autowired
    public FileListService(FileStorageConfig fileStorageConfig) {
        this.rootLocation = fileStorageConfig.getUploadDirPath(); // Obtém o diretório de upload
    }

    // Método atualizado para aceitar um Path
    public List<String> listFilesInDirectory(Path directoryPath) {
        try {
            return Files.list(directoryPath) // Lista os arquivos e pastas no diretório
                    .map(Path::getFileName) // Obtém o nome do arquivo ou pasta
                    .map(Path::toString) // Converte para String
                    .collect(Collectors.toList()); // Coleta em uma lista
        } catch (Exception e) {
            // Log de erro
            return List.of(); // Retorna uma lista vazia em caso de erro
        }
    }

    public Path getRootLocation() {
        return rootLocation; // Retorna o diretório raiz
    }
}