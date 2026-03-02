package com.secretaria.FileStorage.config;

import com.secretaria.FileStorage.entity.FolderEntity;
import com.secretaria.FileStorage.repository.FolderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedFolders(FolderRepository folderRepository) {
        return args -> {
            createIfMissing(folderRepository, "01.BRUTOS", false);
            createIfMissing(folderRepository, "02.FINALIZADOS", true);  // público
            createIfMissing(folderRepository, "03.PRODUCAO", false);
        };
    }

    private void createIfMissing(FolderRepository repo, String name, boolean isPublic) {
        repo.findByName(name).orElseGet(() -> repo.save(new FolderEntity(name, isPublic, null)));
    }
}