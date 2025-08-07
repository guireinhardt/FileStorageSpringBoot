package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.entity.KeywordEntity;
import com.secretaria.FileStorage.service.KeywordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/keywords")
public class KeywordController {

    @Autowired
    private KeywordService keywordService;

    // Exibe a página com a lista de palavras-chave e suas subpalavras
    @GetMapping
    public String showKeywordsPage(Model model) {
        // Carrega as palavras-chave e suas subpalavras
        List<KeywordEntity> keywords = keywordService.getAllKeywords();

        // Adiciona as palavras-chave ao modelo
        model.addAttribute("keywords", keywords);
        return "keywords"; // O template "keywords.html" será exibido
    }

    // Adiciona uma nova palavra-chave e suas subpalavras
    @PostMapping("/add")
    public String addKeyword(@RequestParam String keyword, @RequestParam List<String> subKeywords) {
        keywordService.addKeyword(keyword, subKeywords);
        return "redirect:/keywords"; // Redireciona para a página de palavras-chave
    }

    // Adiciona uma subpalavra a uma palavra-chave existente
    @PostMapping("/addSub")
    public String addSubKeyword(@RequestParam String keyword, @RequestParam String subKeyword) {
        keywordService.addSubKeyword(keyword, subKeyword);
        return "redirect:/keywords"; // Redireciona para a página de palavras-chave
    }
}
