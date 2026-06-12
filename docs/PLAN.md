# PLAN.md — 반도체 시료 생산주문관리 시스템 구현 계획

> 각 Phase는 독립적으로 실행·테스트 가능한 동작하는 SW를 목표로 한다.
> 도메인 요구사항은 [AGENTS.md](../AGENTS.md), 개발 환경·PoC 참조는 [CLAUDE.md](../CLAUDE.md) 참조.

---

## 도메인 모델 (공통 기술 참조)

| 엔티티 | 속성 |
|---|---|
| `Sample` | `sampleId`, `name`, `avgProductionTime` (double, 분, 소수점 한 자리), `yieldRate` (0.0~1.0), `stock` (ea) |
| `Order` | `orderId`, `sampleId`, `customerName`, `quantity`, `status` |
| `ProductionLine` | `orderId`, `scheduledQty`, `producedQty`, `estimatedTime` |

**생산량·시간 계산 공식** (Phase 4에서 구현):
```
실 생산량  = ceil(부족분 / (yieldRate × 0.9))   ← 올림 처리
총 생산시간 = avgProductionTime × 실 생산량
```

---

## Phase 1 — 프로젝트 골격 + 시료 관리

> 상세 설계·TDD 사이클·REVIEW 결과 → **[docs/design/phase1.md](design/phase1.md)**

---

## Phase 2 — 주문 접수 + 승인/거절

> 상세 설계·TDD 사이클·REVIEW 결과 → **[docs/design/phase2.md](design/phase2.md)**

---

## Phase 3 — 모니터링 + 출고 처리

> 상세 설계·TDD 사이클·REVIEW 결과 → **[docs/design/phase3.md](design/phase3.md)**

---

## Phase 4 — 생산 라인

### 목표
PRODUCING 상태 주문의 생산 스케줄링을 FIFO 큐로 관리하고,
생산 완료 시 CONFIRMED로 전환하는 생산 라인 기능을 완성하여 전체 주문 흐름을 닫는다.

### 구현 범위
- `ProductionLine` 엔티티 (도메인 모델 참조)
- `ProductionQueue` — `java.util.Queue<ProductionLine>` FIFO 구조, JSON 영속화
- Phase 2의 PRODUCING 전환 시점에 `ProductionLine` 생성 및 큐 enqueue
- 생산량·시간 계산 공식 구현 (도메인 모델 공식 참조, `Math.ceil` 사용)
- 생산 현황 표시: 현재 큐 head의 주문 정보·예상 생산량·예상 시간
- 대기 주문(큐 전체) 목록 출력
- 생산 완료 처리: 큐에서 dequeue → `PRODUCING` → `CONFIRMED` + `sample.stock += scheduledQty`
- Regression Test: 공식 계산 정확성, FIFO 순서 보장, 완료 후 상태 전환
- Safety Test: 큐 비어있을 때 완료 처리 시도, yieldRate 경계값(0, 1)

### 테스트 포인트 (사용자 확인 항목)
- [ ] 재고 부족 주문 승인 시 생산 라인에 자동 등록되는가
- [ ] 생산 큐가 FIFO 순서(먼저 들어온 것이 먼저 출력)로 표시되는가
- [ ] 생산량이 공식(`ceil(부족분 / (수율 × 0.9))`)대로 올림 처리되는가
- [ ] 생산 완료 처리 후 주문이 CONFIRMED로 전환되는가
- [ ] 생산 완료 후 해당 시료의 재고가 증가하는가
- [ ] 생산 완료 → 출고(Phase 3)까지 이어지는 전체 흐름이 동작하는가

---

---

## TDD 사이클 기록 · REVIEW 결과 — Phase 1

> Phase 1 TDD 사이클·REVIEW 결과 → **[docs/design/phase1.md](design/phase1.md)**

---

## Phase 진행 순서 요약

```
Phase 1  시료 관리 (등록·조회·검색·JSON 영속성)
   ↓
Phase 2  주문 흐름 (접수·승인 분기·거절)
   ↓
Phase 3  모니터링 + 출고 (전체 상태 가시성 + 재고 차감)
   ↓
Phase 4  생산 라인 (FIFO 큐·생산 계산·완료 처리 → 전체 흐름 완결)
```
