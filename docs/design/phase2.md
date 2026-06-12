# Phase 2 — 주문 접수 + 승인/거절

> 상위 계획: [PLAN.md](../PLAN.md)  
> 도메인 요구사항: [AGENTS.md](../../AGENTS.md)

---

## 목표

고객 주문을 접수하고, 재고 상황에 따라 자동 분기(CONFIRMED / PRODUCING)하는 승인 로직을 완성한다.

---

## 구현 범위

- `Order` 엔티티 (도메인 모델 참조)
- `OrderStatus` enum: `RESERVED`, `CONFIRMED`, `PRODUCING`, `REJECTED`, `RELEASE`
- 주문 접수: 시료 ID·고객명·수량 입력 → `RESERVED` 저장
- RESERVED 목록 조회
- 주문 승인 분기:
  - `stock >= quantity` → 즉시 `CONFIRMED`
  - `stock < quantity` → `PRODUCING` (생산 라인 UI는 Phase 4, 상태만 전환)
- 주문 거절: `REJECTED`
- 예외 처리: 미등록 시료 ID로 주문 시도, 이미 처리된 주문 재승인 시도
- Regression Test: 분기 로직 정상 동작 검증
- Safety Test: 재고 경계값(정확히 일치), 음수 수량 입력

---

## 도메인 모델 — Order 엔티티

| 속성 | 타입 | 설명 |
|---|---|---|
| `orderId` | `String` | 주문 고유 ID |
| `sampleId` | `String` | 주문 대상 시료 ID |
| `customerName` | `String` | 고객명 |
| `quantity` | `int` | 주문 수량 |
| `status` | `OrderStatus` | 주문 상태 |

### OrderStatus enum

| 상태 | 설명 |
|---|---|
| `RESERVED` | 접수 대기 |
| `CONFIRMED` | 승인 (재고 충분) |
| `PRODUCING` | 생산 중 (재고 부족) |
| `REJECTED` | 거절 |
| `RELEASE` | 출고 완료 (Phase 3) |

---

## 테스트 포인트 (사용자 확인 항목)

- [ ] 주문 접수 후 RESERVED 목록에 표시되는가
- [ ] 재고가 충분한 시료를 승인하면 CONFIRMED로 전환되는가
- [ ] 재고가 부족한 시료를 승인하면 PRODUCING으로 전환되는가
- [ ] 거절 처리 후 해당 주문이 RESERVED 목록에서 사라지는가
- [ ] 존재하지 않는 시료 ID로 주문 시 안내 메시지가 출력되는가

---

## TDD 사이클 기록

### Cycle 1 — RED Plan

#### 테스트할 동작
`Order` 엔티티를 생성하면 전달한 속성값을 getter로 정확히 읽을 수 있다.

#### 이 테스트가 지금 필요한 이유
모든 주문 기능의 기반 데이터 구조. `Order`와 `OrderStatus`가 없으면 저장·승인 테스트를 작성할 수 없다.

#### 예상 테스트 구조
- 메서드: `주문_생성시_속성값을_정확히_반환한다()`
- Given: `Order o = new Order("O001", "S001", "홍길동", 10, OrderStatus.RESERVED)`
- Then:
  - `assertThat(o.getOrderId()).isEqualTo("O001")`
  - `assertThat(o.getSampleId()).isEqualTo("S001")`
  - `assertThat(o.getStatus()).isEqualTo(OrderStatus.RESERVED)`

#### 예상 실패 이유
`Order` 클래스 및 `OrderStatus` enum이 존재하지 않아 컴파일 오류 → 최소 스텁 후 올바르게 실패

---

### Cycle 2 — RED Plan

#### 테스트할 동작
`OrderRepository.save()`로 저장한 주문이 `findAll()`에서 조회된다.

#### 이 테스트가 지금 필요한 이유
Cycle 1에서 엔티티가 갖춰지면, 저장·조회 영속성 기반을 확인해야 한다. 이 위에 상태 조회와 서비스 로직이 쌓인다.

