package com.guireinhardt.FileStorage.utils;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.net.URI;
import java.net.URL;

public class DeletableInputStreamResource implements Resource {
    private final File file;
    private final InputStream inputStream;

    public DeletableInputStreamResource(File file) throws IOException {
        this.file = file;
        this.inputStream = new FileInputStream(file);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FilterInputStream(inputStream) {
            @Override
            public void close() throws IOException {
                super.close();
                if (file.exists()) {
                    file.delete(); // Apaga o arquivo temporário
                }
            }
        };
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public String getFilename() {
        return file.getName();
    }

    @Override
    public long contentLength() throws IOException {
        return file.length();
    }

    @Override
    public String getDescription() {
        return "Deletable resource for file: " + file.getAbsolutePath();
    }

    // Métodos abaixo apenas para compilar corretamente
    @Override public boolean isReadable() { return true; }
    @Override public boolean isOpen() { return true; }
    @Override public URL getURL() throws IOException { return file.toURI().toURL(); }
    @Override public URI getURI() throws IOException { return file.toURI(); }
    @Override public File getFile() throws IOException { return file; }
    @Override public long lastModified() throws IOException { return file.lastModified(); }
    @Override public Resource createRelative(String relativePath) throws IOException { throw new UnsupportedOperationException(); }
}
