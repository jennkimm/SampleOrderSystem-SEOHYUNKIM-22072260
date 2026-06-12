package com.ssemi.sampleorder.regression;

import com.ssemi.sampleorder.model.ProductionLine;
import com.ssemi.sampleorder.repository.ProductionLineRepository;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductionLineRepository Regression Test")
class ProductionLineRepositoryRegressionTest {

    private Path file;
    private ProductionLineRepository repo;

    @BeforeEach
    void setUp() throws IOException {
        file = Files.createTempFile("production_", ".json");
        Files.deleteIfExists(file);
        repo = new ProductionLineRepository(file.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(file);
    }

    @Test
    @DisplayName("enqueue 순서대로 findAll 에서 FIFO 순서로 반환된다")
    void enqueueOrderIsPreservedInFindAll() throws IOException {
        ProductionLine a = new ProductionLine("O-001", 10, 0.75, 2.5);
        ProductionLine b = new ProductionLine("O-002", 20, 0.80, 1.5);

        repo.enqueue(a);
        repo.enqueue(b);

        List<ProductionLine> all = repo.findAll();
        assertThat(all).hasSize(2);
        assertThat(all.get(0).getOrderId()).isEqualTo("O-001");
        assertThat(all.get(1).getOrderId()).isEqualTo("O-002");
    }

    @Test
    @DisplayName("dequeueHead 는 첫 번째 항목을 제거하고 나머지를 유지한다")
    void dequeueHeadRemovesFirstEntry() throws IOException {
        repo.enqueue(new ProductionLine("O-001", 10, 0.75, 2.5));
        repo.enqueue(new ProductionLine("O-002", 20, 0.80, 1.5));

        ProductionLine head = repo.dequeueHead();

        assertThat(head.getOrderId()).isEqualTo("O-001");
        assertThat(repo.findAll()).hasSize(1);
        assertThat(repo.findAll().get(0).getOrderId()).isEqualTo("O-002");
    }
}
