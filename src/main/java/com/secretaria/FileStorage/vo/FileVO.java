package com.secretaria.FileStorage.vo;

import java.util.List;

public class FileVO {

    public static class FileValueObject {
        private final String name;
        private final boolean isDirectory;

        public FileValueObject(String name, boolean isDirectory) {
            this.name = name;
            this.isDirectory = isDirectory;
        }

        public String getName() {
            return name;
        }

        public boolean isDirectory() {
            return isDirectory;
        }
    }

    // Método para criar uma lista de FileValueObject a partir de uma lista de arquivos
    public static List<FileValueObject> fromFileList(List<java.io.File> files) {
        return files.stream()
                .map(file -> new FileValueObject(file.getName(), file.isDirectory()))
                .toList();
    }
}