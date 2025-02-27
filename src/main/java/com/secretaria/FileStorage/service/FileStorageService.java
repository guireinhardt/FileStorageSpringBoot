package com.secretaria.FileStorage.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.secretaria.FileStorage.config.FileStorageConfig;
import com.secretaria.FileStorage.exception.FileStorageException;
import com.secretaria.FileStorage.exception.FileStorageNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    @Autowired
    public FileStorageService(FileStorageConfig fileStorageConfig) {
        this.fileStorageLocation = Paths.get(fileStorageConfig.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception e ){
            throw  new FileStorageException("Não foi possivel criar o diretório",e);
        }
    }

    /* public String storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (fileName.contains("..")){
                throw  new FileStorageException("Desculpe, o nome do arquivo está incorreto " + fileName );
            }
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(),targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return  fileName;
        }catch (Exception e){
            throw  new FileStorageException("Não foi possivel salvar o arquivo" + fileName + ". Tente Novamente",e);
        }
    } */
    public String storeFile(MultipartFile file, String folderPath) throws IOException {
        Path destinationFile = this.fileStorageLocation.resolve(Paths.get(folderPath)).resolve(file.getOriginalFilename()).normalize().toAbsolutePath();
        Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
        return file.getOriginalFilename();
    }


    private String validateFolderPath(String folderPath) {
        if (folderPath == null || folderPath.isEmpty()) {
            return fileStorageLocation.toString(); // Retorna o diretório base se nenhum caminho for fornecido
        }

        try {
            // Cria um Path a partir do folderPath fornecido
            Path path = Paths.get(folderPath).normalize();

            // Verifica se o caminho está dentro do diretório base
            if (!path.startsWith(fileStorageLocation)) {
                return null; // Caminho inválido
            }

            // Verifica se o diretório existe, se não, cria
            if (!Files.exists(path)) {
                Files.createDirectories(path); // Cria o diretório se não existir
            }

            return path.toString(); // Retorna o caminho validado
        } catch (IOException e) {
            return null; // Retorna null se ocorrer um erro
        }
    }
    public Resource loadFileAsResource( String fileName){
        try{
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()){
                return  resource;
            }else {
                throw new FileStorageNotFoundException("File not found " + fileName );
            }

        } catch (Exception e) {
            throw new FileStorageNotFoundException("File not found " + fileName,e );
        }
    }
    public boolean createFolder(String folderName) {
        try {
            File folder = new File(fileStorageLocation + File.separator + folderName);
            if (!folder.exists()) {
                return folder.mkdirs(); // Cria a pasta e todas as pastas necessárias
            } else {
                return false; // A pasta já existe
            }
        } catch (Exception e) {
            // Log de erro
            return false;
        }
    }
}
