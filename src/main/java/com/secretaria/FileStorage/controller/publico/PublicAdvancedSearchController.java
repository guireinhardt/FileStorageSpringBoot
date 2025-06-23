package com.secretaria.FileStorage.controller.publico;

import com.secretaria.FileStorage.config.KeywordConfig;
import com.secretaria.FileStorage.dto.FileResultDTO;
import com.secretaria.FileStorage.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class PublicAdvancedSearchController {
    @Autowired
    private FileSearchService fileSearchService;

    @Autowired
    private KeywordConfig keywordConfig;

    @Autowired
    private FolderService folderService;

    @Autowired
    private KeywordService keywordService;

    @Autowired
    private FileListService fileListService;

    @Autowired
    private InstituteService instituteService;

    @Autowired
    private CityService cityService;


    @GetMapping("/advanced-public")
    public String showPublicForm(Model model) {
        // Pega as chaves normalizadas (exemplo: "maquinas", "plantacao")
        Set<String> keywords = keywordService.getKeywordMap().keySet();

        // Pega o map para exibição (chave com acento)
        Map<String, String> displayMap = keywordService.getDisplayMap();

        // Passa as subpalavras para a view (mapa chave->lista de subpalavras)
        Map<String, List<String>> subkeywordsMap = keywordService.getKeywordMap();

        model.addAttribute("query", "");
        model.addAttribute("selectedFolders", List.of());
        model.addAttribute("files", null);
        model.addAttribute("foldersSet", Collections.emptySet());
        model.addAttribute("keywords", keywords);
        model.addAttribute("displayMap", displayMap);
        model.addAttribute("subkeywordsMap", subkeywordsMap);
        model.addAttribute("keyword", "");
        model.addAttribute("selectedSubkeywords", List.of());

        return "public/public-search";
    }

    @PostMapping("/advanced-public")
    public String searchPublicFiles(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, name = "subkeywords") List<String> selectedSubkeywords,
            @RequestParam(required = false) String institute,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Model model) throws IOException {

        Path publicRoot = Paths.get(fileListService.getRootLocation().toString(), "02.FINALIZADOS");

        // Chama o método do serviço com os parâmetros adicionais
        List<FileResultDTO> files = fileSearchService.searchFilesWithinRoot(
                query,
                keyword,
                selectedSubkeywords,
                institute,   // Passa o instituto
                city,         // Passa a cidade
                startDate,    // Passa a data de início
                endDate,      // Passa a data de fim
                publicRoot
        );

        // Passa as listas de institutos e cidades para o modelo
        model.addAttribute("institutes", instituteService.getAllInstitutes());
        model.addAttribute("cities", cityService.getAllCities());

        Set<String> foldersSet = files.stream()
                .map(FileResultDTO::getFullPath)
                .collect(Collectors.toSet());

        Set<String> keywords = keywordService.getKeywordMap().keySet();
        Map<String, String> displayMap = keywordService.getDisplayMap();
        Map<String, List<String>> subkeywordsMap = keywordService.getKeywordMap();

        model.addAttribute("query", query);
        model.addAttribute("files", files);
        model.addAttribute("foldersSet", foldersSet);
        model.addAttribute("keywords", keywords);
        model.addAttribute("displayMap", displayMap);
        model.addAttribute("subkeywordsMap", subkeywordsMap);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedSubkeywords", selectedSubkeywords != null ? selectedSubkeywords : List.of());

        return "public/public-search";
    }


    @GetMapping("/subkeywords")
    @ResponseBody
    public List<String> getSubkeywords(@RequestParam String keyword) {
        return keywordService.getKeywordMap().getOrDefault(keyword, List.of());
    }
    @GetMapping("/view-public")
    public ResponseEntity<Resource> viewFile(@RequestParam String path) throws IOException {
        Path filePath = fileListService.getRootLocation().resolve(path).normalize();

        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .body(resource);
    }


    @GetMapping("/download-public")
    public ResponseEntity<Resource> publicDownload(@RequestParam String path) throws IOException {
        Path rootFinalizados = Paths.get(fileListService.getRootLocation().toString(), "02.FINALIZADOS");
        Path filePath = rootFinalizados.resolve(path).normalize();

        if (!filePath.startsWith(rootFinalizados)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath.getFileName() + "\"")
                .body(resource);
    }




}
