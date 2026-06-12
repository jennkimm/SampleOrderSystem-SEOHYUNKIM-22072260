# Phase 3 — 모니터링 + 출고 처리

> 상위 계획: [PLAN.md](../PLAN.md)  
> 도메인 요구사항: [AGENTS.md](../../AGENTS.md)

---

## 목표

시스템 전체 상태를 한눈에 파악할 수 있는 모니터링 화면을 제공하고,
CONFIRMED 주문을 출고 처리하여 재고를 차감하는 흐름을 완성한다.

---

## 구현 범위

- **출고 처리**
  - `OrderService.release()`: CONFIRMED → RELEASE 전환 + `sample.stock -= order.quantity`
- **모니터링**
  - 상태별(RESERVED / CONFIRMED / PRODUCING / RELEASE) 주문 목록·수량 집계 (`REJECTED` 필터 제외)
  - 시료별 재고 상태 판정: `StockStatus` enum (여유 / 부족 / 고갈)
  - `MonitoringService`로 집계 로직 캡슐화
- Regression Test: 출고 후 재고 차감, 모니터링 집계 정확성, 재고 상태 판정
- Safety Test: 이미 출고된 주문 재출고, CONFIRMED 아닌 주문 출고 시도

---

## 도메인 모델 추가

| 클래스 | 역할 |
|---|---|
| `model/StockStatus` | 재고 상태 enum: `SUFFICIENT`(여유) / `INSUFFICIENT`(부족) / `DEPLETED`(고갈) |
| `service/MonitoringService` | 주문 상태별 집계, 시료 재고 상태 판정 |

**재고 상태 판정 기준**

| 상태 | 조건 |
|---|---|
| DEPLETED (고갈) | `stock == 0` |
| INSUFFICIENT (부족) | `0 < stock < (CONFIRMED + PRODUCING) 주문 합계 수량` |
| SUFFICIENT (여유) | `stock >= (CONFIRMED + PRODUCING) 주문 합계 수량` (해당 주문 없는 경우 포함) |

> **판정 기준 설명**: CONFIRMED(출고 대기)와 PRODUCING(생산 중) 주문 모두 향후 재고에서 차감될 수량이므로 합산하여 재고 건전성을 판단한다. `MonitoringService.getTotalPendingQty(sampleId)`가 두 상태의 수량 합계를 반환하고, `judgeStockStatus(sample, pendingQty)`에 전달된다.

---

## 테스트 포인트 (사용자 확인 항목)

- [x] 출고 처리 후 해당 시료의 재고가 즉시 차감되는가
- [x] 출고 후 주문 상태가 RELEASE로 변경되는가
- [x] 모니터링 화면에서 상태별 주문 수가 정확히 집계되는가
- [x] REJECTED 주문이 모니터링에서 제외되는가
- [x] 재고 0인 시료가 "고갈"로 표시되는가
- [x] CONFIRMED 합계보다 재고 부족 시 "부족"으로 표시되는가

---

## TDD 사이클 기록

### Cycle 1 — RED Plan

#### 테스트할 동작
`OrderService.release()` — CONFIRMED 주문을 출고하면 주문 상태가 RELEASE로 전환되고 시료 재고가 주문 수량만큼 차감된다.

#### 이 테스트가 지금 필요한 이유
Phase 3의 핵심 기능. 재고 차감과 상태 전환을 동시에 검증해야 출고 흐름이 완결된다.

#### 예상 테스트 구조
- 메서드: `출고_처리_시_RELEASE_전환_및_재고가_차감된다()`
- Given: stock=100인 Sample, quantity=30인 CONFIRMED Order
- When: `orderService.release("O001")`
- Then:
  - `orderRepo.findAll().get(0).getStatus() == RELEASE`
  - `sampleRepo.findById("S001").getStock() == 70`

#### 예상 실패 이유
`OrderService.release()` 미구현 → 컴파일 오류 → UnsupportedOperationException

---

### Cycle 2 (Safety) — RED Plan

#### 테스트할 동작
CONFIRMED가 아닌 주문(RESERVED)에 출고를 시도하면 `IllegalStateException`이 발생한다.

#### 이 테스트가 지금 필요한 이유
출고는 CONFIRMED 상태에서만 가능하다. Cycle 1로 정상 경로가 완성되면 즉시 방어 케이스를 추가한다.

#### 예상 테스트 구조
- 메서드: `CONFIRMED_아닌_주문_출고_시_예외가_발생한다()`
- Given: RESERVED Order
- When: `orderService.release("O001")`
- Then: `assertThatThrownBy(...).isInstanceOf(IllegalStateException.class)`

#### 예상 실패 이유
예외 처리 미구현 → IllegalStateException 미발생, 테스트 실패

---

### Cycle 3 (Safety) — RED Plan

#### 테스트할 동작
이미 출고된(RELEASE) 주문을 재출고 시도하면 `IllegalStateException`이 발생한다.

