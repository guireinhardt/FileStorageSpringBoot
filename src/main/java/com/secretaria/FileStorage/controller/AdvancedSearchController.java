package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.config.KeywordConfig;
import com.secretaria.FileStorage.dto.FileResultDTO;
import com.secretaria.FileStorage.service.*;
import com.secretaria.FileStorage.utils.StringUtils;
import com.secretaria.FileStorage.vo.FileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
    private  KeywordService keywordService;

    @GetMapping("/advanced/subkeywords")
    @ResponseBody
    public List<String> getSubkeywordsP(@RequestParam String keyword) {
        String normalizedKeyword = StringUtils.normalize(keyword); // Normaliza a palavra-chave
        return keywordService.getKeywordMap().getOrDefault(normalizedKeyword, List.of());
    }

    @GetMapping("")
    public String showForm(Model model) {
        preencherModelPadrao(model);

        model.addAttribute("query", "");
        model.addAttribute("selectedCity", "");
        model.addAttribute("selectedInstitute", "");
        model.addAttribute("selectedDate", null);
        model.addAttribute("selectedFolders", List.of());
        model.addAttribute("files", null);
        model.addAttribute("foldersSet", Collections.emptySet());

        // Adiciona as palavras-chave
        model.addAttribute("displayMap", keywordService.getDisplayMap());
        model.addAttribute("keyword", ""); // valor inicial do select

        // Adiciona subpalavras selecionadas como vazio no carregamento inicial
        model.addAttribute("selectedSubkeywords", List.of());

        return "advancedSearch";
    }



    @PostMapping("")
    public String search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String city,
            @RequestParam(required = false, name = "folders") List<String> selectedFolders,
            @RequestParam(required = false) String institute,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> subkeywords, // ADICIONAR AQUI
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) throws IOException {

        List<FileResultDTO> files;

        boolean hasFolders = selectedFolders != null && !selectedFolders.isEmpty();

        if (hasFolders) {
            // Busca restrita às pastas selecionadas
            files = fileSearchService.searchFilesByFolders(query, city, selectedFolders);
        } else {
            // Busca global com todos os filtros (agora com subkeywords)
            files = fileSearchService.searchFiles(query, keyword, subkeywords, institute, city, startDate, endDate);
        }

        // Monta o conjunto de pastas para usar na view
        Set<String> foldersSet = files.stream()
                .filter(f -> {
                    Path p = Paths.get(f.getFullPath());
                    return Files.exists(p) && Files.isDirectory(p);
                })
                .map(FileResultDTO::getFullPath)
                .collect(Collectors.toSet());

        preencherModelPadrao(model);

        model.addAttribute("query", query);
        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedInstitute", institute);
        model.addAttribute("selectedDate", startDate);
        model.addAttribute("selectedFolders", selectedFolders);
        model.addAttribute("files", files);
        model.addAttribute("foldersSet", foldersSet);

        model.addAttribute("keywords", keywordConfig.getKeywordList());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedSubkeywords", subkeywords); // Adicione isso para manter a seleção no form

        return "advancedSearch";
    }



    private void preencherModelPadrao(Model model) {
        model.addAttribute("cities", cityService.getAllCities());
        model.addAttribute("institutes", instituteService.getAllInstitutes());
        model.addAttribute("folders", folderService.getFolderTree());
    }



}

