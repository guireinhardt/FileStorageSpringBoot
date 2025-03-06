package com.secretaria.FileStorage.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
@PropertySource("classpath:keywords.properties") // Especifica o arquivo de propriedades
public class KeywordConfig {

    @Value("${keywords}")
    private String keywords;

    private List<String> keywordList;

    @PostConstruct
    public void init() {
        keywordList = Arrays.asList(keywords.split(","));
    }

    public List<String> getKeywordList() {
        return Stream.of(keywords.split(","))
                .collect(Collectors.toList());
    }
}
