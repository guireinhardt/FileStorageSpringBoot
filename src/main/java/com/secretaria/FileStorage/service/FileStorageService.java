package com.secretaria.FileStorage.service;

import java.io.File;
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
    public String storeFile(MultipartFile file) throws Exception {
        // Verifica se o arquivo está vazio
        if (file.isEmpty()) {
            throw new Exception("Por favor, selecione um arquivo para enviar.");
        }

        // Validação do tipo de arquivo (opcional)
        String fileType = file.getContentType();

        // Lógica para armazenar o arquivo
        try {
            // Salva o arquivo no diretório especificado
            Path destinationFile = fileStorageLocation.resolve(Paths.get(file.getOriginalFilename()));
            Files.copy(file.getInputStream(), destinationFile);
            return "Arquivo enviado com sucesso: " + file.getOriginalFilename();
        } catch (Exception e) {
            throw new Exception("Erro ao enviar o arquivo: " + e.getMessage());
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
