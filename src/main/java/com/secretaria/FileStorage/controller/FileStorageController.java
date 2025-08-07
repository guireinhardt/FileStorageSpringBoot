package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.service.FileStorageService;
import com.secretaria.FileStorage.utils.DeletableInputStreamResource;
import com.secretaria.FileStorage.utils.FileValidator;
import com.secretaria.FileStorage.vo.FileResponseVO;
import com.secretaria.FileStorage.vo.UploadFileResponseVO;
import com.secretaria.FileStorage.service.FileListService;
import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/storage")
public class FileStorageController {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileListService fileListService;

    /* @PostMapping("/uploadFiles")
    public String uploadFiles(@RequestParam(value = "file", required = false) MultipartFile file,
                              @RequestParam(value = "files", required = false) MultipartFile[] files,
                              @RequestParam("folderPath") String folderPath) throws Exception {
        String message;

        if (file != null && !file.isEmpty()) {
            // Lógica para upload de um único arquivo
            UploadFileResponseVO response = uploadSingleFile(file, folderPath);
            message = "Arquivo enviado com sucesso: " + response.getFileName();
        } else if (files != null && files.length > 0) {
            // Lógica para upload de múltiplos arquivos
            List<UploadFileResponseVO> responses = uploadMultipleFiles(files, folderPath);
            message = "Arquivos enviados com sucesso: " + responses.stream()
                    .map(UploadFileResponseVO::getFileName)
                    .collect(Collectors.joining(", "));
        } else {
            return "redirect:/error?message=" + UriUtils.encode("Por favor, selecione um arquivo para enviar.", StandardCharsets.UTF_8);
        }

        return "redirect:/success?message=" + UriUtils.encode(message, StandardCharsets.UTF_8);
    }

    private UploadFileResponseVO uploadSingleFile(MultipartFile file, String folderPath) throws Exception {
        String fileName = fileStorageService.storeFile(file, folderPath); // Passa o folderPath para o serviço de armazenamento
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toString();
        return new UploadFileResponseVO(fileName, fileDownloadUri, file.getContentType(), file.getSize());
    }

    private List<UploadFileResponseVO> uploadMultipleFiles(MultipartFile[] files, String folderPath) throws Exception {
        List<UploadFileResponseVO> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = fileStorageService.storeFile(file, folderPath); // Passa o folderPath para o serviço de armazenamento
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/downloadFile/")
                    .path(fileName)
                    .toString();
            responses.add(new UploadFileResponseVO(fileName, fileDownloadUri, file.getContentType(), file.getSize()));
        }
        return responses;
    } */
    @PostMapping("/uploadFiles")
    @ResponseBody
    public ResponseEntity<String> uploadFiles(@RequestParam(value = "file", required = false) MultipartFile file,
                                              @RequestParam(value = "files", required = false) MultipartFile[] files,
                                              @RequestParam("folderPath") String folderPath) throws Exception {
        String message;

        if (file != null && !file.isEmpty()) {
            // Verifica se o arquivo é válido com base no tipo MIME
            if (!FileValidator.isValidFile(file)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Tipo de arquivo não permitido.");
            }

            // Lógica para upload de um único arquivo
            UploadFileResponseVO response = uploadSingleFile(file, folderPath);
            message = "Arquivo enviado com sucesso: " + response.getFileName();
        } else if (files != null && files.length > 0) {
            // Valida os arquivos múltiplos com base no tipo MIME
            for (MultipartFile fileToCheck : files) {
                if (!FileValidator.isValidFile(fileToCheck)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Tipo de arquivo não permitido.");
                }
            }

            // Lógica para upload de múltiplos arquivos
            List<UploadFileResponseVO> responses = uploadMultipleFiles(files, folderPath);
            message = "Arquivos enviados com sucesso: " + responses.stream()
                    .map(UploadFileResponseVO::getFileName)
                    .collect(Collectors.joining(", "));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Por favor, selecione um arquivo para enviar.");
        }

        // Retorna uma resposta de sucesso com código 200 OK
        return ResponseEntity.ok(message);
    }

    private UploadFileResponseVO uploadSingleFile(MultipartFile file, String folderPath) throws Exception {
        String fileName = fileStorageService.storeFile(file, folderPath);
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponseVO(fileName, fileDownloadUri, file.getContentType(), file.getSize());
    }

