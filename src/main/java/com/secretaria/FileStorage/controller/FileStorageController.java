package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.service.FileStorageService;
import com.secretaria.FileStorage.utils.FileValidator;
import com.secretaria.FileStorage.vo.FileResponseVO;
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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
    public ModelAndView createFolder(@RequestParam("folderName") String folderName) {
        boolean isCreated = fileStorageService.createFolder(folderName);
        ModelAndView modelAndView = new ModelAndView("folderResult"); // Nome do template HTML

        if (isCreated) {
            modelAndView.addObject("message", "Pasta criada com sucesso: " + folderName);
        } else {
            modelAndView.addObject("message", "Essa pasta já existe: " + folderName);
        }

        return modelAndView;
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

}


