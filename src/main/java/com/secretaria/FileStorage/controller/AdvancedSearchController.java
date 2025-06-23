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
    private  KeywordService keywordService;

    @GetMapping("/advanced/subkeywords")
    @ResponseBody
    public List<String> getSubkeywordsP(@RequestParam String keyword) {
        String normalizedKeyword = StringUtils.normalize(keyword); // Normaliza a palavra-chave
        return keywordService.getKeywordMap().getOrDefault(normalizedKeyword, List.of());
    }

    @GetMapping("")
    public String showForm(Model model) {
        // Preenche o modelo com dados comuns
        preencherModelPadrao(model);

        // Passa dados para o modelo para serem usados na view
        model.addAttribute("query", "");
        model.addAttribute("selectedCity", "");
        model.addAttribute("selectedInstitute", "");
        model.addAttribute("selectedDate", null);
        model.addAttribute("selectedFolders", List.of());
        model.addAttribute("files", null);
        model.addAttribute("foldersSet", Collections.emptySet());

        // Passa as palavras-chave e subpalavras-chave para a view
        model.addAttribute("displayMap", keywordService.getDisplayMap());
        model.addAttribute("keyword", ""); // valor inicial do select
        model.addAttribute("selectedSubkeywords", List.of()); // Inicialmente nenhuma subpalavra selecionada

        return "advancedSearch"; // Ou o nome do template que você está usando
    }


    @PostMapping("")
    public String search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String city,
            @RequestParam(required = false, name = "folders") List<String> selectedFolders,
            @RequestParam(required = false) String institute,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> subkeywords, // Subpalavras-chave
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) throws IOException {

        // Verifica se há filtros aplicados
        boolean hasFilters = (query != null && !query.isBlank())
                || (keyword != null && !keyword.isBlank())
                || (subkeywords != null && !subkeywords.isEmpty())
                || (institute != null && !institute.isBlank())
                || (city != null && !city.isBlank())
                || startDate != null
                || endDate != null;

        List<FileResultDTO> results = new ArrayList<>();

        // Se houver pastas selecionadas, carrega os arquivos dentro delas, independente da pesquisa
        if (selectedFolders != null && !selectedFolders.isEmpty()) {
            results = fileSearchService.searchFilesByFolders(null, city, selectedFolders);
        } else if (hasFilters) {
            results = fileSearchService.searchFiles(query, keyword, subkeywords, institute, city, startDate, endDate);
        }

        // Adiciona palavras-chave e subpalavras-chave no modelo
        List<String> keywords = new ArrayList<>(keywordService.getDisplayMap().keySet());
        Map<String, List<String>> subkeywordsMap = keywordService.getKeywordMap();

        model.addAttribute("folders", folderService.getFolderTree());  // Certifique-se de que as pastas estão carregadas após a busca

        model.addAttribute("files", results);
        model.addAttribute("hasFilters", hasFilters);

        // Passa as palavras-chave, subpalavras-chave e dados de filtro para o modelo
        model.addAttribute("keywords", keywords);
        model.addAttribute("displayMap", keywordService.getDisplayMap());
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("subkeywordsMap", subkeywordsMap);
        model.addAttribute("selectedSubkeywords", subkeywords != null ? subkeywords : new ArrayList<>());

        // Passa cidades e institutos
        model.addAttribute("cities", cityService.getAllCities());
        model.addAttribute("selectedCity", city);

        model.addAttribute("institutes", instituteService.getAllInstitutes());
        model.addAttribute("selectedInstitute", institute);

        // Passa as datas
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        // Passa as pastas selecionadas para manter o estado delas
        model.addAttribute("selectedFolders", selectedFolders);

        return "advancedSearch"; // Ou o nome do template que você está usando
    }

    private void preencherModelPadrao(Model model) {
        model.addAttribute("cities", cityService.getAllCities());
        model.addAttribute("institutes", instituteService.getAllInstitutes());
        model.addAttribute("folders", folderService.getFolderTree());
    }



}

