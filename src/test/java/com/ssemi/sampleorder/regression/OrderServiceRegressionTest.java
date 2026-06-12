package com.ssemi.sampleorder.regression;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import com.ssemi.sampleorder.service.OrderService;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderService Regression Test")
class OrderServiceRegressionTest {

    private Path sampleFile;
    private Path orderFile;
    private SampleRepository sampleRepo;
    private OrderRepository orderRepo;
    private OrderService orderService;

    @BeforeEach
    void setUp() throws IOException {
        sampleFile = Files.createTempFile("samples_svc_", ".json");
        orderFile  = Files.createTempFile("orders_svc_",  ".json");
        Files.deleteIfExists(sampleFile);
        Files.deleteIfExists(orderFile);
        sampleRepo   = new SampleRepository(sampleFile.toString());
        orderRepo    = new OrderRepository(orderFile.toString());
        orderService = new OrderService(sampleRepo, orderRepo);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(sampleFile);
        Files.deleteIfExists(orderFile);
    }

    // Cycle 4: approve — 재고 충분 → CONFIRMED
    @Test
    @DisplayName("재고 충분 시 승인하면 CONFIRMED로 전환된다")
    void 재고_충분_시_승인하면_CONFIRMED로_전환된다() throws IOException {
        sampleRepo.save(new Sample("S001", "AlphaChip", 30.0, 0.9, 100));
        orderRepo.save(new Order("O001", "S001", "홍길동", 50, OrderStatus.RESERVED));

        orderService.approve("O001", List.of());

        assertThat(orderRepo.findAll().get(0).getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    // Cycle 5: approve — 재고 부족 → PRODUCING
    @Test
    @DisplayName("재고 부족 시 승인하면 PRODUCING으로 전환된다")
    void 재고_부족_시_승인하면_PRODUCING으로_전환된다() throws IOException {
        sampleRepo.save(new Sample("S001", "AlphaChip", 30.0, 0.9, 30));
        orderRepo.save(new Order("O001", "S001", "홍길동", 50, OrderStatus.RESERVED));

        orderService.approve("O001", List.of());

        assertThat(orderRepo.findAll().get(0).getStatus()).isEqualTo(OrderStatus.PRODUCING);
    }

    // Cycle 6: reject — RESERVED → REJECTED
    @Test
    @DisplayName("거절하면 REJECTED로 전환된다")
    void 거절하면_REJECTED로_전환된다() throws IOException {
        orderRepo.save(new Order("O001", "S001", "홍길동", 50, OrderStatus.RESERVED));

        orderService.reject("O001");

        assertThat(orderRepo.findAll().get(0).getStatus()).isEqualTo(OrderStatus.REJECTED);
    }
}