#### 예상 테스트 구조
- 메서드: `저장한_주문은_findAll에서_조회된다()`
- Given: 임시 파일 경로로 `OrderRepository` 생성, `Order` 1개
- When: `repo.save(order)`
- Then: `assertThat(repo.findAll()).hasSize(1)`

#### 예상 실패 이유
`OrderRepository` 클래스 미존재 → 컴파일 오류

---

### Cycle 3 — RED Plan

#### 테스트할 동작
`OrderRepository.findByStatus()`는 해당 상태의 주문만 반환한다.

#### 이 테스트가 지금 필요한 이유
RESERVED 목록 조회, 승인 후 상태 변경 확인 등 이후 모든 로직이 상태 필터링에 의존한다.

#### 예상 테스트 구조
- 메서드: `상태로_필터링하면_해당_상태의_주문만_반환한다()`
- Given: RESERVED 2개, CONFIRMED 1개 저장
- When: `repo.findByStatus(OrderStatus.RESERVED)`
- Then: `assertThat(result).hasSize(2)`

#### 예상 실패 이유
`findByStatus()` 미구현 → 컴파일 오류

---

### Cycle 4 — RED Plan

#### 테스트할 동작
`OrderService.approve()` — 재고가 충분(stock >= quantity)하면 주문 상태가 `CONFIRMED`로 전환된다.

#### 이 테스트가 지금 필요한 이유
승인 분기의 핵심 경로 1. Cycle 3까지 저장·조회가 확인되어야 서비스 로직을 테스트할 수 있다.

#### 예상 테스트 구조
- 메서드: `재고_충분_시_승인하면_CONFIRMED로_전환된다()`
- Given: stock=100인 Sample, quantity=50인 RESERVED Order 저장
- When: `service.approve(orderId)`
- Then: `assertThat(repo.findAll().get(0).getStatus()).isEqualTo(OrderStatus.CONFIRMED)`

#### 예상 실패 이유
`OrderService.approve()` 미구현 → 컴파일 오류

---

### Cycle 5 — RED Plan

#### 테스트할 동작
`OrderService.approve()` — 재고가 부족(stock < quantity)하면 주문 상태가 `PRODUCING`으로 전환된다.

#### 이 테스트가 지금 필요한 이유
승인 분기의 핵심 경로 2. Cycle 4와 대칭 케이스 — 두 케이스가 모두 GREEN이 되어야 분기 로직이 완성된다.

#### 예상 테스트 구조
- 메서드: `재고_부족_시_승인하면_PRODUCING으로_전환된다()`
- Given: stock=30인 Sample, quantity=50인 RESERVED Order 저장
- When: `service.approve(orderId)`
- Then: `assertThat(repo.findAll().get(0).getStatus()).isEqualTo(OrderStatus.PRODUCING)`

#### 예상 실패 이유
재고 비교 로직 미구현 → 상태가 CONFIRMED로 잘못 전환되거나 예외 발생

---

### Cycle 6 — RED Plan

#### 테스트할 동작
`OrderService.reject()` — RESERVED 주문이 `REJECTED`로 전환된다.

#### 이 테스트가 지금 필요한 이유
승인과 대칭되는 거절 경로. Cycle 4·5에서 approve가 완성되면 reject를 추가한다.

#### 예상 테스트 구조
- 메서드: `거절하면_REJECTED로_전환된다()`
- Given: RESERVED Order 저장
- When: `service.reject(orderId)`
- Then: `assertThat(repo.findAll().get(0).getStatus()).isEqualTo(OrderStatus.REJECTED)`

#### 예상 실패 이유
`OrderService.reject()` 미구현 → 컴파일 오류

---

### Cycle 7 (Safety) — RED Plan

#### 테스트할 동작
재고 경계값(stock == quantity)에서 승인하면 `CONFIRMED`로 전환된다.

#### 이 테스트가 지금 필요한 이유
`>=` 조건의 경계. off-by-one 오류를 조기에 차단한다.

#### 예상 테스트 구조
- 메서드: `재고와_수량이_같을때_승인하면_CONFIRMED로_전환된다()`
- Given: stock=50인 Sample, quantity=50인 RESERVED Order
- When: `service.approve(orderId)`
- Then: `isEqualTo(OrderStatus.CONFIRMED)`

