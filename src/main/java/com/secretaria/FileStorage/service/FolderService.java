package com.secretaria.FileStorage.service;
import com.secretaria.FileStorage.dto.FileResultDTO;
import com.secretaria.FileStorage.dto.FolderDTO;
import com.secretaria.FileStorage.service.CityService;
import com.secretaria.FileStorage.service.InstituteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FolderService {

    @Value("${file.upload-dir}")
    private String basePath; // usa basePath como diretório base
    private final InstituteService instituteService;
    private final CityService cityService;

    // Injeção via construtor (recomendado)
    public FolderService(InstituteService instituteService, CityService cityService) {
        this.instituteService = instituteService;
        this.cityService = cityService;
    }

    public List<FolderDTO> getFolderTree() {
        File baseDir = new File(basePath);
        List<FolderDTO> folders = new ArrayList<>();
        if (baseDir.exists() && baseDir.isDirectory()) {
            for (File folder : baseDir.listFiles(File::isDirectory)) {
                folders.add(buildFolderDTO(folder));
            }
        }
        return folders;
    }

    private FolderDTO buildFolderDTO(File folder) {
        FolderDTO dto = new FolderDTO();
        dto.setId(folder.getAbsolutePath());
        dto.setName(folder.getName());
        dto.setPath(folder.getAbsolutePath());

        File[] subfolders = folder.listFiles(File::isDirectory);
        if (subfolders != null) {
            for (File sub : subfolders) {
                dto.getSubFolders().add(buildFolderDTO(sub));
            }
        }
        return dto;
    }

    public List<FileResultDTO> getFilesInFolder(String folderPath) {
        File folder = new File(folderPath);

        List<FileResultDTO> files = new ArrayList<>();

        if (folder.exists() && folder.isDirectory()) {
            File[] listFiles = folder.listFiles(File::isFile);
            if (listFiles != null) {
                for (File file : listFiles) {
                    FileResultDTO dto = new FileResultDTO();
                    dto.setName(file.getName());
                    dto.setFullPath(file.getAbsolutePath());
                    // Pode adicionar data de criação, cidade, etc., se tiver essa lógica
                    files.add(dto);
                }
            }
        }

        return files;
    }

    /**
     * Método recursivo para explorar arquivos a partir do basePath
     */
    private List<FileResultDTO> explorePath(String path) {
        List<FileResultDTO> result = new ArrayList<>();
        File current = new File(path);

        if (current.exists()) {
            if (current.isFile()) {
                FileResultDTO dto = new FileResultDTO();
                dto.setName(current.getName());
                dto.setFullPath(current.getAbsolutePath());
                // Aqui pode buscar a data de criação, cidade, etc.
                result.add(dto);
            } else if (current.isDirectory()) {
                File[] filesAndFolders = current.listFiles();
                if (filesAndFolders != null) {
                    for (File f : filesAndFolders) {
                        result.addAll(explorePath(f.getAbsolutePath()));
                    }
                }
            }
        }

        return result;
    }

    /**
     * Busca avançada com vários filtros, usando o basePath corretamente
     */
    public List<FileResultDTO> advancedSearchFiles(String keyword, String city, String institute, String type,
                                                   LocalDate startDate, LocalDate endDate) {
        List<FileResultDTO> allFiles = explorePath(basePath);

        // listas fixas das cidades e institutos válidos (você pode usar o serviço para pegar isso)
        List<String> validCities = cityService.getAllCities();
        List<String> validInstitutes = instituteService.getAllInstitutes();

        return allFiles.stream()
                .filter(file -> keyword == null || keyword.isBlank() || file.getName().toLowerCase().contains(keyword.toLowerCase()))

                // para o filtro de cidade, só aceita se for válido OU vazio (sem filtro)
                .filter(file -> city == null || city.isBlank() || validCities.contains(city))

                // para o filtro de instituto, mesmo esquema, aceita só se válido
                .filter(file -> institute == null || institute.isBlank() || validInstitutes.contains(institute))

                .filter(file -> type == null || type.isBlank() || file.getName().toLowerCase().endsWith(type.toLowerCase()))
                .filter(file -> {
                    if (startDate != null && file.getCreationDate() != null) {
                        return !file.getCreationDate().isBefore(startDate);
                    }
                    return true;
                })
                .filter(file -> {
                    if (endDate != null && file.getCreationDate() != null) {
                        return !file.getCreationDate().isAfter(endDate);
                    }
                    return true;
                })

                .collect(Collectors.toList());
    }

}
