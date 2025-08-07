package com.secretaria.FileStorage.vo;

public class DataPoint {
    private final String period;  // Exemplo: "Janeiro", "2022"
    private final long dataSize;  // Tamanho dos dados em bytes
    private final double growth;  // Crescimento em percentual

    // Construtor
    public DataPoint(String period, long dataSize, double growth) {
        this.period = period;
        this.dataSize = dataSize;
        this.growth = growth;
    }

    // Getters
    public String getPeriod() {
        return period;
    }

    public long getDataSize() {
        return dataSize;
    }

    public double getGrowth() {
        return growth;
    }
}
