package com.guireinhardt.FileStorage.dto;

import java.util.List;

public class ChartDTO {
    private List<String> labels;  // Lista de datas
    private List<Long> counts;    // Lista de contagens de acessos

    // Construtor

    public ChartDTO(List<String> labels, List<Long> counts) {
        this.labels = labels;
        this.counts = counts;
    }

    // Getters
    public List<String> getLabels() {
        return labels;
    }

    public List<Long> getCounts() {
        return counts;
    }

    // Setters
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public void setCounts(List<Long> counts) {
        this.counts = counts;
    }
}
