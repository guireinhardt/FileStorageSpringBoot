package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.service.FileStorageService;
import com.secretaria.FileStorage.vo.UploadFileResponseVO;
import com.secretaria.FileStorage.service.FileListService;
import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/storage")
public class FileStorageController {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileListService fileListService;

    @PostMapping("/uploadFiles")
    public String uploadFiles(@RequestParam(value = "file", required = false) MultipartFile file,
                              @RequestParam(value = "files", required = false) MultipartFile[] files) throws Exception {
        String message;

        // Adicione logs para verificar o que está sendo recebido
        if (file != null) {
            logger.info("Arquivo único recebido: " + file.getOriginalFilename());
        } else {
            logger.info("Nenhum arquivo único recebido.");
        }

        if (files != null && files.length > 0) {
            logger.info("Múltiplos arquivos recebidos: " + files.length);
        } else {
            logger.info("Nenhum arquivo múltiplo recebido.");
        }

        if (file != null && !file.isEmpty()) {
            // Lógica para upload de um único arquivo
            UploadFileResponseVO response = uploadSingleFile(file);
            message = "Arquivo enviado com sucesso: " + response.getFileName();
        } else if (files != null && files.length > 0) {
            // Lógica para upload de múltiplos arquivos
            List<UploadFileResponseVO> responses = uploadMultipleFiles(files);
            message = "Arquivos enviados com sucesso: " + responses.stream()
                    .map(UploadFileResponseVO::getFileName)
                    .collect(Collectors.joining(", "));
        } else {
            // Redirecionar para a página de erro se nenhum arquivo for enviado
            return "redirect:/error?message=" + UriUtils.encode("Por favor, selecione um arquivo para enviar.", StandardCharsets.UTF_8);
        }

        // Redirecionar para a página de sucesso
        return "redirect:/success?message=" + UriUtils.encode(message, StandardCharsets.UTF_8);
    }

    private UploadFileResponseVO uploadSingleFile(MultipartFile file) throws Exception {
        String fileName = fileStorageService.storeFile(file);
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();
        return new UploadFileResponseVO(fileName, fileDownloadUri, file.getContentType(), file.getSize());
    }

    private List<UploadFileResponseVO> uploadMultipleFiles(MultipartFile[] files) {
        return Arrays.stream(files)
                .map(file -> {
                    try {
                        return uploadSingleFile(file);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                    .collect(Collectors.toList());
        }

        @GetMapping("/success")
        public String successPage(@RequestParam String message, Model model) {
            model.addAttribute("message", message);
            return "success"; // Nome do arquivo HTML sem a extensão
        }

        @GetMapping("/error")
        public String errorPage(@RequestParam String message, Model model) {
            model.addAttribute("message", message);
            return "error"; // Nome do arquivo HTML sem a extensão
        }
    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (Exception e) {
            logger.info("Could not determine file type");
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/createFolder")
    public ResponseEntity<String> createFolder(@RequestParam("folderName") String folderName) {
        boolean isCreated = fileStorageService.createFolder(folderName);
        if (isCreated) {
            return ResponseEntity.ok("Pasta criada com sucesso: " + folderName);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Essa pasta já existe:  " + folderName);
        }
    }

    @GetMapping("/")
    public List<String> listFilesAndFolders() {
        Path rootLocation = fileListService.getRootLocation(); // Obtém o diretório raiz
        return fileListService.listFilesInDirectory(Path.of(String.valueOf(rootLocation))); // Usa o FileListService para listar arquivos e pastas
    }
}


