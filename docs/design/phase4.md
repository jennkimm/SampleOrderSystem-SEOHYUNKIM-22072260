# Phase 4 — 생산 라인

> 상위 계획: [PLAN.md](../PLAN.md)  
> 도메인 요구사항: [AGENTS.md](../../AGENTS.md)

---

## 목표

PRODUCING 상태 주문의 생산 스케줄링을 FIFO 큐로 관리하고,
생산 완료 시 CONFIRMED로 전환하는 생산 라인 기능을 완성하여 전체 주문 흐름을 닫는다.

---

## 구현 범위

- `ProductionLine` 엔티티: `orderId`, `scheduledQty`, `producedQty`, `estimatedTime`
- `ProductionLineRepository`: JSON 영속화, FIFO 순서 보장
- `ProductionService`:
  - `enqueue(orderId)`: 부족분 계산 → 공식으로 `scheduledQty` 산출 → `ProductionLine` 생성 및 저장
  - `completeNext()`: 큐 head dequeue → 주문 CONFIRMED + `sample.stock += scheduledQty`
  - `getQueue()`: 현재 대기 목록 반환
- `OrderController.approve()` 연동: PRODUCING 반환 시 자동 `enqueue()` 호출
- `ProductionController` 개선: 큐 현황 표시, 생산 완료 처리
- Regression Test: 공식 계산 정확성, FIFO 순서 보장, 완료 후 상태 전환
- Safety Test: 빈 큐 완료 처리 예외, yieldRate = 0 경계값 예외

---

## 도메인 모델 추가

| 클래스 | 역할 |
|---|---|
| `ProductionLine` | 생산 스케줄 단위 엔티티 |
| `ProductionLineRepository` | JSON 파일(`data/production.json`) 기반 FIFO 큐 영속화 |
| `ProductionService` | 생산 큐 관리·완료 처리 비즈니스 로직 |

---

## 생산량·시간 계산 공식

```
부족분(shortfall)  = order.quantity - sample.stock  (approve 시점)
실 생산량(scheduledQty) = ceil(shortfall / (yieldRate × 0.9))
총 생산시간(estimatedTime) = avgProductionTime × scheduledQty
```

> `yieldRate = 0`이면 0으로 나누기 발생 → `IllegalArgumentException` 발생

---

## 상태 흐름 (Phase 4 완성 후 전체)

```
RESERVED ──approve()──→ CONFIRMED  (재고 충분)  → release() → RELEASE
                  └───→ PRODUCING  (재고 부족)
                               │
                        productionService.enqueue()
                        (자동: approve 후 controller 호출)
                               │
                        completeNext()
                        재고 += scheduledQty
                               │
                               ▼
                          CONFIRMED ─────────────→ RELEASE  재고 -=quantity
```

---

## TDD 사이클 계획

### Cycle 1 — RED Plan: ProductionLine 공식 계산

#### 테스트할 동작
`ProductionLine` 생성 시 shortfall·yieldRate·avgProductionTime으로
`scheduledQty = ceil(shortfall / (yieldRate × 0.9))`, `estimatedTime = avgProductionTime × scheduledQty`가 계산된다.

#### 이 테스트가 지금 필요한 이유
ProductionLine 엔티티의 핵심 불변식. 공식이 올바르게 동작하는지 가장 먼저 검증한다.

#### 예상 테스트 구조
- 메서드명: `scheduledQtyAndEstimatedTimeAreCalculatedByFormula()`
- Given: shortfall=10, yieldRate=0.75, avgProductionTime=2.5
- When: `new ProductionLine(orderId, shortfall, yieldRate, avgProductionTime)`
- Then: `scheduledQty = ceil(10 / (0.75 × 0.9)) = ceil(14.81) = 15`, `estimatedTime = 2.5 × 15 = 37.5`

#### 예상 실패 이유
`ProductionLine` 클래스가 없어 컴파일 실패 → 스텁 생성 후 로직 없음으로 실패

---

### Cycle 2 — RED Plan: ProductionLineRepository FIFO 순서

#### 테스트할 동작
`enqueue()` 순서대로 `findAll()` 반환 시 먼저 추가된 항목이 index 0에 위치한다 (FIFO).

#### 이 테스트가 지금 필요한 이유
Cycle 1이 엔티티 검증이었다면, Cycle 2는 영속화 계층의 FIFO 순서를 검증한다.

#### 예상 테스트 구조
- 메서드명: `enqueueOrderIsPreservedInFindAll()`
- Given: ProductionLine A, B 순서로 enqueue
- Then: `findAll().get(0).getOrderId() = A.orderId`, `get(1).getOrderId() = B.orderId`

#### 예상 실패 이유
`ProductionLineRepository` 클래스 없어 컴파일 실패 → 스텁 후 저장 로직 없음으로 실패

---

