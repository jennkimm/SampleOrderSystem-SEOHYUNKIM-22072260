package com.ssemi.sampleorder.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductionLine {

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("shortfall")
    private int shortfall;

    @JsonProperty("scheduledQty")
    private int scheduledQty;

    @JsonProperty("estimatedTime")
    private double estimatedTime;

    @JsonProperty("producedQty")
    private int producedQty;

    public ProductionLine() {}

    public ProductionLine(String orderId, int shortfall, double yieldRate, double avgProductionTime) {
        if (yieldRate <= 0) {
            throw new IllegalArgumentException("yieldRate는 0보다 커야 합니다: " + yieldRate);
        }
        this.orderId       = orderId;
        this.shortfall     = shortfall;
        this.scheduledQty  = (int) Math.ceil(shortfall / (yieldRate * 0.9));
        this.estimatedTime = avgProductionTime * this.scheduledQty;
        this.producedQty   = 0;
    }

    public String getOrderId()       { return orderId; }
    public int    getShortfall()     { return shortfall; }
    public int    getScheduledQty()  { return scheduledQty; }
    public double getEstimatedTime() { return estimatedTime; }
    public int    getProducedQty()   { return producedQty; }

    public void setOrderId(String orderId)             { this.orderId = orderId; }
    public void setShortfall(int shortfall)            { this.shortfall = shortfall; }
    public void setScheduledQty(int scheduledQty)      { this.scheduledQty = scheduledQty; }
    public void setEstimatedTime(double estimatedTime) { this.estimatedTime = estimatedTime; }
    public void setProducedQty(int producedQty)        { this.producedQty = producedQty; }
}
