package com.guireinhardt.FileStorage.controller;

import com.guireinhardt.FileStorage.entity.FileEntity;
import com.guireinhardt.FileStorage.entity.FileVisibility;
import com.guireinhardt.FileStorage.repository.FileRepository;
import com.guireinhardt.FileStorage.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
public class FileDownloadController {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // 🔓 Público: só libera se pasta for pública + arquivo PUBLIC
    @GetMapping("/public/files/{id}/download")
    public ResponseEntity<Resource> downloadPublic(@PathVariable UUID id) {
        FileEntity file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Arquivo não encontrado"));

        if (!file.getFolder().isPublic() || file.getVisibility() != FileVisibility.PUBLIC) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return buildDownloadResponse(file);
    }

    // 🔐 Interno: precisa login (e depois você coloca RBAC por pasta)
    @GetMapping("/files/{id}/download")
    public ResponseEntity<Resource> downloadInternal(@PathVariable UUID id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        FileEntity file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Arquivo não encontrado"));

        return buildDownloadResponse(file);
    }

    private ResponseEntity<Resource> buildDownloadResponse(FileEntity file) {
        Resource resource = fileStorageService.loadFileAsResource(file.getStorageKey());

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(file.getContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getOriginalName() + "\"")
                .body(resource);
    }
}