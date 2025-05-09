package com.secretaria.FileStorage.vo;

import java.util.List;

public class FileVO {

    public static class FileValueObject {
        private final String name;
        private final boolean isDirectory;
        private final String relativePath; // novo campo

        public FileValueObject(String name, boolean isDirectory, String relativePath) {
            this.name = name;
            this.isDirectory = isDirectory;
            this.relativePath = relativePath;
        }

        public String getName() {
            return name;
        }

        public boolean isDirectory() {
            return isDirectory;
        }

        public String getRelativePath() {
            return relativePath;
        }
    }


    // Método para criar uma lista de FileValueObject a partir de uma lista de arquivos
    public static List<FileValueObject> fromFileList(List<java.io.File> files, String basePath) {
        return files.stream()
                .map(file -> {
                    String absolutePath = file.getAbsolutePath().replace("\\", "/");
                    String relative = absolutePath.replace(basePath.replace("\\", "/") + "/", "");
                    return new FileValueObject(file.getName(), file.isDirectory(), relative);
                })
                .toList();
    }

}