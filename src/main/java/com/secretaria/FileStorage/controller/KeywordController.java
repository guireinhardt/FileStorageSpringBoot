package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.config.KeywordConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class KeywordController {

    @Autowired
    private KeywordConfig keywordConfig;

    @GetMapping("/add-keyword")
    public String showAddKeywordPage() {
        return "add-keyword"; // Retorna a página para adicionar palavras-chave
    }

    @PostMapping("/add-keyword")
    public String addKeyword(@RequestParam String keyword, Model model) {
        try {
            // Adiciona a nova palavra-chave ao arquivo de propriedades
            List<String> keywords = new ArrayList<>(keywordConfig.getKeywordList());
            keywords.add(keyword);

            // Atualiza o arquivo de propriedades
            String keywordsString = String.join(",", keywords);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/keywords.properties"))) {
                writer.write("keywords=" + keywordsString);
            }

            model.addAttribute("message", "Palavra-chave adicionada com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("message", "Erro ao adicionar a palavra-chave.");
        }
        return "add-keyword"; // Retorna à página de adicionar palavras-chave
    }
}