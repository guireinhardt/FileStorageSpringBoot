package com.guireinhardt.FileStorage.dto;

import java.time.LocalDate;

public class SearchDayCountDTO {
    private LocalDate day;
    private long total;

    public SearchDayCountDTO(LocalDate day, long total) {
        this.day = day;
        this.total = total;
    }

    public LocalDate getDay() {
        return day;
    }

    public long getTotal() {
        return total;
    }
}
