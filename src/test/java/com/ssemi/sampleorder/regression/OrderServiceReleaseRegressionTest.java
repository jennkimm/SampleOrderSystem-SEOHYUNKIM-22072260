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

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderService Release Regression Test")
class OrderServiceReleaseRegressionTest {

    private Path sampleFile;
    private Path orderFile;
    private SampleRepository sampleRepo;
    private OrderRepository orderRepo;
    private OrderService orderService;

    @BeforeEach
    void setUp() throws IOException {
        sampleFile = Files.createTempFile("samples_rel_", ".json");
        orderFile  = Files.createTempFile("orders_rel_",  ".json");
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

    // Cycle 1: release — RELEASE 전환 + 재고 차감
    @Test
    @DisplayName("출고 처리 시 RELEASE 전환 및 재고가 차감된다")
    void 출고_처리_시_RELEASE_전환_및_재고가_차감된다() throws IOException {
        sampleRepo.save(new Sample("S001", "AlphaChip", 30.0, 0.9, 100));
        orderRepo.save(new Order("O001", "S001", "홍길동", 30, OrderStatus.CONFIRMED));

        orderService.release("O001");

        assertThat(orderRepo.findById("O001").getStatus()).isEqualTo(OrderStatus.RELEASE);
        assertThat(sampleRepo.findById("S001").getStock()).isEqualTo(70);
    }
}
