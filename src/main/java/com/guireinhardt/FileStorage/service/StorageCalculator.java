package com.guireinhardt.FileStorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.guireinhardt.FileStorage.vo.DataPoint;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@Service
public class StorageCalculator {
    @Value("${file.upload-dir}")
    private String fileStorageLocation;



    @Autowired
    private FileStorageService fileStorageService;  // Injeção do FileStorageService

    // Método para calcular o tamanho total dos arquivos
    public long calculateTotalDataSize() throws IOException {
        Path rootDirectory = fileStorageService.getFileStorageLocation();  // Usando o FileStorageService para obter o diretório raiz
        return calculateDirectorySize(rootDirectory);
    }

    // Método para calcular o crescimento mensal
    public List<DataPoint> calculateMonthlyGrowth(List<DataPoint> previousData, List<DataPoint> currentData) {
        return calculateGrowth(previousData, currentData);
    }

    // Método para calcular o crescimento anual
    public List<DataPoint> calculateAnnualGrowth(List<DataPoint> previousData, List<DataPoint> currentData) {
        return calculateGrowth(previousData, currentData);
    }

    // Método auxiliar para cálculo de crescimento
    private List<DataPoint> calculateGrowth(List<DataPoint> previousData, List<DataPoint> currentData) {
        List<DataPoint> growthData = new ArrayList<>();

        for (int i = 0; i < previousData.size(); i++) {
            DataPoint prevPoint = previousData.get(i);
            DataPoint currPoint = currentData.get(i);

            // Calcular o crescimento entre os dois períodos
            double growth = 0;
            if (prevPoint.getDataSize() != 0) {
                growth = ((currPoint.getDataSize() - prevPoint.getDataSize()) / prevPoint.getDataSize()) * 100;
            }

            // Criar um novo ponto de dados com o crescimento calculado
            DataPoint growthPoint = new DataPoint(currPoint.getPeriod(), currPoint.getDataSize(), growth);
            growthData.add(growthPoint);
        }

        return growthData;
    }

    // Método auxiliar para cálculo de tamanho de diretório
    private long calculateDirectorySize(Path dir) throws IOException {
        final long[] size = {0};

        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                size[0] += Files.size(file);  // Adiciona o tamanho do arquivo
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });

        return size[0];
    }
}
