package com.secretaria.FileStorage.vo;

public class FileResponseVO {
    private final String name;
    private final boolean isDirectory;
    private final String downloadUrl;
    private final String publicUrl;

    public FileResponseVO(String name, boolean isDirectory, String downloadUrl, String publicUrl) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.downloadUrl = downloadUrl;
        this.publicUrl = publicUrl;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getPublicUrl() {
        return publicUrl;
    }
}