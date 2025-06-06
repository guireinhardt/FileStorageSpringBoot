package com.secretaria.FileStorage.service;

import com.secretaria.FileStorage.config.FileStorageConfig;
import com.secretaria.FileStorage.dto.FileResultDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileListService {

    private final Path rootLocation;
    Logger logger;
    @Autowired
    public FileListService(FileStorageConfig fileStorageConfig) {
        this.rootLocation = fileStorageConfig.getUploadDirPath(); // Obtém o diretório de upload
    }

    public List<String> listFilesInDirectory(Path directoryPath) {
        try {
            // Log para verificar se o diretório está sendo acessado corretamente
            logger.info("Listando arquivos e pastas no diretório: " + directoryPath.toString());

            // Usando Files.list para listar arquivos no diretório
            return Files.list(directoryPath)
                    .map(Path::getFileName) // Obtém o nome do arquivo ou pasta
                    .map(Path::toString) // Converte para String
                    .collect(Collectors.toList()); // Coleta em uma lista

        } catch (IOException e) {
            // Log do erro para depuração
            logger.info("Erro ao listar arquivos no diretório: " + directoryPath);
            return List.of(); // Retorna uma lista vazia em caso de erro
        }
    }


    public Path getRootLocation() {
        return rootLocation; // Retorna o diretório raiz
    }





}