package com.ssemi.sampleorder.regression;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.repository.OrderRepository;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderRepository Regression Test")
class OrderRepositoryRegressionTest {

    private Path tempFile;
    private OrderRepository repo;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("orders_test_", ".json");
        Files.deleteIfExists(tempFile);
        repo = new OrderRepository(tempFile.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    // Cycle 1: Order 엔티티
    @Test
    @DisplayName("주문 생성 시 속성값을 정확히 반환한다")
    void 주문_생성시_속성값을_정확히_반환한다() {
        Order o = new Order("O001", "S001", "홍길동", 10, OrderStatus.RESERVED);

        assertThat(o.getOrderId()).isEqualTo("O001");
        assertThat(o.getSampleId()).isEqualTo("S001");
        assertThat(o.getCustomerName()).isEqualTo("홍길동");
        assertThat(o.getQuantity()).isEqualTo(10);
        assertThat(o.getStatus()).isEqualTo(OrderStatus.RESERVED);
    }

    // Cycle 2: save + findAll
    @Test
    @DisplayName("저장한 주문은 findAll에서 조회된다")
    void 저장한_주문은_findAll에서_조회된다() throws IOException {
        Order o = new Order("O001", "S001", "홍길동", 10, OrderStatus.RESERVED);

        repo.save(o);

        assertThat(repo.findAll()).hasSize(1);
        assertThat(repo.findAll().get(0).getOrderId()).isEqualTo("O001");
    }

    @Test
    @DisplayName("저장한 주문은 앱 재시작(Repository 재생성) 후에도 유지된다")
    void 저장한_주문은_재생성_후에도_유지된다() throws IOException {
        repo.save(new Order("O001", "S001", "홍길동", 10, OrderStatus.RESERVED));

        OrderRepository reloaded = new OrderRepository(tempFile.toString());

        assertThat(reloaded.findAll()).hasSize(1);
        assertThat(reloaded.findAll().get(0).getOrderId()).isEqualTo("O001");
    }

    // Cycle 3: findByStatus
    @Test
    @DisplayName("상태로 필터링하면 해당 상태의 주문만 반환한다")
    void 상태로_필터링하면_해당_상태의_주문만_반환한다() throws IOException {
        repo.save(new Order("O001", "S001", "홍길동", 10, OrderStatus.RESERVED));
        repo.save(new Order("O002", "S001", "김철수", 20, OrderStatus.RESERVED));
        repo.save(new Order("O003", "S002", "이영희", 5, OrderStatus.CONFIRMED));

        List<Order> result = repo.findByStatus(OrderStatus.RESERVED);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Order::getOrderId)
                .containsExactlyInAnyOrder("O001", "O002");
    }
}
