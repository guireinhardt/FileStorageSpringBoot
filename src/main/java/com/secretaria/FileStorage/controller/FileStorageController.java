package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.service.FileStorageService;
import com.secretaria.FileStorage.vo.UploadFileResponseVO;
import jakarta.servlet.Servlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/")
public class FileStorageController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/uploadFile")
    public UploadFileResponseVO uploadfile(@RequestParam("file") MultipartFile file){
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponseVO(fileName,fileDownloadUri,file.getContentType(),file.getSize());
    }
}
