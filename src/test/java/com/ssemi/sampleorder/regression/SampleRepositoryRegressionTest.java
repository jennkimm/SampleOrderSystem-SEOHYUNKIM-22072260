package com.ssemi.sampleorder.regression;

import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.repository.SampleRepository;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SampleRepository Regression Test")
class SampleRepositoryRegressionTest {

    private Path tempFile;
    private SampleRepository repo;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("samples_test_", ".json");
        Files.deleteIfExists(tempFile);
        repo = new SampleRepository(tempFile.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    // Cycle 1: Sample 엔티티
    @Test
    @DisplayName("샘플 생성 시 속성값을 정확히 반환한다")
    void 샘플_생성시_속성값을_정확히_반환한다() {
        Sample s = new Sample("S001", "AlphaChip", 30, 0.9, 100);

        assertThat(s.getSampleId()).isEqualTo("S001");
        assertThat(s.getName()).isEqualTo("AlphaChip");
        assertThat(s.getAvgProductionTime()).isEqualTo(30.0);
        assertThat(s.getYieldRate()).isEqualTo(0.9);
        assertThat(s.getStock()).isEqualTo(100);
    }

    // Cycle 4: save + findAll
    @Test
    @DisplayName("저장한 시료는 findAll에서 조회된다")
    void 저장한_시료는_findAll에서_조회된다() throws IOException {
        Sample s = new Sample("S001", "AlphaChip", 30, 0.9, 100);

        repo.save(s);
        List<Sample> result = repo.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSampleId()).isEqualTo("S001");
    }

    @Test
    @DisplayName("여러 시료를 저장하면 모두 조회된다")
    void 여러_시료를_저장하면_모두_조회된다() throws IOException {
        repo.save(new Sample("S001", "AlphaChip", 30, 0.9, 100));
        repo.save(new Sample("S002", "BetaChip", 45, 0.85, 50));

        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("저장한 시료는 앱 재시작(Repository 재생성) 후에도 유지된다")
    void 저장한_시료는_재생성_후에도_유지된다() throws IOException {
        repo.save(new Sample("S001", "AlphaChip", 30, 0.9, 100));

        SampleRepository reloaded = new SampleRepository(tempFile.toString());
        assertThat(reloaded.findAll()).hasSize(1);
        assertThat(reloaded.findAll().get(0).getSampleId()).isEqualTo("S001");
    }

    // Cycle 5: findByName
    @Test
    @DisplayName("이름 키워드로 검색하면 포함된 시료만 반환한다")
    void 이름_키워드로_검색하면_포함된_시료만_반환한다() throws IOException {
        repo.save(new Sample("S001", "AlphaChip", 30, 0.9, 100));
        repo.save(new Sample("S002", "BetaChip", 45, 0.85, 50));
        repo.save(new Sample("S003", "GammaSensor", 60, 0.8, 30));

        List<Sample> result = repo.findByName("Chip");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Sample::getName)
                .containsExactlyInAnyOrder("AlphaChip", "BetaChip");
    }

    @Test
    @DisplayName("검색 키워드와 일치하는 시료가 없으면 빈 리스트를 반환한다")
    void 검색_키워드_일치_없으면_빈리스트_반환한다() throws IOException {
        repo.save(new Sample("S001", "AlphaChip", 30, 0.9, 100));

        List<Sample> result = repo.findByName("없는이름");

        assertThat(result).isEmpty();
    }
}
