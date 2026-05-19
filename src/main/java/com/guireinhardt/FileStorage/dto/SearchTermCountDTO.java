package com.guireinhardt.FileStorage.dto;

public class SearchTermCountDTO {
    private String term;
    private long total;

    public SearchTermCountDTO(String term, long total) {
        this.term = term;
        this.total = total;
    }

    public String getTerm() {
        return term;
    }

    public long getTotal() {
        return total;
    }
}