    private List<UploadFileResponseVO> uploadMultipleFiles(MultipartFile[] files, String folderPath) throws Exception {
        List<UploadFileResponseVO> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            responses.add(uploadSingleFile(file, folderPath));
        }
        return responses;
    }



    @GetMapping("/success")
        public String successPage(@RequestParam String message, Model model) {
            model.addAttribute("message", message);
            return "success"; // Nome do arquivo HTML sem a extensão
        }

        /* @GetMapping("/error")
        public String errorPage(@RequestParam String message, Model model) {
            model.addAttribute("message", message);
            return "error"; // Nome do arquivo HTML sem a extensão
        } */
        @GetMapping("/downloadFile/**")
        public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {
            String uri = request.getRequestURI(); // /storage/downloadFile/Finalizados/produ%C3%A7%C3%A3o/arquivo.mp4
            String basePath = "/storage/downloadFile/";
            String encodedPath = uri.substring(uri.indexOf(basePath) + basePath.length());

            // Decodifica o caminho para tratar corretamente acentos e caracteres especiais
            String decodedPath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8);

            Resource resource = fileStorageService.loadFileAsResource(decodedPath);
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


    /* metodo funcional 08/05 @GetMapping("/viewFile/**")
    public ResponseEntity<Resource> viewFile(HttpServletRequest request) {
        try {
            String requestURI = request.getRequestURI();
            String basePath = "/storage/viewFile/";
            String filePathEncoded = requestURI.substring(requestURI.indexOf(basePath) + basePath.length());

            // Decodifica caracteres especiais como %C3%A7 para "ç"
            String filePath = URLDecoder.decode(filePathEncoded, StandardCharsets.UTF_8);

            Resource resource = fileStorageService.loadFileAsResource(filePath);

            if (resource == null || !resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    } */
    @GetMapping("/viewFile/**")
    public ResponseEntity<Resource> viewFile(HttpServletRequest request) {
        try {
            String requestURI = request.getRequestURI();
            String basePath = "/storage/viewFile/";
            String filePathEncoded = requestURI.substring(requestURI.indexOf(basePath) + basePath.length());

            // Decodifica o caminho do arquivo
            String filePath = URLDecoder.decode(filePathEncoded, StandardCharsets.UTF_8);

            // Carrega o arquivo como recurso
            Resource resource = fileStorageService.loadFileAsResource(filePath);

            if (resource == null || !resource.exists()) {
                return ResponseEntity.notFound().build();  // Retorna 404 se o arquivo não for encontrado
            }

            // Determina o tipo MIME do arquivo
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (contentType == null) {
                contentType = "application/octet-stream";  // Tipo genérico se não encontrado
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);  // Retorna o arquivo como resposta

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();  // Retorna erro interno em caso de falha
        }
    }
    @GetMapping("/shared/view/**")
    public ResponseEntity<Resource> sharedViewFile(HttpServletRequest request) {
        try {
            String requestURI = request.getRequestURI();
            String basePath = "/storage/shared/view/";
            String filePathEncoded = requestURI.substring(requestURI.indexOf(basePath) + basePath.length());

            // Decodifica o caminho
            String relativePath = URLDecoder.decode(filePathEncoded, StandardCharsets.UTF_8);

            // ✅ Removido o "uploads/" do início, se estiver presente
            if (relativePath.startsWith("uploads/")) {
                relativePath = relativePath.substring("uploads/".length());
            } else if (relativePath.startsWith("uploads\\")) {
                relativePath = relativePath.substring("uploads\\".length());
            }

            Resource resource = fileStorageService.loadFileAsResource(relativePath);
            if (resource == null || !resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/bulk-download")
    public ResponseEntity<Resource> bulkDownload(@RequestParam("selectedFiles") List<String> selectedFiles) throws IOException {
        File tempZip = File.createTempFile("arquivos-", ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZip))) {
            for (String fileName : selectedFiles) {
                // Remove o prefixo "uploads/" se existir para evitar duplicidade
                if (fileName.startsWith("uploads/")) {
                    fileName = fileName.substring("uploads/".length());
                } else if (fileName.startsWith("/uploads/")) {
                    fileName = fileName.substring("/uploads/".length());
                }

                Resource resource = fileStorageService.loadFileAsResource(fileName);
                if (resource.exists()) {
                    zos.putNextEntry(new ZipEntry(resource.getFilename()));
                    try (InputStream is = resource.getInputStream()) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = is.read(buffer)) >= 0) {
                            zos.write(buffer, 0, length);
                        }
                    }
                    zos.closeEntry();
                }
            }
        }

        Resource zipResource = new DeletableInputStreamResource(tempZip);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"arquivos.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(tempZip.length())
                .body(zipResource);
    }




    //rota para renderizar o create Folder
    @GetMapping("/createFolderForm")
    public String showCreateFolderForm() {
        logger.info("Acessando o formulário de criação de pasta");
        return "createFolderForm"; // Nome do template HTML do formulário
    }

    @PostMapping("/createFolder")
     /* public ResponseEntity<String> createFolder(@RequestParam("folderName") String folderName) {
        boolean isCreated = fileStorageService.createFolder(folderName);
        if (isCreated) {
            return ResponseEntity.ok("Pasta criada com sucesso: " + folderName);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Essa pasta já existe:  " + folderName);
        }
    } */
    public String createFolder(
            @RequestParam("folderName") String folderName,
            @RequestParam(value = "currentFolder", required = false) String currentFolder,
            RedirectAttributes redirectAttributes
    ) {
        // Substitui as barras invertidas por barras normais para garantir que o caminho esteja correto
        folderName = folderName.replace("\\", "/");

        // Se o currentFolder não for nulo, adiciona o caminho relativo
        String relativePath = (currentFolder == null || currentFolder.isEmpty())
                ? folderName
                : currentFolder + File.separator + folderName;

        // Normaliza o caminho para garantir que ele esteja correto
        relativePath = relativePath.replace("\\", "/"); // Troca barras invertidas por barras normais

        boolean isCreated = fileStorageService.createFolder(relativePath);

        // Define o caminho de redirecionamento
        String redirectPath = (currentFolder != null && !currentFolder.isEmpty())
                ? "/storage/openFolder/" + currentFolder
                : "/";

        // Mensagem de sucesso ou erro ao criar a pasta
        if (isCreated) {
            redirectAttributes.addFlashAttribute("toastMessage", "Pasta criada com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute("toastMessage", "Essa pasta já existe.");
        }

        // Redireciona para a página da pasta ou à raiz
        return "redirect:" + redirectPath;
    }



    /*@GetMapping("/")
    public List<String> listFilesAndFolders() {
        Path rootLocation = fileListService.getRootLocation(); // Obtém o diretório raiz
        return fileListService.listFilesInDirectory(Path.of(String.valueOf(rootLocation))); // Usa o FileListService para listar arquivos e pastas
    } */
    @GetMapping("/")
    public List<FileResponseVO> listFilesAndFolders() {
        Path rootLocation = fileListService.getRootLocation();  // Obtém o diretório raiz
        List<String> fileNames = fileListService.listFilesInDirectory(Path.of(String.valueOf(rootLocation))); // Obtém os nomes dos arquivos e pastas

        // Mapeia a lista de nomes de arquivos e pastas para FileResponseVO
        List<FileResponseVO> fileResponseList = new ArrayList<>();
        for (String fileName : fileNames) {
            boolean isDirectory = Files.isDirectory(Path.of(fileName));  // Verifica se é diretório
            String downloadUrl = "/storage/downloadFile/" + fileName;  // Exemplo de URL para download
            String publicUrl = "/storage/viewFile/" + fileName;  // Exemplo de URL pública

            // Cria o FileResponseVO e adiciona à lista
            FileResponseVO fileResponseVO = new FileResponseVO(fileName, isDirectory, downloadUrl, publicUrl);
            fileResponseList.add(fileResponseVO);
        }

        return fileResponseList;  // Retorna a lista de objetos FileResponseVO
    }
    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteFileOrFolder(@RequestBody Map<String, String> requestBody) {
        try {
            String path = requestBody.get("path");
            logger.info("Recebido para exclusão: " + path);  // Adicione este log

            Path targetPath = Paths.get(fileStorageService.getFileStorageLocation().toString(), path);

            if (Files.exists(targetPath)) {
                if (Files.isDirectory(targetPath)) {
                    fileStorageService.moveFolderToTrash(path);
                } else {
                    fileStorageService.moveFileToTrash(path);
                }
                return ResponseEntity.ok().body(new HashMap<String, String>() {{
                    put("message", "Arquivo ou pasta movido(a) para a lixeira com sucesso.");
                }});
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<String, String>() {{
                    put("error", "Arquivo ou pasta não encontrado(a).");
                }});
            }
        } catch (Exception e) {
            logger.error("Erro ao deletar arquivo ou pasta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Erro ao mover o arquivo ou pasta para a lixeira: " + e.getMessage());
            }});
        }
    }


    private boolean isAdmin(Principal principal) {
        // Substitua essa verificação pelo seu controle real de usuários
        return principal != null && principal.getName().equals("admin");
    }

    @GetMapping("/folders")
    public ResponseEntity<List<String>> listAllFolders() {
        Path root = fileListService.getRootLocation();
        List<String> folders = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isDirectory)
                    .filter(path -> !path.equals(root)) // ignora o root
                    .forEach(path -> folders.add(root.relativize(path).toString().replace("\\", "/"))); // normaliza
            return ResponseEntity.ok(folders);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/moveFile")
    public ResponseEntity<?> moveFile(@RequestParam String fullPath, @RequestParam String newFolder) {
        try {
            Path root = fileListService.getRootLocation();
            Path sourcePath = root.resolve(fullPath);
            String fileName = sourcePath.getFileName().toString();
            Path targetPath = root.resolve(newFolder).resolve(fileName);

            Files.createDirectories(targetPath.getParent());
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok("Arquivo movido com sucesso!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao mover o arquivo: " + e.getMessage());
        }
    }
    @PatchMapping("/moveFolder")
    public ResponseEntity<?> moveFolder(@RequestParam String fullPath, @RequestParam String newParentFolder) {
        try {
            Path rootLocation = fileListService.getRootLocation();
            Path sourcePath = rootLocation.resolve(fullPath).normalize();
            Path targetPath = rootLocation.resolve(newParentFolder).resolve(sourcePath.getFileName()).normalize();

            if (!Files.exists(sourcePath) || !Files.isDirectory(sourcePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pasta de origem não encontrada.");
            }

            if (!Files.exists(targetPath.getParent())) {
                Files.createDirectories(targetPath.getParent());
            }

            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok("Pasta movida com sucesso.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao mover a pasta: " + e.getMessage());
        }
    }
    @GetMapping("/downloadFolder")
    public ResponseEntity<Resource> downloadFolder(@RequestParam String folderPath) {
        try {
            Path rootLocation = fileListService.getRootLocation();
            Path folderToZip = rootLocation.resolve(Paths.get(folderPath)).normalize();

            if (!folderToZip.startsWith(rootLocation)) {
                // Tentativa de acessar fora da pasta raiz
                return ResponseEntity.badRequest().build();
            }


            Path zipPath = Files.createTempFile("folder-", ".zip");

            try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                Files.walkFileTree(folderToZip, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path relativePath = folderToZip.relativize(file);
                        ZipEntry zipEntry = new ZipEntry(folderToZip.getFileName() + "/" + relativePath.toString());
                        System.out.println("Adicionando arquivo: " + file);
                        zs.putNextEntry(zipEntry);
                        Files.copy(file, zs);
                        zs.closeEntry();
                        return FileVisitResult.CONTINUE;
                    }
                });
            }


            Resource resource = new UrlResource(zipPath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + folderToZip.getFileName() + ".zip\"")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/trash")
    public String showTrash(Model model) throws IOException {
        // Buscar os arquivos e pastas na lixeira
        List<String> trashFiles = fileStorageService.getFilesInTrash();
        List<String> trashFolders = fileStorageService.getFoldersInTrash();

        // Adiciona os arquivos e pastas da lixeira ao modelo
        model.addAttribute("trashFiles", trashFiles);
        model.addAttribute("trashFolders", trashFolders);
        return "trash"; // Nome do template HTML para exibir a lixeira
    }

    @PostMapping("/restoreFromTrash")
    public ResponseEntity<?> restoreFileFromTrash(@RequestParam String filePath) {
        try {
            fileStorageService.restoreFileFromTrash(filePath);  // Serviço que move o arquivo de volta para o local original
            return ResponseEntity.ok("Arquivo restaurado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao restaurar arquivo: " + e.getMessage());
        }
    }

    @PostMapping("/restoreFolderFromTrash")
    public ResponseEntity<?> restoreFolderFromTrash(@RequestParam String folderPath) {
        try {
            fileStorageService.restoreFolderFromTrash(folderPath);  // Serviço que move a pasta de volta para o local original
            return ResponseEntity.ok("Pasta restaurada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao restaurar pasta: " + e.getMessage());
        }
    }
    // Deleta definitivamente um arquivo da lixeira
    @PostMapping("/deleteFromTrash")
    public ResponseEntity<?> deleteFileFromTrash(@RequestParam String filePath) {
        try {
            fileStorageService.deleteFileFromTrash(filePath);  // Serviço que exclui o arquivo permanentemente
            return ResponseEntity.ok("Arquivo deletado permanentemente!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao deletar arquivo: " + e.getMessage());
        }
    }

    // Deleta definitivamente uma pasta da lixeira
    @PostMapping("/deleteFolderFromTrash")
    public ResponseEntity<?> deleteFolderFromTrash(@RequestParam String folderPath) {
        try {
            fileStorageService.deleteFolderFromTrash(folderPath);  // Serviço que exclui a pasta permanentemente
            return ResponseEntity.ok("Pasta deletada permanentemente!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao deletar pasta: " + e.getMessage());
        }
    }

}




