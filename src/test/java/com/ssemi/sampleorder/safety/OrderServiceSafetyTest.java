package com.ssemi.sampleorder.safety;

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

@DisplayName("OrderService Safety Test")
class OrderServiceSafetyTest {

    private Path sampleFile;
    private Path orderFile;
    private SampleRepository sampleRepo;
    private OrderRepository orderRepo;
    private OrderService orderService;

    @BeforeEach
    void setUp() throws IOException {
        sampleFile = Files.createTempFile("samples_safe_", ".json");
        orderFile  = Files.createTempFile("orders_safe_",  ".json");
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

    // Cycle 7: 경계값 stock == quantity → CONFIRMED
    @Test
    @DisplayName("재고와 수량이 같을 때 승인하면 CONFIRMED로 전환된다")
    void 재고와_수량이_같을때_승인하면_CONFIRMED로_전환된다() throws IOException {
        sampleRepo.save(new Sample("S001", "AlphaChip", 30.0, 0.9, 50));
        orderRepo.save(new Order("O001", "S001", "홍길동", 50, OrderStatus.RESERVED));

        orderService.approve("O001");

        assertThat(orderRepo.findAll().get(0).getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    // Cycle 9: 음수 수량 주문 접수 → IllegalArgumentException
    @Test
    @DisplayName("음수 수량으로 주문 접수 시 예외가 발생한다")
    void 음수_수량으로_주문_접수_시_예외가_발생한다() throws IOException {
        sampleRepo.save(new Sample("S001", "AlphaChip", 30.0, 0.9, 100));

        assertThatThrownBy(() -> orderService.place("S001", "홍길동", -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // Cycle 8: 이미 처리된 주문 재승인 → IllegalStateException
    @Test
    @DisplayName("이미 처리된 주문 재승인 시 예외가 발생한다")
    void 이미_처리된_주문_재승인_시_예외가_발생한다() throws IOException {
        orderRepo.save(new Order("O001", "S001", "홍길동", 50, OrderStatus.CONFIRMED));

        assertThatThrownBy(() -> orderService.approve("O001"))
                .isInstanceOf(IllegalStateException.class);
    }
}
