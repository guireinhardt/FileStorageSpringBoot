package com.guireinhardt.FileStorage.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
@PropertySource("classpath:keywords.properties") // Lê o arquivo keywords.properties
public class KeywordConfig {

    @Value("${keywords}")
    private String keywords;

    private List<String> keywordList;

    @PostConstruct
    public void init() {
        // Remove espaços em branco e gera a lista de keywords
        keywordList = Arrays.stream(keywords.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public List<String> getKeywordList() {
        return keywordList;
    }
}

