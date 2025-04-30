package com.secretaria.FileStorage.dto;

public class FileResultDTO {
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
}
