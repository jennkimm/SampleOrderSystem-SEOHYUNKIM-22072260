package com.ssemi.sampleorder.safety;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.ProductionLineRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import com.ssemi.sampleorder.service.ProductionService;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductionService Safety Test")
class ProductionServiceSafetyTest {

    private Path sampleFile;
    private Path orderFile;
    private Path productionFile;
    private SampleRepository sampleRepo;
    private OrderRepository orderRepo;
    private ProductionLineRepository productionRepo;
    private ProductionService productionService;

    @BeforeEach
    void setUp() throws IOException {
        sampleFile     = Files.createTempFile("samples_safety_",    ".json");
        orderFile      = Files.createTempFile("orders_safety_",     ".json");
        productionFile = Files.createTempFile("production_safety_", ".json");
        Files.deleteIfExists(sampleFile);
        Files.deleteIfExists(orderFile);
        Files.deleteIfExists(productionFile);
        sampleRepo     = new SampleRepository(sampleFile.toString());
        orderRepo      = new OrderRepository(orderFile.toString());
        productionRepo = new ProductionLineRepository(productionFile.toString());
        productionService = new ProductionService(sampleRepo, orderRepo, productionRepo);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(sampleFile);
        Files.deleteIfExists(orderFile);
        Files.deleteIfExists(productionFile);
    }

    @Test
    @DisplayName("빈 큐에서 completeNext 호출 시 IllegalStateException 이 발생한다")
    void completeNextThrowsWhenQueueIsEmpty() {
        assertThatThrownBy(() -> productionService.completeNext())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("비어");
    }

    @Test
    @DisplayName("yieldRate 가 0 인 시료로 enqueue 시 IllegalArgumentException 이 발생한다")
    void enqueueThrowsWhenYieldRateIsZero() throws IOException {
        Sample sample = new Sample("S-001", "불량 시료", 1.0, 0.0, 50);
        sampleRepo.save(sample);
        orderRepo.save(new Order("O-001", "S-001", "고객A", 100, OrderStatus.PRODUCING));

        assertThatThrownBy(() -> productionService.enqueue("O-001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("yieldRate");
    }
}
