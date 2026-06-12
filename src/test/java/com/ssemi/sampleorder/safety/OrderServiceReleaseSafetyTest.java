package com.ssemi.sampleorder.safety;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import com.ssemi.sampleorder.service.OrderService;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderService Release Safety Test")
class OrderServiceReleaseSafetyTest {

    private Path sampleFile;
    private Path orderFile;
    private SampleRepository sampleRepo;
    private OrderRepository orderRepo;
    private OrderService orderService;

    @BeforeEach
    void setUp() throws IOException {
        sampleFile = Files.createTempFile("samples_relsafe_", ".json");
        orderFile  = Files.createTempFile("orders_relsafe_",  ".json");
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

    // Cycle 2: CONFIRMED 아닌 주문 출고 → IllegalStateException
    @Test
    @DisplayName("CONFIRMED 아닌 주문 출고 시 예외가 발생한다")
    void CONFIRMED_아닌_주문_출고_시_예외가_발생한다() throws IOException {
        orderRepo.save(new Order("O001", "S001", "홍길동", 30, OrderStatus.RESERVED));

        assertThatThrownBy(() -> orderService.release("O001"))
                .isInstanceOf(IllegalStateException.class);
    }

    // Cycle 3: 이미 출고된 주문 재출고 → IllegalStateException
    @Test
    @DisplayName("이미 출고된 주문 재출고 시 예외가 발생한다")
    void 이미_출고된_주문_재출고_시_예외가_발생한다() throws IOException {
        orderRepo.save(new Order("O001", "S001", "홍길동", 30, OrderStatus.RELEASE));

        assertThatThrownBy(() -> orderService.release("O001"))
                .isInstanceOf(IllegalStateException.class);
    }
}
