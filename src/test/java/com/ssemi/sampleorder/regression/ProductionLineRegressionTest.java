package com.ssemi.sampleorder.regression;

import com.ssemi.sampleorder.model.ProductionLine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductionLine Regression Test")
class ProductionLineRegressionTest {

    @Test
    @DisplayName("생산량·시간은 공식 scheduledQty=ceil(부족분/(수율×0.9)) 대로 계산된다")
    void scheduledQtyAndEstimatedTimeAreCalculatedByFormula() {
        // Given: shortfall=10, yieldRate=0.75, avgProductionTime=2.5
        // scheduledQty = ceil(10 / (0.75 * 0.9)) = ceil(14.814...) = 15
        // estimatedTime = 2.5 * 15 = 37.5
        ProductionLine pl = new ProductionLine("O-001", 10, 0.75, 2.5);

        assertThat(pl.getScheduledQty()).isEqualTo(15);
        assertThat(pl.getEstimatedTime()).isEqualTo(37.5);
    }
}
