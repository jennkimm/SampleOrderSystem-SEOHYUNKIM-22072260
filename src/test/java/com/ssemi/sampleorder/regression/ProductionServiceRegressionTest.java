package com.ssemi.sampleorder.regression;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.ProductionLine;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.ProductionLineRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import com.ssemi.sampleorder.service.ProductionService;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductionService Regression Test")
class ProductionServiceRegressionTest {

    private Path sampleFile;
    private Path orderFile;
    private Path productionFile;
    private SampleRepository sampleRepo;
    private OrderRepository orderRepo;
    private ProductionLineRepository productionRepo;
    private ProductionService productionService;

    @BeforeEach
    void setUp() throws IOException {
        sampleFile     = Files.createTempFile("samples_prod_",    ".json");
        orderFile      = Files.createTempFile("orders_prod_",     ".json");
        productionFile = Files.createTempFile("production_svc_",  ".json");
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
    @DisplayName("enqueue 는 부족분·공식으로 scheduledQty 를 계산해 ProductionLine 을 저장한다")
    void enqueueCreatesProductionLineWithCorrectScheduledQty() throws IOException {
        // Given: stock=90, order.quantity=100 → shortfall=10, yieldRate=0.75
        // scheduledQty = ceil(10 / (0.75 * 0.9)) = 15
        Sample sample = new Sample("S-001", "테스트 시료", 2.5, 0.75, 90);
        sampleRepo.save(sample);
        orderRepo.save(new Order("O-001", "S-001", "고객A", 100, OrderStatus.PRODUCING));

        productionService.enqueue("O-001");

        List<ProductionLine> queue = productionRepo.findAll();
        assertThat(queue).hasSize(1);
        assertThat(queue.get(0).getOrderId()).isEqualTo("O-001");
        assertThat(queue.get(0).getScheduledQty()).isEqualTo(15);
    }

    @Test
    @DisplayName("completeNext 는 큐 head 를 dequeue 하고 주문을 CONFIRMED 로 전환하며 재고를 scheduledQty 만큼 증가시킨다")
    void completeNextTransitionsOrderToConfirmedAndIncrementsStock() throws IOException {
        // Given: stock=90, order.quantity=100 → shortfall=10, scheduledQty=15
        Sample sample = new Sample("S-001", "테스트 시료", 2.5, 0.75, 90);
        sampleRepo.save(sample);
        orderRepo.save(new Order("O-001", "S-001", "고객A", 100, OrderStatus.PRODUCING));
        productionService.enqueue("O-001");

        productionService.completeNext();

        assertThat(orderRepo.findById("O-001").getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(sampleRepo.findById("S-001").getStock()).isEqualTo(90 + 15); // 105
        assertThat(productionRepo.findAll()).isEmpty();
    }
}