### Cycle 3 — RED Plan: ProductionService.enqueue()

#### 테스트할 동작
`productionService.enqueue(orderId)` 호출 시 OrderRepository·SampleRepository에서 주문·시료를 조회해
공식으로 `scheduledQty`를 계산하고 `ProductionLineRepository`에 저장한다.

#### 이 테스트가 지금 필요한 이유
Repository들을 조합해 비즈니스 로직을 실행하는 서비스 계층의 첫 번째 Regression 케이스.

#### 예상 테스트 구조
- 메서드명: `enqueueCreatesProductionLineWithCorrectScheduledQty()`
- Given: PRODUCING 주문(O-001, sampleId=S-001, qty=100), 시료 stock=90, yieldRate=0.75
- When: `productionService.enqueue("O-001")`
- Then: `repository.findAll().size() == 1`, `scheduledQty == ceil(10 / (0.75×0.9)) == 15`

#### 예상 실패 이유
`ProductionService` 클래스 없어 컴파일 실패 → 스텁 후 empty 구현으로 실패

---

### Cycle 4 — RED Plan: ProductionService.completeNext()

#### 테스트할 동작
`completeNext()` 호출 시 큐 head dequeue, 해당 주문 `CONFIRMED` 전환, `sample.stock += scheduledQty`.

#### 이 테스트가 지금 필요한 이유
생산 완료 흐름의 핵심: 큐 관리·주문 상태·재고 세 가지를 원자적으로 처리하는지 검증.

#### 예상 테스트 구조
- 메서드명: `completeNextTransitionsOrderToConfirmedAndIncrementsStock()`
- Given: PRODUCING 주문 enqueue 완료, stock=90, scheduledQty=15
- When: `productionService.completeNext()`
- Then: 주문 상태 `CONFIRMED`, `sample.stock == 90 + 15 == 105`, 큐 size == 0

#### 예상 실패 이유
`completeNext()` 미구현으로 실패

---

### Cycle 5 — RED Plan (Safety): 빈 큐 완료 처리 예외

#### 테스트할 동작
큐가 비어있을 때 `completeNext()` 호출 시 `IllegalStateException` 발생한다.

#### 예상 테스트 구조
- 메서드명: `completeNextThrowsWhenQueueIsEmpty()`
- Given: 빈 ProductionLineRepository
- Then: `assertThatThrownBy(() -> productionService.completeNext()).isInstanceOf(IllegalStateException.class)`

---

### Cycle 6 — RED Plan (Safety): yieldRate = 0 예외

#### 테스트할 동작
`yieldRate = 0`인 시료로 `enqueue()` 시 `IllegalArgumentException` 발생한다.

#### 예상 테스트 구조
- 메서드명: `enqueueThrowsWhenYieldRateIsZero()`
- Given: PRODUCING 주문, 시료 yieldRate=0.0
- Then: `assertThatThrownBy(...).isInstanceOf(IllegalArgumentException.class)`

---

## 테스트 포인트 (사용자 확인 항목)

- [ ] 재고 부족 주문 승인 시 생산 라인에 자동 등록되는가
- [ ] 생산 큐가 FIFO 순서로 표시되는가
- [x] `scheduledQty = ceil(부족분 / (수율 × 0.9))`대로 올림 처리되는가
- [ ] 생산 완료 후 주문이 CONFIRMED로 전환되는가
- [ ] 생산 완료 후 해당 시료의 재고가 `scheduledQty`만큼 증가하는가
- [ ] 생산 완료 → 출고(Phase 3)까지 이어지는 전체 흐름이 동작하는가

---

## REVIEW

### 테스트 실행 결과

| Cycle | 테스트 메서드 | 결과 |
|---|---|---|
| 1 | `scheduledQtyAndEstimatedTimeAreCalculatedByFormula` | ✅ PASSED |
| 2 | `enqueueOrderIsPreservedInFindAll`, `dequeueHeadRemovesFirstEntry` | ✅ PASSED |
| 3 | `enqueueCreatesProductionLineWithCorrectScheduledQty` | ✅ PASSED |
| 4 | `completeNextTransitionsOrderToConfirmedAndIncrementsStock` | ✅ PASSED |
| 5 | `completeNextThrowsWhenQueueIsEmpty` | ✅ PASSED |
| 6 | `enqueueThrowsWhenYieldRateIsZero` | ✅ PASSED |

### 정리 적용

- `OrderService.completeProduction()` — Phase 4 `ProductionService.completeNext()`로 완전히 대체하여 제거
- `OrderController.approve()` — PRODUCING 결과 시 `productionService.enqueue()` 자동 호출 추가
- `ProductionController` — `ProductionService` 기반으로 전면 교체, 큐 목록 표시 추가
- `Main.java` — `ProductionLineRepository`, `ProductionService` 초기화 추가
