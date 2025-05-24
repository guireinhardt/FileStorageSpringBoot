package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.dto.FileResultDTO;
import com.secretaria.FileStorage.dto.FolderDTO;
import com.secretaria.FileStorage.service.CityService;
import com.secretaria.FileStorage.service.FileListService;
import com.secretaria.FileStorage.service.FolderService;
import com.secretaria.FileStorage.vo.FileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/explorer")
public class ExplorerController {

    @Autowired
    FileListService fileListService;

    @Autowired
    CityService cityService;

    @Autowired
    private FolderService folderService;

    @Value("${file.upload-dir}")
    private String fileStorageLocation;  // diretório base vindo do application.properties

    private boolean isPathInsideBaseDir(String baseDir, String targetPath) throws IOException {
        Path base = Paths.get(baseDir).toRealPath();
        Path target = Paths.get(targetPath).toRealPath();
        return target.startsWith(base);
    }

    @GetMapping("/tree")
    public ResponseEntity<List<FolderDTO>> getFolderTree() {
        List<FolderDTO> tree = folderService.getFolderTree();
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileResultDTO>> getFilesInFolder(@RequestParam String folderPath) {
        if (folderPath == null || folderPath.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // Validação do path da pasta
            if (!isPathInsideBaseDir(fileStorageLocation, folderPath)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<FileResultDTO> files = folderService.getFilesInFolder(folderPath);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            // Log do erro aqui
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/view")
    public ResponseEntity<Resource> viewFile(@RequestParam String path) throws IOException {
        if (!isPathInsideBaseDir(fileStorageLocation, path)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        File file = new File(path);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        String contentType = Files.probeContentType(file.toPath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .body(resource);
    }

    @GetMapping
    public String getExplorerPage(Model model) {
        model.addAttribute("files", new ArrayList<>()); // inicia vazio ou com arquivos padrão
        return "explorer"; // corresponde ao explorer.html
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String path) throws IOException {
        if (!isPathInsideBaseDir(fileStorageLocation, path)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        File file = new File(path);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        String contentType = Files.probeContentType(file.toPath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .contentLength(file.length())
                .body(resource);
    }

    @PostMapping("/download-zip")
    public ResponseEntity<Resource> downloadMultiple(@RequestParam List<String> files) throws IOException {
        for (String filePath : files) {
            if (!isPathInsideBaseDir(fileStorageLocation, filePath)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        Path zipPath = Files.createTempFile("batch-", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            for (String filePath : files) {
                File file = new File(filePath);
                if (file.exists()) {
                    zos.putNextEntry(new ZipEntry(file.getName()));
                    Files.copy(file.toPath(), zos);
                    zos.closeEntry();
                }
            }
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(zipPath.toFile()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"arquivos.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(zipPath.toFile().length())
                .body(resource);
    }
}












