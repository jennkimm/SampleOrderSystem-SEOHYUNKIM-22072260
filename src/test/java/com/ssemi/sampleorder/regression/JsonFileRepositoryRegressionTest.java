package com.ssemi.sampleorder.regression;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.repository.JsonFileRepository;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JsonFileRepository Regression Test")
class JsonFileRepositoryRegressionTest {

    private Path tempFile;
    private JsonFileRepository<Sample> repo;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("json_test_", ".json");
        Files.deleteIfExists(tempFile);
        repo = new JsonFileRepository<>(tempFile.toString(),
                new TypeReference<List<Sample>>() {});
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    // Cycle 2: 파일 없을 때 빈 리스트 반환
    @Test
    @DisplayName("파일이 없을 때 readAll은 빈 리스트를 반환한다")
    void 파일이_없을때_readAll은_빈리스트를_반환한다() throws IOException {
        assertThat(repo.readAll()).isEmpty();
    }

    // Cycle 3: 저장/읽기 왕복
    @Test
    @DisplayName("저장 후 읽기하면 동일한 샘플 목록을 반환한다")
    void 저장후_읽기하면_동일한_샘플_목록을_반환한다() throws IOException {
        List<Sample> original = List.of(new Sample("S001", "AlphaChip", 30, 0.9, 100));

        repo.writeAll(original);
        List<Sample> result = repo.readAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSampleId()).isEqualTo("S001");
        assertThat(result.get(0).getName()).isEqualTo("AlphaChip");
    }

    @Test
    @DisplayName("빈 리스트를 저장하면 readAll도 빈 리스트를 반환한다")
    void 빈리스트_저장하면_readAll도_빈리스트_반환한다() throws IOException {
        repo.writeAll(List.of());
        assertThat(repo.readAll()).isEmpty();
    }
}
