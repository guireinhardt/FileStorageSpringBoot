package com.secretaria.FileStorage.dto;

public class FileResultDTO {
    private String id;
    private String name;
    private String fullPath;

    public FileResultDTO(String name, String fullPath) {
        this.name = name;
        this.fullPath = fullPath;
    }

    public String getName() {
        return name;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }
    // ✅ Adicione este getter para uso no Thymeleaf
    public String getRelativePath() {
        if (fullPath != null) {
            // Normaliza separadores e extrai a parte relativa a partir de "uploads/"
            String normalized = fullPath.replace("\\", "/");
            int index = normalized.indexOf("uploads/");
            if (index != -1) {
                return normalized.substring(index);
            }
        }
        return "";
    }


}
