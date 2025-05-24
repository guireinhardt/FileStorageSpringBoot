package com.secretaria.FileStorage.service;

import com.secretaria.FileStorage.dto.FileResultDTO;
import com.secretaria.FileStorage.dto.FolderDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FolderService {
    @Value("${file.upload-dir}")
    private String basePath;

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
        dto.setId(folder.getAbsolutePath());  // usar caminho absoluto como id
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
                    //dto.setId(UUID.randomUUID().toString());  // ou file.getAbsolutePath() se preferir
                    dto.setName(file.getName());
                    dto.setFullPath(file.getAbsolutePath());
                    // Não altera nada mais para não quebrar código existente

                    files.add(dto);
                }
            }
        }

        return files;
    }




}
