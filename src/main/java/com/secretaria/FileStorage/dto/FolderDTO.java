package com.secretaria.FileStorage.dto;

import java.util.ArrayList;
import java.util.List;

public class FolderDTO {
    private String id;        // pode ser caminho ou UUID
    private String name;
    private String path;

    private List<FolderDTO> subFolders = new ArrayList<>();

    // construtores, getters e setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    public List<FolderDTO> getSubFolders() {
        return subFolders;
    }

    public void setSubFolders(List<FolderDTO> subFolders) {
        this.subFolders = subFolders;
    }
}

