package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.config.FileStorageConfig;
import com.secretaria.FileStorage.config.KeywordConfig;
import com.secretaria.FileStorage.dto.FileResultDTO;
import com.secretaria.FileStorage.exception.FileStorageException;
import com.secretaria.FileStorage.exception.FileStorageNotFoundException;
import com.secretaria.FileStorage.service.*;
import com.secretaria.FileStorage.vo.FileVO;
import com.secretaria.FileStorage.infra.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
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


import java.io.*;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
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
    private  KeywordService keywordService;
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

                // Obtém o caminho absoluto e calcula o relativePath
                String absolutePath = path.toAbsolutePath().toString().replace("\\", "/");
                String relativePath = absolutePath.replace(rootLocation.toAbsolutePath().toString().replace("\\", "/") + "/", "");

                // Cria o objeto de valor para o arquivo/pasta, agora incluindo o relativePath
                FileVO.FileValueObject fileItem = new FileVO.FileValueObject(fileName, isDirectory, relativePath);

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

                // Obtém o caminho absoluto e calcula o relativePath
                String absolutePath = path.toAbsolutePath().toString().replace("\\", "/");
                String relativePath = absolutePath.replace(fileListService.getRootLocation().toAbsolutePath().toString().replace("\\", "/") + "/", "");

                // Cria o objeto de valor para o arquivo/pasta, agora incluindo o relativePath
                FileVO.FileValueObject fileItem = new FileVO.FileValueObject(fileName, isDirectory, relativePath);

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

        // Passa as informações para o template
        model.addAttribute("folderParts", folderParts);
        model.addAttribute("folders", folders);
        model.addAttribute("files", files);
        model.addAttribute("currentFolder", folderRelativePath);
        model.addAttribute("parentFolder", parentFolder);  // Adiciona a pasta anterior

        return "folderView"; // Retorna para o template correto
    }

    @Autowired
    private InstituteService instituteService;

    @Autowired
    private CityService cityService;

    @GetMapping("/view/subkeywords")
    @ResponseBody
    public List<String> getSubkeywordsP(@RequestParam String keyword) {
        return keywordService.getKeywordMap().getOrDefault(keyword, List.of());
    }
    @GetMapping("/search")
    public String searchFilesPrivate(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> subkeywords,
            @RequestParam(required = false) String institute,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) throws IOException {

        boolean hasFilters = (query != null && !query.isBlank())
                || (keyword != null && !keyword.isBlank())
                || (subkeywords != null && !subkeywords.isEmpty())
                || (institute != null && !institute.isBlank())
                || (city != null && !city.isBlank())
                || startDate != null
                || endDate != null;

        List<FileResultDTO> results = new ArrayList<>();

        if (hasFilters) {
            results = fileSearchService.searchFiles(query, keyword,subkeywords, institute, city, startDate, endDate);
        }

        List<String> keywords = new ArrayList<>(keywordService.getDisplayMap().keySet());
        Map<String, List<String>> subkeywordsMap = keywordService.getKeywordMap();

        model.addAttribute("files", results);
        model.addAttribute("hasFilters", hasFilters);

        model.addAttribute("keywords", keywords);
        model.addAttribute("displayMap", keywordService.getDisplayMap());
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("subkeywordsMap", subkeywordsMap);
        model.addAttribute("selectedSubkeywords", subkeywords != null ? subkeywords : new ArrayList<>());

        model.addAttribute("cities", cityService.getAllCities());
        model.addAttribute("selectedCity", city);

        model.addAttribute("institutes", instituteService.getAllInstitutes());
        model.addAttribute("selectedInstitute", institute);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "search"; // <- ou o nome da sua tela privada
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

    //VISUALIZAR OS ARQUIVOS NA PAGINA DE BUSCA
    @GetMapping("/view")
    public ResponseEntity<Resource> viewFileSearch(@RequestParam("path") String filePath,
                                             @CookieValue(value = "authToken", required = false) String authToken) {
        try {
            if (authToken == null || authToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            String username = tokenService.validateToken(authToken);
            if (username == null || username.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            Path basePath = fileStorageConfig.getUploadDirPath();
            Path resolvedPath = basePath.resolve(filePath).normalize();

            Resource resource = new UrlResource(resolvedPath.toUri());
            if (resource.exists() && resource.isReadable()) {
                String contentType = fileViewService.getContentType(filePath);

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    //VISUALIZAR OS ARQUIVOS NA RAIZ
    @GetMapping("/view/{filePath:.+}")
    public ResponseEntity<Resource> viewFile(@PathVariable String filePath,
                                             @CookieValue(value = "authToken", required = false) String authToken) {
        try {
            // Verifica se o token de autenticação está presente
            if (authToken == null || authToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            // Valida o token
            String username = tokenService.validateToken(authToken);
            if (username == null || username.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            // Resolve o caminho absoluto com base no diretório base + caminho relativo
            Path basePath = fileStorageConfig.getUploadDirPath();  // Diretório de upload configurado
            Path resolvedPath = basePath.resolve(filePath).normalize();  // Resolve o caminho do arquivo

            // Verifica se o arquivo existe e se é legível
            Resource resource = new UrlResource(resolvedPath.toUri());
            if (resource.exists() && resource.isReadable()) {
                // Obtém o tipo de conteúdo do arquivo
                String contentType = fileViewService.getContentType(filePath);

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();  // Retorna 404 se não encontrado
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();  // Em caso de erro na URL
        }
    }

    @PatchMapping("/storage/renameFile")
    public ResponseEntity<?> renameFile(@RequestParam String oldName, @RequestParam String newName) {
        try {
            // Obtém o caminho completo com base no diretório raiz
            Path rootLocation = fileStorageConfig.getUploadDirPath();

            // Resolve o caminho completo do arquivo de origem
            Path sourcePath = rootLocation.resolve(oldName);  // Caminho completo do arquivo original (incluindo subpastas)

            // Resolve o caminho do arquivo de destino no mesmo diretório, mas com o novo nome
            Path targetPath = sourcePath.getParent().resolve(newName);  // Mantém o diretório, apenas altera o nome

            // Renomeia o arquivo
            Files.move(sourcePath, targetPath);

            return ResponseEntity.ok().body("Arquivo renomeado com sucesso");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao renomear o arquivo: " + e.getMessage());
        }
    }
    //rota de renomear na pasta raiz
    @PatchMapping("/storage/renameRootFile")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> renameRootFile(
            @RequestParam String oldFileName,
            @RequestParam String newFileName) {

        Map<String, Object> response = new HashMap<>();
        try {
            // Obter o diretório de upload
            Path rootLocation = fileStorageConfig.getUploadDirPath();  // Já existe em sua configuração

            // Verifica se o nome do arquivo contém "/" para evitar renomeação fora da raiz
            if (oldFileName.contains("/") || newFileName.contains("/")) {
                response.put("success", false);
                response.put("message", "Renomeação apenas na raiz permitida.");
                return ResponseEntity.badRequest().body(response);
            }

            // Obter a extensão do arquivo original
            String oldExtension = getExtension(oldFileName);

            // Se o novo nome não contiver a extensão, adicione a extensão original ao nome
            if (!newFileName.contains(".")) {
                newFileName += oldExtension; // Adiciona a extensão original ao novo nome
            }

            // Resolve os caminhos completos do arquivo de origem e destino
            Path sourcePath = rootLocation.resolve(oldFileName);  // Caminho completo do arquivo original
            Path targetPath = sourcePath.getParent().resolve(newFileName);  // Caminho do novo arquivo com novo nome

            // Verifica se o arquivo de origem existe
            if (!Files.exists(sourcePath)) {
                response.put("success", false);
                response.put("message", "Arquivo original não encontrado.");
                return ResponseEntity.badRequest().body(response);
            }

            // Realiza a renomeação
            Files.move(sourcePath, targetPath);

            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro ao renomear: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Função para obter a extensão do arquivo
    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index > 0) {
            return fileName.substring(index);  // Retorna a extensão incluindo o ponto (ex: .mp4)
        }
        return "";  // Retorna uma string vazia se não houver extensão
    }
    @PatchMapping("/storage/search/renameFile")
    public ResponseEntity<?> renameFileFromSearch(@RequestParam String fullPath, @RequestParam String newName) {
        try {
            Path rootLocation = fileStorageConfig.getUploadDirPath();

            // Caminho completo do arquivo atual
            Path sourcePath = rootLocation.resolve(fullPath).normalize();

            // Validação de segurança (opcional)
            if (!sourcePath.startsWith(rootLocation)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado.");
            }

            // Novo caminho (mantendo o diretório original, apenas troca o nome)
            Path targetPath = sourcePath.getParent().resolve(newName).normalize();

            Files.move(sourcePath, targetPath);

            return ResponseEntity.ok().body("Arquivo renomeado com sucesso");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao renomear o arquivo: " + e.getMessage());
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
    //download pagina de busca
    @GetMapping("/download/**")
    public ResponseEntity<Resource> downloadFileSearch(HttpServletRequest request) {
        String uri = request.getRequestURI(); // Ex: /download/uploads/2024/maio/arquivo.pdf
        String basePath = "/download/";
        String encodedPath = uri.substring(uri.indexOf(basePath) + basePath.length());

        // Decodifica a URL
        String relativePath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8);

        // Remove o prefixo "uploads/" se existir, pois uploadDir já aponta para a raiz
        if (relativePath.startsWith("uploads/") || relativePath.startsWith("uploads\\")) {
            relativePath = relativePath.substring("uploads/".length());
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Resource resource = fileStorageService.loadFileAsResource(relativePath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (FileStorageNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    //compartilhamento publico
    @GetMapping("/storage/view/{fileName}")
    public ResponseEntity<Resource> viewFile(@PathVariable String fileName) {
        try {
            Path filePath = fileStorageConfig.getUploadDirPath().resolve(fileName);
            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // Detectar o tipo MIME do arquivo
            String contentType = Files.probeContentType(filePath);

            if (contentType == null) {
                contentType = "application/octet-stream"; // fallback, caso o tipo MIME não seja detectado
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType)) // Define o tipo correto
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }






}