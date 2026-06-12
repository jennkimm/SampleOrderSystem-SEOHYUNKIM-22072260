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

> 상세 설계·TDD 사이클·REVIEW 결과 → **[docs/design/phase4.md](design/phase4.md)**

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
