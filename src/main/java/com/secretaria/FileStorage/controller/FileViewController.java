package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.config.FileStorageConfig;
import com.secretaria.FileStorage.config.KeywordConfig;
import com.secretaria.FileStorage.dto.FileResultDTO;
import com.secretaria.FileStorage.exception.FileStorageException;
import com.secretaria.FileStorage.exception.FileStorageNotFoundException;
import com.secretaria.FileStorage.service.FileListService;
import com.secretaria.FileStorage.service.FileSearchService;
import com.secretaria.FileStorage.service.FileStorageService;
import com.secretaria.FileStorage.service.FileViewService;
import com.secretaria.FileStorage.vo.FileVO;
import com.secretaria.FileStorage.infra.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.servlet.HandlerMapping;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
    @Autowired
    private TokenService tokenService;

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
        List<FileVO.FileValueObject> folders = new ArrayList<>();
        List<FileVO.FileValueObject> files = new ArrayList<>();

        // Nome do diretório da lixeira (ajuste conforme necessário)
        String trashDirectory = "lixeira"; // Se sua lixeira estiver em um diretório chamado 'trash', por exemplo

        try (Stream<Path> paths = Files.list(rootLocation)) {
            paths.forEach(path -> {
                String fileName = path.getFileName().toString();
                boolean isDirectory = Files.isDirectory(path);

                // Verifica se o caminho é a lixeira ou dentro da lixeira
                if (path.toString().contains(trashDirectory)) {
                    return; // Ignora arquivos/pastas dentro da lixeira
                }

                // Cria o objeto de valor para o arquivo/pasta
                FileVO.FileValueObject fileItem = new FileVO.FileValueObject(fileName, isDirectory);

                // Adiciona o item na lista de pastas ou arquivos
                if (isDirectory) {
                    folders.add(fileItem); // Adiciona à lista de pastas
                } else {
                    files.add(fileItem); // Adiciona à lista de arquivos
                }
            });
        } catch (IOException e) {

            throw new FileStorageException("Erro ao listar arquivos e pastas", e);
        }

        // Adiciona as listas de pastas e arquivos no modelo
        model.addAttribute("folders", folders); // Pastas
        model.addAttribute("files", files); // Arquivos

        return "list"; // Retorna o nome do template a ser renderizado
    }


    @GetMapping("/storage/openFolder/**")
    public String openFolder(HttpServletRequest request, Model model) throws UnsupportedEncodingException {
        // Extrai o caminho da pasta após /storage/openFolder/
        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String folderRelativePath = fullPath.replace("/storage/openFolder/", "");

        // Decodifica o caminho da pasta
        folderRelativePath = URLDecoder.decode(folderRelativePath, "UTF-8");

        // Divide o caminho da pasta em partes
        String[] folderParts = folderRelativePath.split("/");

        // Define a pasta anterior (caso contrário, será "Início")
        String parentFolder = (folderParts.length > 1) ? String.join("/", Arrays.copyOfRange(folderParts, 0, folderParts.length - 1)) : "";

        // Resolve o caminho completo da pasta
        Path folderPath = fileListService.getRootLocation().resolve(folderRelativePath);
        List<FileVO.FileValueObject> folders = new ArrayList<>();
        List<FileVO.FileValueObject> files = new ArrayList<>();

        try (Stream<Path> paths = Files.list(folderPath)) {
            paths.forEach(path -> {
                String fileName = path.getFileName().toString();
                boolean isDirectory = Files.isDirectory(path);
                FileVO.FileValueObject fileItem = new FileVO.FileValueObject(fileName, isDirectory);
                if (isDirectory) {
                    folders.add(fileItem);
                } else {
                    files.add(fileItem);
                }
            });
        } catch (IOException e) {
            throw new FileStorageException("Erro ao listar arquivos e pastas", e);
        }

        // Passa as informações para o template
        model.addAttribute("folderParts", folderParts);
        model.addAttribute("folders", folders);
        model.addAttribute("files", files);
        model.addAttribute("currentFolder", folderRelativePath);
        model.addAttribute("parentFolder", parentFolder);  // Adiciona a pasta anterior

        return "folderView"; // Retorna para o template correto
    }






    @GetMapping("/search")
    public String search(@RequestParam(required = false) String query,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String subkeyword,
                         @RequestParam(required = false) String city,
                         Model model) {

        System.out.println("Keyword selecionada: " + keyword);
        System.out.println("Subkeyword selecionada: " + subkeyword);
        System.out.println("Cidade selecionada: " + city);

        List<FileResultDTO> files = new ArrayList<>();

        // Monta a query completa
        StringBuilder fullQuery = new StringBuilder();
        if (query != null && !query.isEmpty()) {
            fullQuery.append(query.trim()).append(" ");
        }
        if (keyword != null && !keyword.isEmpty()) {
            fullQuery.append(keyword.trim()).append(" ");
        }
        if (subkeyword != null && !subkeyword.isEmpty()) {
            fullQuery.append(subkeyword.trim()).append(" ");
        }
        if (city != null && !city.isEmpty()) {
            fullQuery.append(city.trim()).append(" ");
        }

        // A busca só será realizada se o campo de pesquisa não estiver vazio
        if (!fullQuery.toString().trim().isEmpty()) {
            try {
                // Realiza a busca (agora já sem arquivos da lixeira)
                files = fileSearchService.searchFiles(fullQuery.toString().trim());

                model.addAttribute("files", files);
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("files", List.of());
            }
        }

        model.addAttribute("keywords", keywordConfig.getKeywordList());
        return "search";
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
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName,
                                              @CookieValue(value = "authToken", required = false) String authToken) { // Busca o token no cookie
        try {
            if (authToken == null || authToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);  // Retorna 403 se o token não estiver presente
            }

            // Valida o token JWT
            String username = tokenService.validateToken(authToken);  // Usa o TokenService para validar o token

            if (username == null || username.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);  // Retorna 403 se o token for inválido
            }

            // Se o token for válido, processa a requisição e serve o arquivo
            Path filePath = fileStorageConfig.getUploadDirPath().resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = fileViewService.getContentType(fileName);  // Chama o método do serviço para obter o tipo de conteúdo

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();  // Retorna 404 caso o arquivo não seja encontrado
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();  // Retorna 500 em caso de erro de URL
        }
    }
    @PatchMapping("/storage/renameFile")
    public ResponseEntity<?> renameFile(@RequestParam String oldName, @RequestParam String newName) {
        Path sourcePath = fileStorageConfig.getUploadDirPath().resolve(oldName);
        Path targetPath = fileStorageConfig.getUploadDirPath().resolve(newName);
        try {
            Files.move(sourcePath, targetPath);
            return ResponseEntity.ok().body("Arquivo renomeado com sucesso");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao renomear o arquivo");
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