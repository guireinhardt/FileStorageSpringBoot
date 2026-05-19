package com.guireinhardt.FileStorage.controller.publico;

import com.guireinhardt.FileStorage.infra.log.AuditLogService;
import com.guireinhardt.FileStorage.service.FileListService;
import com.guireinhardt.FileStorage.service.FileSearchService;
import com.guireinhardt.FileStorage.vo.FileVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/public")
public class PublicController {
    @Autowired
    private FileListService fileListService;
    @Autowired
    private FileSearchService fileSearchService;
    @Autowired
    private AuditLogService auditLogService;


    @GetMapping("/index")
    public String listPublicFiles(@RequestParam(name = "path", required = false) String path, Model model, Principal principal) {
        Path basePath = fileListService.getRootLocation().resolve("02.FINALIZADOS");
        Path targetPath = (path != null && !path.isEmpty()) ? basePath.resolve(path) : basePath;

        List<FileVO.FileValueObject> folders = new ArrayList<>();
        List<FileVO.FileValueObject> files = new ArrayList<>();

        try (Stream<Path> paths = Files.list(targetPath)) {
            paths.forEach(file -> {
                String name = file.getFileName().toString();
                boolean isDir = Files.isDirectory(file);

                String relative = basePath.relativize(file).toString().replace("\\", "/");
                FileVO.FileValueObject vo = new FileVO.FileValueObject(name, isDir, relative);

                if (isDir) {
                    folders.add(vo);
                } else {
                    files.add(vo);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Erro ao listar arquivos", e);
        }

        // Montar lista de partes do caminho para navegação breadcrumb
        List<String> pathParts = (path != null && !path.isEmpty()) ? List.of(path.split("/")) : List.of();

        model.addAttribute("folders", folders);
        model.addAttribute("files", files);
        model.addAttribute("pathParts", pathParts);
        model.addAttribute("currentPath", path); // para montar os hrefs do breadcrumb

        // Log de navegação
        if (principal != null) {
            String usuario = principal.getName();
            String caminho = (path != null && !path.isEmpty()) ? path : "/";
            auditLogService.log(usuario, "NAVEGAÇÃO", "Acessou a pasta: " + caminho);
        }

        return "public/public-files";
    }

    @GetMapping("/view/**")
    @ResponseBody
    public ResponseEntity<Resource> visualizarArquivoPublico(HttpServletRequest request) {
        try {
            String requestURI = request.getRequestURI();
            String basePath = "/public/view/";
            String filePath = URLDecoder.decode(requestURI.substring(requestURI.indexOf(basePath) + basePath.length()), StandardCharsets.UTF_8);

            Path file = fileListService.getRootLocation()
                    .resolve("02.FINALIZADOS")
                    .resolve(filePath)
                    .normalize();

            UrlResource resource = new UrlResource(file.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/download/**")
    @ResponseBody
    public ResponseEntity<Resource> downloadArquivo(HttpServletRequest request,Principal principal) {
        try {
            String requestURI = request.getRequestURI();
            String basePath = "/public/download/";
            String filePath = URLDecoder.decode(requestURI.substring(requestURI.indexOf(basePath) + basePath.length()), StandardCharsets.UTF_8);

            Path file = fileListService.getRootLocation()
                    .resolve("02.FINALIZADOS")
                    .resolve(filePath)
                    .normalize();

            UrlResource resource = new UrlResource(file.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            // LOG DO DOWNLOAD
            if (principal != null) {
                String usuario = principal.getName();
                auditLogService.log(usuario, "DOWNLOAD", "Baixou o arquivo: " + filePath);
            }

            String contentDisposition = "attachment; filename=\"" + file.getFileName().toString() + "\"";

            MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    @PostMapping("/download-zip")
    public void downloadMultipleFiles(@RequestParam List<String> files, HttpServletResponse response, Principal principal) throws IOException {
        Path basePath = fileListService.getRootLocation().resolve("02.FINALIZADOS");

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=arquivos.zip");

        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            for (String relativePath : files) {
                Path filePath = basePath.resolve(relativePath).normalize();

                if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                    ZipEntry zipEntry = new ZipEntry(filePath.getFileName().toString());
                    zipOut.putNextEntry(zipEntry);

                    Files.copy(filePath, zipOut);

                    zipOut.closeEntry();
                }
            }
            // LOG DO DOWNLOAD-ZIP
            if (principal != null) {
                String usuario = principal.getName();
                auditLogService.log(usuario, "DOWNLOAD-ZIP", "Baixou arquivos em ZIP: " + String.join(", ", files));
            }

            zipOut.finish();
        }
        }
    }




