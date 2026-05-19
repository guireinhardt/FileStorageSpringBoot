package com.guireinhardt.FileStorage.vo;

public class FileResponseVO {
    private final String name;
    private final boolean isDirectory;
    private final String downloadUrl;
    private final String publicUrl;
    private String iconClass;

    public FileResponseVO(String name, boolean isDirectory, String downloadUrl, String publicUrl) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.downloadUrl = downloadUrl;
        this.publicUrl = publicUrl;
        this.iconClass = resolveIconClass(name);  // Definindo o ícone automaticamente
    }

    // Método que resolve o ícone com base no nome do arquivo
    private String resolveIconClass(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".pdf")) return "fa-file-pdf pdf";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")) return "fa-file-image image";
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return "fa-file-word doc";
        if (lower.endsWith(".zip") || lower.endsWith(".rar")) return "fa-file-archive zip";
        return "fa-file";  // Default para arquivos genéricos
    }

    // Getters
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

    public String getIconClass() {
        return iconClass;
    }
}
