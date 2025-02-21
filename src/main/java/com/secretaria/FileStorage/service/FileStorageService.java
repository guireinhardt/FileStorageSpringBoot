package com.secretaria.FileStorage.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.secretaria.FileStorage.config.FileStorageConfig;
import com.secretaria.FileStorage.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Autowired;
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

    public String storeFile(MultipartFile file) {
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
    }
}
