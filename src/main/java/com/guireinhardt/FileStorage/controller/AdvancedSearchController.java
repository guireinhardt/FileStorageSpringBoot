package com.guireinhardt.FileStorage.controller;

import com.guireinhardt.FileStorage.config.KeywordConfig;
import com.guireinhardt.FileStorage.entity.KeywordEntity;
import com.guireinhardt.FileStorage.entity.SubkeywordEntity;
import com.guireinhardt.FileStorage.service.*;
import com.guireinhardt.FileStorage.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/advanced-search")
public class AdvancedSearchController {

    @Autowired
    private FileListService fileListService;

    @Autowired
    private CityService cityService;
    @Autowired
    private InstituteService instituteService;
    @Autowired
    private FolderService folderService;
    @Autowired
    private FileSearchService fileSearchService;
    @Autowired
    private KeywordConfig keywordConfig;
    @Autowired
    private KeywordService keywordService;

    @GetMapping("/advanced/subkeywords")
    @ResponseBody
    public List<String> getSubkeywordsP(@RequestParam String keyword) {
        String normalizedKeyword = StringUtils.normalize(keyword); // Normaliza a palavra-chave
        // Busca a palavra-chave pela normalização e obtém suas subpalavras
        List<String> subkeywords = keywordService.getAllKeywords().stream()
                .filter(k -> StringUtils.normalize(k.getPalavra()).equals(normalizedKeyword))
                .flatMap(k -> k.getSubkeywords().stream())
                .map(SubkeywordEntity::getPalavra)
                .collect(Collectors.toList());
        return subkeywords;
    }
    @GetMapping("")
    public String showForm(Model model) {
        // Preenche o modelo com dados comuns
        preencherModelPadrao(model);
        String selectedKeyword = "";

        // Carrega todas as palavras-chave e suas subpalavras
        List<KeywordEntity> allKeywords = keywordService.getAllKeywords();
        List<String> keywords = new ArrayList<>();
        Map<String, List<String>> subkeywordsMap = new HashMap<>();

        // Preenche as palavras-chave e subpalavras no modelo
        for (KeywordEntity k : allKeywords) {
            keywords.add(k.getPalavra());
            subkeywordsMap.put(k.getPalavra(), k.getSubkeywords().stream()
                    .map(SubkeywordEntity::getPalavra)
                    .collect(Collectors.toList()));
        }

        // Passa dados para o modelo para serem usados na view
        model.addAttribute("query", "");
        model.addAttribute("selectedCity", "");
        model.addAttribute("selectedInstitute", "");
        model.addAttribute("selectedDate", null);
        model.addAttribute("selectedFolders", List.of());
        model.addAttribute("files", null);
        model.addAttribute("foldersSet", Collections.emptySet());

        model.addAttribute("keywords", keywords);
        model.addAttribute("subkeywordsMap", subkeywordsMap);
        model.addAttribute("selectedKeyword", selectedKeyword);
        model.addAttribute("selectedSubkeywords", new ArrayList<>());


        return "advancedSearch"; // Ou o nome do template que você está usando
    }



    @PostMapping("")
    public String search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String city,
            @RequestParam(required = false, name = "folders") List<String> selectedFolders,
            @RequestParam(required = false) String institute,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, name = "selectedSubkeywords") List<String> selectedSubkeywords,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) throws IOException {

        // Preenche as palavras-chave e subpalavras
        List<String> keywords = new ArrayList<>();
        Map<String, List<String>> subkeywordsMap = new HashMap<>();

        if (keyword != null && !keyword.isBlank()) {
            keywords.add(keyword);
            subkeywordsMap.put(keyword, keywordService.getAllKeywords().stream()
                    .filter(k -> k.getPalavra().equals(keyword))
                    .flatMap(k -> k.getSubkeywords().stream())
                    .map(SubkeywordEntity::getPalavra)
                    .collect(Collectors.toList()));
        } else {
            List<KeywordEntity> allKeywords = keywordService.getAllKeywords();
            for (KeywordEntity k : allKeywords) {
                keywords.add(k.getPalavra());
                subkeywordsMap.put(k.getPalavra(), k.getSubkeywords().stream()
                        .map(SubkeywordEntity::getPalavra)
                        .collect(Collectors.toList()));
            }
        }

        // Passa os dados para o modelo
        model.addAttribute("folders", folderService.getFolderTree());
        model.addAttribute("files", fileSearchService.searchFiles(query, keyword, selectedSubkeywords, institute, city, startDate, endDate));
        model.addAttribute("keywords", keywords);
        model.addAttribute("subkeywordsMap", subkeywordsMap);
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedSubkeywords", selectedSubkeywords != null ? selectedSubkeywords : new ArrayList<>());
        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedInstitute", institute);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedFolders", selectedFolders);

        return "advancedSearch";
    }









    private void preencherModelPadrao(Model model) {
        model.addAttribute("cities", cityService.getAllCities());
        model.addAttribute("institutes", instituteService.getAllInstitutes());
        model.addAttribute("folders", folderService.getFolderTree());
    }



}

