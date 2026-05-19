package com.guireinhardt.FileStorage.controller;

import com.guireinhardt.FileStorage.service.StorageCalculator;
import com.guireinhardt.FileStorage.vo.DataPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;  // Alterado para Controller
import org.springframework.ui.Model;  // Adicionado para passar dados ao template
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller  // Garantindo que seja um Controller
@RequestMapping("/report")
public class StorageController {

    @Autowired
    private StorageCalculator storageCalculator;

    @GetMapping("/report")
    public String showReport(Model model) throws IOException {
        List<DataPoint> monthlyGrowth = getMonthlyGrowth();
        List<DataPoint> annualGrowth = getAnnualGrowth();

        // Passando os dados para o template
        model.addAttribute("monthlyGrowth", monthlyGrowth);
        model.addAttribute("annualGrowth", annualGrowth);

        // Nome do template sem a extensão ".html"
        return "report";  // Este é o nome do template que o Thymeleaf irá buscar
    }

    public List<DataPoint> getMonthlyGrowth() throws IOException {
        // Dados de exemplo
        return List.of(new DataPoint("Janeiro", 1000000, 5.5));
    }

    public List<DataPoint> getAnnualGrowth() throws IOException {
        // Dados de exemplo
        return List.of(new DataPoint("2021", 12000000, 8.0));
    }
}
