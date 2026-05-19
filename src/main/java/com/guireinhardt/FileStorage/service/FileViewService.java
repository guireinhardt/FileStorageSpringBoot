package com.guireinhardt.FileStorage.service;

import java.io.IOException;

public interface FileViewService {
    byte[] getFileContent(String fileName) throws IOException;
    String getContentType(String fileName);
}
