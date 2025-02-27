package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.exception.FileStorageException;
import com.secretaria.FileStorage.service.FileListService;
import com.secretaria.FileStorage.service.FileStorageService;
import com.secretaria.FileStorage.vo.FileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestParameterPropertyValues;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;



@Controller
public class FileViewController {

    @Autowired
    private FileListService fileListService;
    @Autowired
    private FileStorageService fileStorageService;


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
}