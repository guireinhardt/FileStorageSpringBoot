package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.dto.FileResultDTO;
import com.secretaria.FileStorage.service.*;
import com.secretaria.FileStorage.vo.FileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

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


    @GetMapping("")
    public String showForm(Model model) {
        preencherModelPadrao(model);
        model.addAttribute("query", "");
        model.addAttribute("selectedCity", "");
        model.addAttribute("selectedInstitute", "");
        model.addAttribute("selectedDate", null);
        model.addAttribute("selectedFolders", List.of());
        model.addAttribute("files", null);
        return "advancedSearch";
    }

    @PostMapping("")
    public String search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String city,
            @RequestParam(required = false, name = "folders") List<String> selectedFolders,
            @RequestParam(required = false) String institute,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) throws IOException {

        List<FileResultDTO> files;

        boolean hasFolders = selectedFolders != null && !selectedFolders.isEmpty();

        if (hasFolders) {
            // Busca restrita às pastas selecionadas
            files = fileSearchService.searchFilesByFolders(query, city, selectedFolders);
        } else {
            // Busca global com todos os filtros
            files = fileSearchService.searchFiles(query, keyword, institute, city, startDate, endDate);
        }

        preencherModelPadrao(model);

        model.addAttribute("query", query);
        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedInstitute", institute);
        model.addAttribute("selectedDate", startDate); // ou use um objeto que combine startDate e endDate na view
        model.addAttribute("selectedFolders", selectedFolders);
        model.addAttribute("files", files);

        return "advancedSearch";
    }

    private void preencherModelPadrao(Model model) {
        model.addAttribute("cities", cityService.getAllCities());
        model.addAttribute("institutes", instituteService.getAllInstitutes());
        model.addAttribute("folders", folderService.getFolderTree());
    }



}

