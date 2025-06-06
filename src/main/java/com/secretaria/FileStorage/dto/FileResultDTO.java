package com.secretaria.FileStorage.dto;

import java.time.LocalDate;

public class FileResultDTO {
    private String id;
    private String name;
    private String fullPath;
    private LocalDate creationDate; // Novo campo


    public FileResultDTO(){}

    public FileResultDTO(String name, String fullPath) {
        this.name = name;
        this.fullPath = fullPath;
    }

    public FileResultDTO(String name, String fullPath, LocalDate creationDate) {
        this.name = name;
        this.fullPath = fullPath;
        this.creationDate = creationDate;
    }

    // Getters e setters
    public String getName() { return name; }
    public String getFullPath() { return fullPath; }
    public void setName(String name) { this.name = name; }
    public void setFullPath(String fullPath) { this.fullPath = fullPath; }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public String getRelativePath() {
        if (fullPath != null) {
            String normalized = fullPath.replace("\\", "/");
            int index = normalized.indexOf("uploads/");
            if (index != -1) {
                return normalized.substring(index);
            }
        }
        return "";
    }
    public String getPublicPath() {
        // Remove apenas o prefixo "uploads/"
        return fullPath.startsWith("uploads/") ? fullPath.substring("uploads/".length()) : fullPath;
    }

}
