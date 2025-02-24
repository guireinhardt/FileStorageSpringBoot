package com.secretaria.FileStorage.vo;

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
}
