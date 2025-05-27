package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.dto.FileResultDTO;
import com.secretaria.FileStorage.dto.FolderDTO;
import com.secretaria.FileStorage.service.CityService;
import com.secretaria.FileStorage.service.FileListService;
import com.secretaria.FileStorage.service.FolderService;
import com.secretaria.FileStorage.vo.FileVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

   /* @PostMapping("/download-zip")
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
    }  */
    @PostMapping("/download-zip")
    public void downloadZip(@RequestParam("files") List<String> selectedPaths, HttpServletResponse response) throws IOException {
        // Criar ZIP
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        for (String pathStr : selectedPaths) {
            Path path = Paths.get(pathStr);
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    zipFolder(path, path.getFileName().toString(), zos); // Pasta
                } else {
                    zipFile(path, path.getFileName().toString(), zos);   // Arquivo
                }
            }
        }

        zos.close();

        // Enviar o ZIP para o navegador
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=arquivos_selecionados.zip");
        response.getOutputStream().write(baos.toByteArray());
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }

    @PostMapping("/download-folders")
    public ResponseEntity<Resource> downloadFoldersAsZip(@RequestParam("selectedFolders") String folderPathsStr) throws IOException {
        List<String> folderPaths = Arrays.asList(folderPathsStr.split(","));

        List<Path> allFiles = new ArrayList<>();
        for (String folderPathStr : folderPaths) {
            Path folderPath = Paths.get(folderPathStr).toAbsolutePath().normalize();
            if (Files.exists(folderPath) && Files.isDirectory(folderPath)) {
                try (Stream<Path> stream = Files.walk(folderPath)) {
                    stream.filter(Files::isRegularFile).forEach(allFiles::add);
                }
            }
        }

        if (allFiles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // Determina o diretório base com base no primeiro caminho informado
        Path basePath = Paths.get(folderPaths.get(0)).toAbsolutePath().normalize();

        Path zipPath = Files.createTempFile("folders-", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            for (Path file : allFiles) {
                Path absoluteFile = file.toAbsolutePath().normalize();
                if (!absoluteFile.startsWith(basePath)) continue; // segurança extra
                Path relativePath = basePath.relativize(absoluteFile);
                zos.putNextEntry(new ZipEntry(relativePath.toString()));
                Files.copy(absoluteFile, zos);
                zos.closeEntry();
            }
        }

        Resource resource = new UrlResource(zipPath.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pastas_selecionadas.zip\"")
                .body(resource);
    }






    //métodos auxiliares
    private void zipFile(Path file, String entryName, ZipOutputStream zos) throws IOException {
        zos.putNextEntry(new ZipEntry(entryName));
        Files.copy(file, zos);
        zos.closeEntry();
    }

    private void zipFolder(Path folder, String baseName, ZipOutputStream zos) throws IOException {
        Files.walk(folder).forEach(path -> {
            try {
                if (Files.isDirectory(path)) return;

                String entryName = baseName + "/" + folder.relativize(path).toString();
                zos.putNextEntry(new ZipEntry(entryName));
                Files.copy(path, zos);
                zos.closeEntry();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
    public File createZipFromFolders(List<String> folderPaths) throws IOException {
        File zipFile = File.createTempFile("pastas_", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (String folderPath : folderPaths) {
                Path folder = Paths.get(folderPath);
                Files.walk(folder).filter(Files::isRegularFile).forEach(file -> {
                    try {
                        ZipEntry zipEntry = new ZipEntry(folder.relativize(file).toString());
                        zos.putNextEntry(zipEntry);
                        Files.copy(file, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        }
        return zipFile;
    }




}












