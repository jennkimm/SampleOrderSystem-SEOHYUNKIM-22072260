package com.ssemi.sampleorder.service;

import com.ssemi.sampleorder.model.ProductionLine;

public class ProductionLineDetail {

    private final ProductionLine productionLine;
    private final String sampleName;
    private final int orderQuantity;
    private final int sampleStock;

    public ProductionLineDetail(ProductionLine productionLine,
                                String sampleName,
                                int orderQuantity,
                                int sampleStock) {
        this.productionLine = productionLine;
        this.sampleName     = sampleName;
        this.orderQuantity  = orderQuantity;
        this.sampleStock    = sampleStock;
    }

    public ProductionLine getProductionLine() { return productionLine; }
    public String getSampleName()             { return sampleName; }
    public int getOrderQuantity()             { return orderQuantity; }
    public int getSampleStock()               { return sampleStock; }
}