#### 이 테스트가 지금 필요한 이유
중복 출고 방지. 동일 패턴의 approve/reject 방어와 대칭을 이룬다.

#### 예상 테스트 구조
- 메서드: `이미_출고된_주문_재출고_시_예외가_발생한다()`
- Given: RELEASE Order
- When: `orderService.release("O001")`
- Then: `assertThatThrownBy(...).isInstanceOf(IllegalStateException.class)`

#### 예상 실패 이유
Cycle 2의 방어 코드가 CONFIRMED 체크이므로 RELEASE → 별도 실패 유발

---

### Cycle 4 — RED Plan

#### 테스트할 동작
`MonitoringService.getOrderSummary()` — 상태별 주문 목록을 반환하고 REJECTED는 포함하지 않는다.

#### 이 테스트가 지금 필요한 이유
모니터링의 핵심. REJECTED 제외 필터링이 정확히 동작해야 집계 화면이 의미를 갖는다.

#### 예상 테스트 구조
- 메서드: `상태별_주문_집계_시_REJECTED는_제외된다()`
- Given: RESERVED 2개, CONFIRMED 1개, REJECTED 1개, PRODUCING 1개 저장
- When: `monitoringService.getOrderSummary()`
- Then:
  - map.containsKey(REJECTED) == false
  - map.get(RESERVED).size() == 2
  - map.get(CONFIRMED).size() == 1

#### 예상 실패 이유
`MonitoringService` 클래스 미존재 → 컴파일 오류

---

### Cycle 5 — RED Plan

#### 테스트할 동작
`MonitoringService.judgeStockStatus()` — stock=0이면 DEPLETED(고갈)로 판정한다.

#### 이 테스트가 지금 필요한 이유
재고 상태 판정 로직의 첫 번째 케이스. 가장 명확한 조건부터 테스트한다.

#### 예상 테스트 구조
- 메서드: `재고_0이면_DEPLETED로_판정된다()`
- Given: stock=0인 Sample, confirmedQty=0
- When: `monitoringService.judgeStockStatus(sample, 0)`
- Then: `result == StockStatus.DEPLETED`

#### 예상 실패 이유
`MonitoringService.judgeStockStatus()` 미구현 → 컴파일 오류

---

### Cycle 6 — RED Plan

#### 테스트할 동작
`MonitoringService.judgeStockStatus()` — stock이 CONFIRMED 주문 합계보다 적으면 INSUFFICIENT(부족)으로 판정한다.

#### 이 테스트가 지금 필요한 이유
Cycle 5로 고갈 판정이 완성되면 부족 판정을 추가한다. 두 케이스가 모두 GREEN이 되어야 3단계 판정 로직이 완성된다.

#### 예상 테스트 구조
- 메서드: `재고가_CONFIRMED_합계보다_적으면_INSUFFICIENT로_판정된다()`
- Given: stock=30인 Sample, confirmedQty=50
- When: `monitoringService.judgeStockStatus(sample, 50)`
- Then: `result == StockStatus.INSUFFICIENT`

#### 예상 실패 이유
고갈 판정만 구현된 상태 → INSUFFICIENT 대신 SUFFICIENT 반환

---

## REVIEW 결과

### 테스트 실행 결과

| Cycle | 테스트 메서드 | 결과 |
|---|---|---|
| 1 | `출고_처리_시_RELEASE_전환_및_재고가_차감된다` | ✅ PASSED |
| 2 (Safety) | `CONFIRMED_아닌_주문_출고_시_예외가_발생한다` | ✅ PASSED |
| 3 (Safety) | `이미_출고된_주문_재출고_시_예외가_발생한다` | ✅ PASSED |
| 4 | `상태별_주문_집계_시_REJECTED는_제외된다` | ✅ PASSED |
| 5 | `재고_0이면_DEPLETED로_판정된다` | ✅ PASSED |
| 6 | `재고가_CONFIRMED_합계보다_적으면_INSUFFICIENT로_판정된다` | ✅ PASSED |

### 구현된 클래스

- `model/StockStatus`: SUFFICIENT / INSUFFICIENT / DEPLETED + 한국어 label
- `service/MonitoringService`: `getOrderSummary()`, `getStockStatusMap()`, `judgeStockStatus()`, `getTotalPendingQty()`
- `service/OrderService.release()`: CONFIRMED → RELEASE 전환 + `sample.stock -= quantity`
- `controller/MonitoringController`: 주문 현황 조회, 시료 재고 현황 표시
- `controller/ReleaseController`: CONFIRMED 목록 조회, 출고 처리

### 정리 적용

- 재고 판정 기준 수정: Phase 4 구현 후 CONFIRMED만 집계하던 `getTotalConfirmedQty()`를 CONFIRMED + PRODUCING 합산 집계 `getTotalPendingQty()`로 전환 (PRODUCING 상태 주문도 향후 재고 차감 대상이므로 함께 고려)
