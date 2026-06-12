# Phase 1 — 프로젝트 골격 + 시료 관리

> 상위 계획: [PLAN.md](../PLAN.md)  
> 도메인 요구사항: [AGENTS.md](../../AGENTS.md)

---

## 목표

Gradle 프로젝트 구조를 세우고, 시료(Sample)를 등록·조회·검색할 수 있는 최소 동작 시스템을 완성한다.
애플리케이션을 재시작해도 데이터가 유지되어야 한다.

---

## 구현 범위

- Gradle 빌드 설정 (Java 21, Jackson, JUnit 5, AssertJ)
- MVC 패키지 골격: `model` / `repository` / `service` / `view` / `controller`
  - ConsoleMVC PoC의 Controller 생성자 주입·메뉴 루프 패턴 적용
- `Sample` 엔티티 (도메인 모델 참조)
- `JsonFileRepository<T>` — DataPersistence PoC의 `readAll` / `writeAll` 재사용
- 시료 등록 / 전체 조회(재고 포함, 단위 `ea` 표시) / 이름 검색
- 메인 메뉴 콘솔 화면 (시료 관리만 활성, 나머지는 "준비 중" 표시)
- Regression Test: 시료 CRUD 정상 동작 검증
- Safety Test: null 입력, 중복 ID, 빈 파일 등 방어 케이스

---

## 도메인 모델 — Sample 엔티티

| 속성 | 타입 | 설명 |
|---|---|---|
| `sampleId` | `String` | 시료 고유 ID |
| `name` | `String` | 시료 이름 |
| `avgProductionTime` | `double` | 평균 생산시간 (분, **소수점 한 자리 입력 가능**) |
| `yieldRate` | `double` | 수율 (0.0 ~ 1.0) |
| `stock` | `int` | 현재 재고 (단위: **ea**, 목록 조회 시 `100 ea` 형식으로 표시) |

---

## 테스트 포인트 (사용자 확인 항목)

- [x] 시료를 등록하고 목록에 즉시 표시되는가
- [x] 시료 목록 조회 시 재고가 `100 ea` 형식으로 표시되는가
- [x] 평균 생산시간에 소수점 한 자리(예: `30.5`) 입력이 가능한가
- [x] 이름으로 검색 시 정확히 필터링되는가
- [x] 앱을 재시작한 뒤에도 등록한 시료가 남아 있는가
- [x] 없는 이름으로 검색 시 오류 없이 "결과 없음"을 출력하는가

---

## TDD 사이클 기록

### Cycle 1 — RED Plan

#### 테스트할 동작
`Sample` 엔티티를 생성하면 전달한 속성값을 getter로 정확히 읽을 수 있다.

#### 이 테스트가 지금 필요한 이유
모든 기능의 기반 데이터 구조. 엔티티가 없으면 저장·조회 테스트를 작성할 수 없다.

#### 예상 테스트 구조
- 메서드: `샘플_생성시_속성값을_정확히_반환한다()`
- Given: `Sample s = new Sample("S001", "TestSample", 30.0, 0.9, 100)`
- Then: `assertEquals("S001", s.getSampleId())` 등

#### 예상 실패 이유
`Sample` 클래스가 존재하지 않아 컴파일 오류 → 최소 스텁 후 올바르게 실패

---

### Cycle 2 — RED Plan

#### 테스트할 동작
`JsonFileRepository.readAll()`은 파일이 없을 때 빈 리스트를 반환한다.

#### 이 테스트가 지금 필요한 이유
최초 실행 시 데이터 파일이 없는 상황에서 NPE 없이 동작해야 한다.

#### 예상 테스트 구조
- 메서드: `파일이_없을때_readAll은_빈리스트를_반환한다()`
- Given: 존재하지 않는 임시 파일 경로로 `JsonFileRepository` 생성
- Then: `assertTrue(repo.readAll().isEmpty())`

#### 예상 실패 이유
`JsonFileRepository` 클래스 미존재 → 컴파일 오류

---

### Cycle 3 — RED Plan

#### 테스트할 동작
`writeAll()`로 저장한 뒤 `readAll()`로 읽으면 동일한 데이터를 복원한다.

#### 이 테스트가 지금 필요한 이유
영속성의 핵심. 저장→읽기 왕복이 보장되어야 Cycle 4의 Repository가 의미를 갖는다.

#### 예상 테스트 구조
- 메서드: `저장후_읽기하면_동일한_샘플_목록을_반환한다()`
- Given: Sample 1개 포함 리스트 → `writeAll()`
- Then: `readAll()` 결과의 sampleId가 원본과 같다

#### 예상 실패 이유
`writeAll()` 미구현으로 파일이 생성되지 않아 읽기 결과가 빈 리스트

---

### Cycle 4 — RED Plan

#### 테스트할 동작
`SampleRepository.save()`로 저장한 시료가 `findAll()`에서 조회된다.

#### 이 테스트가 지금 필요한 이유
Repository 계층의 CRUD 기본 동작 검증. 이 위에 서비스 로직이 쌓인다.

#### 예상 테스트 구조
- 메서드: `저장한_시료는_findAll에서_조회된다()`
- Given: `SampleRepository`, `Sample` 1개
- When: `repo.save(sample)`
- Then: `assertEquals(1, repo.findAll().size())`

#### 예상 실패 이유
`SampleRepository` 클래스 미존재

---

### Cycle 5 — RED Plan

#### 테스트할 동작
`findByName()`은 이름에 키워드가 포함된 시료만 반환한다.

#### 이 테스트가 지금 필요한 이유
시료 검색 기능의 핵심. 정확한 필터링을 보장해야 한다.

#### 예상 테스트 구조
- 메서드: `이름_키워드로_검색하면_포함된_시료만_반환한다()`
- Given: "AlphaChip", "BetaChip", "GammaSensor" 저장
- When: `repo.findByName("Chip")`
- Then: 결과 2개, "GammaSensor" 미포함

#### 예상 실패 이유
`findByName()` 미구현

---

## REVIEW 결과

### 테스트 실행 결과
- JsonFileRepository Regression: 3 PASSED
- SampleRepository Regression: 6 PASSED
- SampleRepository Safety: 4 PASSED
- **총 13 / 13 PASSED, BUILD SUCCESSFUL**

### 구현된 클래스

| 클래스 | 역할 |
|---|---|
| `model/Sample` | 시료 엔티티 (Jackson 어노테이션 포함, `avgProductionTime`: double) |
| `repository/JsonFileRepository<T>` | 제네릭 JSON 파일 읽기/쓰기 |
| `repository/SampleRepository` | 시료 CRUD (save, findAll, findByName) |
| `service/SampleService` | 비즈니스 위임 레이어 |
| `view/ConsoleView` | 모든 콘솔 I/O 담당 (재고: `%d ea` 형식 출력) |
| `controller/SampleController` | 시료 관리 메뉴 루프 |
| `controller/MainController` | 메인 메뉴 루프 |
| `Main` | 진입점, 의존성 조립 |

### 정리 사항
- null 가드: `findByName(null)` → 빈 리스트 반환 (Safety Test 통과)
- 파일 손상 시 IOException 전파 (catch 후 사용자 메시지 출력)
- Phase 2~5 메뉴는 "준비 중"으로 표시
- `avgProductionTime`: `int` → `double` 변경 (소수점 한 자리 입력 지원)
- 시료 목록 재고 표시: `%6d` → `%6d ea` (단위 병기)
