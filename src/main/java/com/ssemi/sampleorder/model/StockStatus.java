package com.ssemi.sampleorder.model;

public enum StockStatus {
    SUFFICIENT("여유"), INSUFFICIENT("부족"), DEPLETED("고갈");

    private final String label;

    StockStatus(String label) { this.label = label; }

    public String getLabel() { return label; }
}
