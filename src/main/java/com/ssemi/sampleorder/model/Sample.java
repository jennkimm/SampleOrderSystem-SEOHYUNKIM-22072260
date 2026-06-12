package com.ssemi.sampleorder.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Sample {

    @JsonProperty("sampleId")
    private String sampleId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("avgProductionTime")
    private double avgProductionTime;

    @JsonProperty("yieldRate")
    private double yieldRate;

    @JsonProperty("stock")
    private int stock;

    public Sample() {}

    public Sample(String sampleId, String name, double avgProductionTime, double yieldRate, int stock) {
        this.sampleId = sampleId;
        this.name = name;
        this.avgProductionTime = avgProductionTime;
        this.yieldRate = yieldRate;
        this.stock = stock;
    }

    public String getSampleId() { return sampleId; }
    public void setSampleId(String sampleId) { this.sampleId = sampleId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getAvgProductionTime() { return avgProductionTime; }
    public void setAvgProductionTime(double avgProductionTime) { this.avgProductionTime = avgProductionTime; }

    public double getYieldRate() { return yieldRate; }
    public void setYieldRate(double yieldRate) { this.yieldRate = yieldRate; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
