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
    private final EncryptionService encryptionService;

    @Autowired
    public FileViewServiceImpl(FileStorageConfig fileStorageConfig) throws Exception {
        this.fileStorageConfig = fileStorageConfig;
        this.encryptionService = new EncryptionService(); // Inicializa o serviço de criptografia
    }

    @Override
    public byte[] getFileContent(String encryptedFileName) throws IOException {
        try {
            // Descriptografa o nome do arquivo
            String fileName = encryptionService.decrypt(encryptedFileName);
            Path filePath = Path.of(fileStorageConfig.getUploadDir(), fileName);
            return Files.readAllBytes(filePath);
        } catch (Exception e) {
            throw new IOException("Erro ao descriptografar o nome do arquivo", e);
        }
    }

    @Override
    public String getContentType(String fileName) {
        String fileExtension = getFileExtension(fileName).toLowerCase();
        switch (fileExtension) {
            case "pdf":
                return MediaType.APPLICATION_PDF_VALUE;
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG_VALUE;
            case "png":
                return MediaType.IMAGE_PNG_VALUE;
            case "gif":
                return MediaType.IMAGE_GIF_VALUE; // Suporte para GIFs
            case "txt":
                return MediaType.TEXT_PLAIN_VALUE; // Arquivos de texto
            case "html":
                return MediaType.TEXT_HTML_VALUE; // Arquivos HTML
            case "mp4":
                return "video/mp4"; // Tipo de mídia para MP4
            case "webm":
                return "video/webm"; // Tipo de mídia para WebM
            case "ogg":
                return "video/ogg"; // Tipo de mídia para OGG
            case "avi":
                return "video/x-msvideo"; // Tipo de mídia para AVI
            case "mov":
                return "video/quicktime"; // Tipo de mídia para MOV
            case "xls":
                return "application/vnd.ms-excel"; // Tipo de mídia para XLS
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; // Tipo de mídia para XLSX
            case "ppt":
                return "application/vnd.ms-powerpoint"; // Tipo de mídia para PPT
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation"; // Tipo de mídia para PPTX
            case "csv":
                return "text/csv"; // Arquivos CSV
            default:
                return MediaType.APPLICATION_OCTET_STREAM_VALUE; // Fallback
        }
    }

    private String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot == -1 || lastIndexOfDot == 0) {
            return ""; // Sem extensão
        }
        return fileName.substring(lastIndexOfDot + 1);
    }


    // Método para criptografar o nome do arquivo
    public String encryptFileName(String fileName) throws Exception {
        return encryptionService.encrypt(fileName);
    }
}