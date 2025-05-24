package com.secretaria.FileStorage.controller;

import com.secretaria.FileStorage.dto.FileResultDTO;
import com.secretaria.FileStorage.service.CityService;
import com.secretaria.FileStorage.service.FileListService;
import com.secretaria.FileStorage.service.FileSearchService;
import com.secretaria.FileStorage.service.FolderService;
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
    private FolderService folderService;
    @Autowired
    private FileSearchService fileSearchService;


        @GetMapping("")
        public String showForm(Model model) {
            model.addAttribute("cities", cityService.getAllCities());
            model.addAttribute("folders", folderService.getFolderTree()); // retorna lista com subpastas aninhadas
            return "advancedSearch";
        }

    @PostMapping("")
    public String search(
            @RequestParam(required=false) String query,
            @RequestParam(required=false) String city,
            @RequestParam(required=false, name = "folders") List<String> selectFolders,
            Model model) throws IOException {

        System.out.println("query: " + query);
        System.out.println("city: " + city);
        System.out.println("folders: " + selectFolders);

        List<FileResultDTO> files = fileSearchService.searchFilesByFolders(query, city, selectFolders);

        model.addAttribute("files", files);
        model.addAttribute("cities", cityService.getAllCities());
        model.addAttribute("folders", folderService.getFolderTree());
        model.addAttribute("query", query);
        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedFolders", selectFolders);

        return "advancedSearch";
    }


}

