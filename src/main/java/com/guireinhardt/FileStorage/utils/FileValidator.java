package com.guireinhardt.FileStorage.utils;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.List;

public class FileValidator {

    // Lista de extensões permitidas
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", // imagens
            "pdf", // PDFs
            "doc", "docx", // arquivos do Word
            "ppt", "pptx", // arquivos do PowerPoint
            "xls", "xlsx", // arquivos do Excel
            "mp4", "avi", "mov", "mkv", "webm" // vídeos
    );

    public static boolean isValidFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }

        // Extrai a extensão do arquivo
        String fileExtension = getFileExtension(fileName);

        // Verifica se a extensão do arquivo está na lista de extensões permitidas
        return ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase());
    }

    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return ""; // Se não houver extensão, retorna uma string vazia
        }
        return fileName.substring(lastDot + 1); // Retorna a extensão após o ponto
    }
}
