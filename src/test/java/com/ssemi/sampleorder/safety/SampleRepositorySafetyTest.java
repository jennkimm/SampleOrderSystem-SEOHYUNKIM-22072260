package com.ssemi.sampleorder.safety;

import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.repository.SampleRepository;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SampleRepository Safety Test")
class SampleRepositorySafetyTest {

    private Path tempFile;
    private SampleRepository repo;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("samples_safety_", ".json");
        Files.deleteIfExists(tempFile);
        repo = new SampleRepository(tempFile.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("null 이름으로 검색하면 빈 리스트를 반환한다 (NPE 없음)")
    void null_이름_검색시_빈리스트_반환한다() throws IOException {
        repo.save(new Sample("S001", "AlphaChip", 30, 0.9, 100));

        assertThatCode(() -> {
            var result = repo.findByName(null);
            assertThat(result).isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("빈 문자열로 검색하면 전체 시료를 반환한다")
    void 빈문자열_검색시_전체_반환한다() throws IOException {
        repo.save(new Sample("S001", "AlphaChip", 30, 0.9, 100));
        repo.save(new Sample("S002", "BetaChip", 45, 0.85, 50));

        assertThat(repo.findByName("")).hasSize(2);
    }

    @Test
    @DisplayName("데이터 파일이 손상된 경우 IOException을 발생시킨다")
    void 데이터_파일_손상시_IOException_발생한다() throws IOException {
        Files.writeString(tempFile, "{ invalid json !!!");

        SampleRepository corruptedRepo = new SampleRepository(tempFile.toString());
        assertThatThrownBy(corruptedRepo::findAll)
                .isInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("재고가 0인 시료도 정상 저장·조회된다")
    void 재고가_0인_시료도_정상_저장_조회된다() throws IOException {
        repo.save(new Sample("S001", "AlphaChip", 30, 0.9, 0));

        assertThat(repo.findAll().get(0).getStock()).isEqualTo(0);
    }
}
