package com.secretaria.FileStorage;

import com.secretaria.FileStorage.config.FileStorageConfig;
import com.secretaria.FileStorage.config.KeywordConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({FileStorageConfig.class})
public class FileStorageApplication {


	public static void main(String[] args) {
		SpringApplication.run(FileStorageApplication.class, args);
	}

}


