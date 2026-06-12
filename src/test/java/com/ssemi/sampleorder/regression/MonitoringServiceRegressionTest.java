package com.ssemi.sampleorder.regression;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.model.StockStatus;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import com.ssemi.sampleorder.service.MonitoringService;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MonitoringService Regression Test")
class MonitoringServiceRegressionTest {

    private Path sampleFile;
    private Path orderFile;
    private SampleRepository sampleRepo;
    private OrderRepository orderRepo;
    private MonitoringService monitoringService;

    @BeforeEach
    void setUp() throws IOException {
        sampleFile = Files.createTempFile("samples_mon_", ".json");
        orderFile  = Files.createTempFile("orders_mon_",  ".json");
        Files.deleteIfExists(sampleFile);
        Files.deleteIfExists(orderFile);
        sampleRepo        = new SampleRepository(sampleFile.toString());
        orderRepo         = new OrderRepository(orderFile.toString());
        monitoringService = new MonitoringService(sampleRepo, orderRepo);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(sampleFile);
        Files.deleteIfExists(orderFile);
    }

    // Cycle 4: getOrderSummary — 상태별 집계, REJECTED 제외
    @Test
    @DisplayName("상태별 주문 집계 시 REJECTED는 제외된다")
    void 상태별_주문_집계_시_REJECTED는_제외된다() throws IOException {
        orderRepo.save(new Order("O001", "S001", "홍길동", 10, OrderStatus.RESERVED));
        orderRepo.save(new Order("O002", "S001", "김철수", 20, OrderStatus.RESERVED));
        orderRepo.save(new Order("O003", "S002", "이영희",  5, OrderStatus.CONFIRMED));
        orderRepo.save(new Order("O004", "S002", "박민준", 15, OrderStatus.REJECTED));
        orderRepo.save(new Order("O005", "S003", "최수진",  8, OrderStatus.PRODUCING));

        Map<OrderStatus, List<Order>> summary = monitoringService.getOrderSummary();

        assertThat(summary).doesNotContainKey(OrderStatus.REJECTED);
        assertThat(summary.get(OrderStatus.RESERVED)).hasSize(2);
        assertThat(summary.get(OrderStatus.CONFIRMED)).hasSize(1);
        assertThat(summary.get(OrderStatus.PRODUCING)).hasSize(1);
    }

    // Cycle 5: judgeStockStatus — stock=0 → DEPLETED
    @Test
    @DisplayName("재고 0이면 DEPLETED로 판정된다")
    void 재고_0이면_DEPLETED로_판정된다() {
        Sample sample = new Sample("S001", "AlphaChip", 30.0, 0.9, 0);

        StockStatus result = monitoringService.judgeStockStatus(sample, 0);

        assertThat(result).isEqualTo(StockStatus.DEPLETED);
    }

    // Cycle 6: judgeStockStatus — stock < confirmedQty → INSUFFICIENT
    @Test
    @DisplayName("재고가 CONFIRMED 합계보다 적으면 INSUFFICIENT로 판정된다")
    void 재고가_CONFIRMED_합계보다_적으면_INSUFFICIENT로_판정된다() {
        Sample sample = new Sample("S001", "AlphaChip", 30.0, 0.9, 30);

        StockStatus result = monitoringService.judgeStockStatus(sample, 50);

        assertThat(result).isEqualTo(StockStatus.INSUFFICIENT);
    }

    @Test
    @DisplayName("재고가 CONFIRMED 합계 이상이면 SUFFICIENT로 판정된다")
    void 재고가_CONFIRMED_합계_이상이면_SUFFICIENT로_판정된다() {
        Sample sample = new Sample("S001", "AlphaChip", 30.0, 0.9, 100);

        StockStatus result = monitoringService.judgeStockStatus(sample, 50);

        assertThat(result).isEqualTo(StockStatus.SUFFICIENT);
    }
}
