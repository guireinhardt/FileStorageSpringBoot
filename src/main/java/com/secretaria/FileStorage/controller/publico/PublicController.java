package com.secretaria.FileStorage.controller.publico;

import com.secretaria.FileStorage.exception.FileStorageException;
import com.secretaria.FileStorage.service.FileListService;
import com.secretaria.FileStorage.service.FileSearchService;
import com.secretaria.FileStorage.vo.FileVO;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Controller
@RequestMapping("/public")
public class PublicController {
    @Autowired
    private FileListService fileListService;
    @Autowired
    private FileSearchService fileSearchService;


    @GetMapping("/index")
    public String listPublicFiles(@RequestParam(name = "path", required = false) String path, Model model) {
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

        model.addAttribute("folders", folders);
        model.addAttribute("files", files);
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

}