#### 예상 실패 이유
경계값 처리 오류 시 PRODUCING으로 잘못 분기

---

### Cycle 8 (Safety) — RED Plan

#### 테스트할 동작
이미 처리된(CONFIRMED) 주문을 재승인 시도하면 예외가 발생한다.

#### 이 테스트가 지금 필요한 이유
중복 처리 방어. 실제 운영 환경에서 빈번히 발생하는 오류 케이스다.

#### 예상 테스트 구조
- 메서드: `이미_처리된_주문_재승인_시_예외가_발생한다()`
- Given: 이미 CONFIRMED 상태인 Order
- When: `service.approve(orderId)`
- Then: `assertThatThrownBy(...).isInstanceOf(IllegalStateException.class)`

#### 예상 실패 이유
예외 처리 로직 미구현 → 예외 발생 없이 잘못된 전환

---

### Cycle 9 (Safety) — RED Plan

#### 테스트할 동작
`OrderService.place()`에 음수 수량을 전달하면 `IllegalArgumentException`이 발생한다.

#### 이 테스트가 지금 필요한 이유
Cycle 1~8에서 정상 경로와 상태 방어는 완성됐다. 마지막 Safety 케이스인 음수 수량 입력 방어를 추가해야 Phase 2 서비스 레이어가 완결된다.

#### 예상 테스트 구조
- 메서드: `음수_수량으로_주문_접수_시_예외가_발생한다()`
- Given: 등록된 Sample, quantity = -1
- When: `service.place("S001", "홍길동", -1)`
- Then: `assertThatThrownBy(...).isInstanceOf(IllegalArgumentException.class)`

#### 예상 실패 이유
`place()`에 수량 검증 로직이 없어 예외 없이 RESERVED 저장 → 테스트 실패

---

## REVIEW 결과

### 테스트 실행 결과
- OrderRepository Regression: 3 PASSED
- OrderService Regression: 3 PASSED
- OrderService Safety: 3 PASSED (Cycle 7·8·9)
- Phase 1 회귀 (JsonFileRepository + SampleRepository): 16 PASSED
- **총 23 / 23 PASSED, BUILD SUCCESSFUL**

### 구현된 클래스

| 클래스 | 역할 |
|---|---|
| `model/Order` | 주문 엔티티 (Jackson 어노테이션 포함) |
| `model/OrderStatus` | 주문 상태 enum (RESERVED·CONFIRMED·PRODUCING·REJECTED·RELEASE) |
| `repository/OrderRepository` | 주문 CRUD (save, update, findAll, findById, findByStatus) |
| `service/OrderService` | 승인 분기(approve), 거절(reject), 접수(place, 음수 수량 방어 포함) |
| `repository/SampleRepository` | findById, update 메서드 추가 |
| `view/ConsoleView` | 주문 메뉴·목록·입력 프롬프트 메서드 추가, 메인 메뉴 case 2 "[준비 중]" 제거 |
| `controller/OrderController` | 주문 접수·RESERVED 조회·승인·거절 메뉴 루프 |
| `controller/MainController` | OrderController 주입 및 case 2 연결 |
| `Main` | OrderRepository·OrderService 의존성 조립 추가 |

### 정리 사항
- `approve()`: RESERVED 외 상태 → `IllegalStateException`
- `reject()`: RESERVED 외 상태 → `IllegalStateException`
- `place()`: quantity ≤ 0 → `IllegalArgumentException`, 미등록 sampleId → `IllegalArgumentException`
- 재고 경계값 `stock >= quantity` → CONFIRMED — Cycle 7 Safety로 검증 완료
- 데이터 파일 경로: `Main.java` 상수 `SAMPLE_DATA_FILE`, `ORDER_DATA_FILE`로 분리 관리
- UI 레이어(OrderController·ConsoleView 주문 메서드)는 Scanner 의존으로 단위 테스트 격리 불가 → 서비스 레이어에서 모든 비즈니스 로직 검증 완료 후 thin controller로 위임
