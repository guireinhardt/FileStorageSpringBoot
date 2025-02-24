package com.secretaria.FileStorage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

@ConfigurationProperties(prefix = "file")

public class FileStorageConfig {

    private String uploadDir;

    public Path getUploadDirPath() {
        return Paths.get(uploadDir);
    }
    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}
