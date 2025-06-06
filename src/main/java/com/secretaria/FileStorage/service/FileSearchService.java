package com.secretaria.FileStorage.service;

import com.secretaria.FileStorage.config.FileStorageConfig;
import com.secretaria.FileStorage.dto.FileResultDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class FileSearchService {

    private final String fileStorageLocation;


    public FileSearchService(FileStorageConfig fileStorageConfig) {
        this.fileStorageLocation = fileStorageConfig.getUploadDir();
    }

    public List<FileResultDTO> searchFiles(
            String query,     // texto livre, pode ser nome do arquivo ou palavras
            String keyword,   // palavra-chave (ex: campo específico)
            String institute,
            String city,
            LocalDate startDate,
            LocalDate endDate) throws IOException {

        List<String> queryTerms = (query == null || query.isBlank()) ?
                Collections.emptyList() :
                Arrays.stream(query.toLowerCase().split("\\s+"))
                        .filter(term -> !term.isBlank())
                        .collect(Collectors.toList());

        try (Stream<Path> paths = Files.walk(Paths.get(fileStorageLocation))) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.toString().toLowerCase().contains("lixeira"))
                    .filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase();

                        // Busca por query/texto livre no nome do arquivo
                        if (!queryTerms.isEmpty() &&
                                queryTerms.stream().anyMatch(term -> !fileName.contains(term))) {
                            return false;
                        }

                        // Busca pela palavra-chave (keyword)
                        if (keyword != null && !keyword.isBlank() && !fileName.contains(keyword.toLowerCase())) {
                            return false;
                        }

                        // Busca pelo instituto
                        if (institute != null && !institute.isBlank() && !fileName.contains(institute.toLowerCase())) {
                            return false;
                        }

                        // Busca pela cidade
                        if (city != null && !city.isBlank() && !fileName.contains(city.toLowerCase())) {
                            return false;
                        }

                        // Busca pela data no começo do nome do arquivo
                        if (startDate != null || endDate != null) {
                            try {
                                String fileNameOriginal = path.getFileName().toString();
                                // Extrai os números do início para tentar pegar a data
                                String prefix = fileNameOriginal.replaceAll("[^0-9]", "");
                                LocalDate fileDate = null;

                                if (prefix.length() >= 8) {
                                    fileDate = LocalDate.parse(prefix.substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd"));
                                } else if (prefix.length() >= 6) {
                                    fileDate = LocalDate.parse(prefix.substring(0, 6), DateTimeFormatter.ofPattern("yyMMdd"));
                                } else {
                                    return false;
                                }

                                if ((startDate != null && fileDate.isBefore(startDate)) ||
                                        (endDate != null && fileDate.isAfter(endDate))) {
                                    return false;
                                }

                            } catch (Exception e) {
                                // Se não conseguir extrair data, descarta o arquivo quando filtro de data existe
                                return false;
                            }
                        }

                        return true;
                    })
                    .map(path -> new FileResultDTO(
                            path.getFileName().toString(),
                            path.toAbsolutePath().toString()
                    ))
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
    private LocalDate extractDateFromFilename(String filename) {
        Pattern pattern = Pattern.compile("(\\d{2,4})[-/](\\d{2})[-/](\\d{2})");
        Matcher matcher = pattern.matcher(filename);

        if (matcher.find()) {
            String yearPart = matcher.group(1);
            String monthPart = matcher.group(2);
            String dayPart = matcher.group(3);

            try {
                int year = Integer.parseInt(yearPart);
                int month = Integer.parseInt(monthPart);
                int day = Integer.parseInt(dayPart);

                if (year < 100) {
                    year += 2000;
                }

                LocalDate result = LocalDate.of(year, month, day);
                System.out.println("✔ Data extraída do nome do arquivo '" + filename + "': " + result);
                return result;

            } catch (Exception e) {
                System.out.println("❌ Erro ao processar data no arquivo: " + filename + " - " + e.getMessage());
                return null;
            }
        } else {
            System.out.println("⚠️ Nenhuma data encontrada no nome do arquivo: " + filename);
        }

        return null;
    }
    public List<FileResultDTO> searchFilesByFolders(String query, String city, List<String> folders) throws IOException {
        if (folders == null || folders.isEmpty()) {
            return Collections.emptyList();
        }

        List<FileResultDTO> result = new ArrayList<>();

        for (String folderName : folders) {
            Path folder = Paths.get(folderName);  // monta caminho completo

            if (!Files.exists(folder) || !Files.isDirectory(folder)) {
                System.out.println("Pasta inválida ou não encontrada: " + folder);
                continue;
            }

            try (Stream<Path> paths = Files.walk(folder)) {
                List<FileResultDTO> filesInFolder = paths
                        .filter(Files::isRegularFile)
                        .filter(path -> !path.toString().toLowerCase().contains("lixeira"))
                        .filter(path -> {
                            String fileName = path.getFileName().toString().toLowerCase();

                            if (query != null && !query.isBlank() && !fileName.contains(query.toLowerCase())) {
                                return false;
                            }
                            if (city != null && !city.isBlank() && !fileName.contains(city.toLowerCase())) {
                                return false;
                            }

                            return true;
                        })
                        .map(path -> new FileResultDTO(path.getFileName().toString(), path.toAbsolutePath().toString()))
                        .collect(Collectors.toList());

                result.addAll(filesInFolder);
            }
        }

        return result;
    }
    public List<FileResultDTO> searchFilesWithinRoot(String query, String keyword, List<String> subkeywords, Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        // Aqui pode buscar a data de criação, se quiser
                        LocalDate creationDate = null;
                        try {
                            creationDate = Files.getLastModifiedTime(path)
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();
                        } catch (IOException e) {
                            // ignorar erro de data
                        }
                        return new FileResultDTO(path.getFileName().toString(), path.toString(), creationDate);
                    })
                    .filter(file -> {
                        String lowerName = file.getName().toLowerCase();

                        boolean matchesQuery = (query == null || query.isBlank()) || lowerName.contains(query.toLowerCase());
                        boolean matchesKeyword = (keyword == null || keyword.isBlank()) || lowerName.contains(keyword.toLowerCase());
                        boolean matchesSubkeywords = (subkeywords == null || subkeywords.isEmpty()) ||
                                subkeywords.stream()
                                        .map(String::toLowerCase)
                                        .anyMatch(lowerName::contains);

                        return matchesQuery && matchesKeyword && matchesSubkeywords;
                    })
                    .collect(Collectors.toList());
        }
    }
}











