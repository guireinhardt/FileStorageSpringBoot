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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/storage")
public class FileStorageController {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileListService fileListService;

    @PostMapping("/storage/uploadFile")
    public UploadFileResponseVO uploadfile(@RequestParam("file") MultipartFile file) throws Exception {
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponseVO(fileName,fileDownloadUri,file.getContentType(),file.getSize());
    }

    @PostMapping("/storage/uploadMultipleFiles")
    public List<UploadFileResponseVO> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files){
        return Arrays.asList(files)
                .stream()
                .map(file -> {
                    try {
                        return uploadfile(file);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

    }
    @GetMapping("/storage/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {

        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());

        }catch (Exception e){
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
    @PostMapping("/storage/createFolder")
    public ResponseEntity<String> createFolder(@RequestParam("folderName") String folderName) {
        boolean isCreated = fileStorageService.createFolder(folderName);
        if (isCreated) {
            return ResponseEntity.ok("Pasta criada com sucesso: " + folderName);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Essa pasta já existe:  " + folderName);
        }
    }
    @PostMapping("/storage/enviar") // Mapeia a URL /enviar
    public String processarFormulario(@ModelAttribute("file") MultipartFile file, Model model) {
        // Lógica para processar o upload do arquivo
        String fileName = null;
        try {
            fileName = fileStorageService.storeFile(file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        model.addAttribute("message", "Arquivo enviado com sucesso: " + fileName);
        return "resultado"; // Retorna a página de resultado
    }
    @GetMapping("/")
    public List<String> listFilesAndFolders() {
        Path rootLocation = fileListService.getRootLocation(); // Obtém o diretório raiz
        return fileListService.listFilesInDirectory(Path.of(String.valueOf(rootLocation))); // Usa o FileListService para listar arquivos e pastas
    }
    }


