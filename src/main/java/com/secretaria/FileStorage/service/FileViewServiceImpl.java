package com.secretaria.FileStorage.service;

import com.secretaria.FileStorage.config.FileStorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileViewServiceImpl implements FileViewService {

    private final FileStorageConfig fileStorageConfig;

    @Autowired
    public FileViewServiceImpl(FileStorageConfig fileStorageConfig) {
        this.fileStorageConfig = fileStorageConfig;
    }

    @Override
    public byte[] getFileContent(String fileName) throws IOException {
        Path filePath = Path.of(fileStorageConfig.getUploadDir(), fileName);
        return Files.readAllBytes(filePath);
    }

    @Override
    public String getContentType(String fileName) {
        if (fileName.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF_VALUE;
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG_VALUE;
        } else if (fileName.endsWith(".png")) {
            return MediaType.IMAGE_PNG_VALUE;
        } else if (fileName.endsWith(".txt")) {
            return MediaType.TEXT_PLAIN_VALUE;
        } else if (fileName.endsWith(".csv")) {
            return MediaType.TEXT_PLAIN_VALUE; // CSV é tratado como texto
        } else if (fileName.endsWith(".xls")) {
            return "application/vnd.ms-excel"; // Excel 97-2003
        } else if (fileName.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; // Excel 2007+
        } else if (fileName.endsWith(".mp4")) {
            return "video/mp4";
        } else if (fileName.endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (fileName.endsWith(".mov")) {
            return "video/quicktime";
        } else if (fileName.endsWith(".wmv")) {
            return "video/x-ms-wmv";
        }
        // Adicione outros tipos conforme necessário
        return MediaType.APPLICATION_OCTET_STREAM_VALUE; // Tipo padrão
    }
}

