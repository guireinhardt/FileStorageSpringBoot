package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.config.FileStorageConfig;
import com.secretaria.FileStorage.config.KeywordConfig;
import com.secretaria.FileStorage.exception.FileStorageException;
import com.secretaria.FileStorage.exception.FileStorageNotFoundException;
import com.secretaria.FileStorage.service.FileListService;
import com.secretaria.FileStorage.service.FileSearchService;
import com.secretaria.FileStorage.service.FileStorageService;
import com.secretaria.FileStorage.service.FileViewService;
import com.secretaria.FileStorage.vo.FileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestParameterPropertyValues;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;




@Controller
public class FileViewController {

    @Autowired
    private FileListService fileListService;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private FileSearchService fileSearchService;
    @Autowired
    private KeywordConfig keywordConfig;

    private final FileViewService fileViewService;
    private final FileStorageConfig fileStorageConfig;
    @Autowired
    public FileViewController(FileViewService fileViewService, FileStorageConfig fileStorageConfig) {
        this.fileViewService = fileViewService;
        this.fileStorageConfig = fileStorageConfig;
    }





    @GetMapping("/upload")
    public String uploadPage() {
        return "upload"; // Retorna o template upload.html
    }


    @GetMapping("/")
    public String listFilesAndFolders(Model model) {
        Path rootLocation = fileListService.getRootLocation(); // Obtém o diretório raiz
        List<FileVO.FileValueObject> fileItems = new ArrayList<>();
        List<FileVO.FileValueObject> folders = new ArrayList<>();
        List<FileVO.FileValueObject> files = new ArrayList<>();

        try (Stream<Path> paths = Files.list(rootLocation)) {
            paths.forEach(path -> {
                String fileName = path.getFileName().toString();
                boolean isDirectory = Files.isDirectory(path);
                FileVO.FileValueObject fileItem = new FileVO.FileValueObject(fileName, isDirectory);
                if (isDirectory) {
                    folders.add(fileItem); // Adiciona à lista de pastas
                } else {
                    files.add(fileItem); // Adiciona à lista de arquivos
                }
            });
        } catch (IOException e) {
            throw new FileStorageException("Erro ao listar arquivos e pastas", e);
        }

        model.addAttribute("folders", folders); // Adiciona a lista de pastas ao modelo
        model.addAttribute("files", files); // Adiciona a lista de arquivos ao modelo
        return "list"; // Retorna o nome do template a ser renderizado
    }
    @GetMapping("/storage/openFolder/{folderName}")
    public String openFolder(@PathVariable String folderName, Model model) {
        Path folderPath = fileListService.getRootLocation().resolve(folderName); // Resolve o caminho da pasta
        List<FileVO.FileValueObject> folders = new ArrayList<>();
        List<FileVO.FileValueObject> files = new ArrayList<>();

        try (Stream<Path> paths = Files.list(folderPath)) {
            paths.forEach(path -> {
                String fileName = path.getFileName().toString();
                boolean isDirectory = Files.isDirectory(path);
                FileVO.FileValueObject fileItem = new FileVO.FileValueObject(fileName, isDirectory);
                if (isDirectory) {
                    folders.add(fileItem); // Adiciona à lista de subpastas
                } else {
                    files.add(fileItem); // Adiciona à lista de arquivos
                }
            });
        } catch (IOException e) {
            throw new FileStorageException("Erro ao listar arquivos e pastas", e);
        }

        model.addAttribute("folders", folders); // Adiciona a lista de subpastas ao modelo
        model.addAttribute("files", files); // Adiciona a lista de arquivos ao modelo
        model.addAttribute("currentFolder", folderName); // Adiciona o nome da pasta atual ao modelo
        return "upload"; // Retorna o template upload.html
    }
    // Método para a página de busca
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String query,
                         @RequestParam(required = false) String keyword,
                         Model model) {
        System.out.println("Keyword selecionada: " + keyword);
        List<String> files = new ArrayList<>();
        System.out.println("Keyword selecionada: " + keyword);

        // Se a palavra-chave estiver selecionada, adicione-a à consulta
        if (keyword != null && !keyword.isEmpty()) {
            query = (query == null ? "" : query.trim()) + " " + keyword.trim();
        }

        if (query == null || query.isEmpty()) {
            model.addAttribute("files", List.of()); // Retorna uma lista vazia se a consulta estiver vazia
            model.addAttribute("keywords", keywordConfig.getKeywordList()); // Adiciona a lista de palavras-chave ao modelo
            return "search"; // Retorna a página de pesquisa
        }

        try {

            // Chama o serviço para buscar arquivos com base na consulta
            files = fileSearchService.searchFiles(query.trim()); // Use trim() para remover espaços em branco
            model.addAttribute("files", files); // Adiciona os arquivos encontrados ao modelo
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("files", List.of()); // Retorna uma lista vazia em caso de erro
        }

        model.addAttribute("keywords", keywordConfig.getKeywordList()); // Adiciona a lista de palavras-chave ao modelo
        return "search"; // Nome do arquivo HTML sem a extensão
    }

    private String sanitizeFileName(String fileName) {
        // Verifica se o nome do arquivo contém caracteres inválidos
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("Nome de arquivo inválido: " + fileName);
        }
        System.out.println(fileName);
        // Retorna o nome do arquivo sem alterações, pois as barras são necessárias
        return fileName;
    }


    @GetMapping("/view/{fileName:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
        try {
            Path filePath = fileStorageConfig.getUploadDirPath().resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = fileViewService.getContentType(fileName); // Chame o método do serviço

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"") // Certifique-se de que é "inline"
                        .contentType(MediaType.parseMediaType(contentType)) // Use o tipo de mídia retornado
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno: " + e.getMessage());
    }

    // Método para buscar imagens e vídeos no banco de imagens
    @GetMapping("/banco-de-imagens/search")
    public String searchMedia(@RequestParam(required = false) String query, Model model) throws IOException {
        List<String> mediaFiles = new ArrayList<>();

        if (query == null || query.isEmpty()) {
            // Se a consulta estiver vazia, você pode decidir o que fazer
            // Por exemplo, retornar todos os arquivos de mídia
            mediaFiles = fileSearchService.getAllMediaFiles(); // Método que retorna todos os arquivos de mídia
        } else {
            try {
                // Chama o serviço para buscar arquivos de mídia com base na consulta
                mediaFiles = fileSearchService.searchMediaFiles(query.trim());
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("mediaFiles", List.of()); // Retorna uma lista vazia em caso de erro
            }
        }

        model.addAttribute("mediaFiles", mediaFiles);
        return "bancodeimagens"; // Retorna a página do banco de imagens
    }
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            // Usa o método loadFileAsResource do FileStorageService
            Resource resource = fileStorageService.loadFileAsResource(fileName);

            // Define o cabeçalho Content-Disposition para download
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM) // Tipo padrão para downloads
                    .body(resource);
        } catch (FileStorageNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



}