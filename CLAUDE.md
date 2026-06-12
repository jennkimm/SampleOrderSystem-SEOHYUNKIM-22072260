# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

Java/Gradle 기반 JSON CRUD 콘솔 애플리케이션 (주문 시스템). 
데이터를 JSON 파일로 저장하며 콘솔 UI로 제품을 관리한다.

> **도메인 요구사항**: 기능 명세, 주문 상태 흐름, 비즈니스 규칙, 생산량 계산 공식은 **[AGENTS.md](AGENTS.md)** 를 참조한다.

## PoC 기반 개발 원칙

아래 4개 PoC에서 검증된 패턴을 참조하여 구현한다.

| PoC | 참조 목적 | 저장소 |
|---|---|---|
| `ConsoleMVC` | MVC 패키지 구조·역할 분리, 메뉴 루프 패턴 | https://github.com/jennkimm/ConsoleMVC-SEOHYUNKIM-22072260 |
| `DataPersistence` | JSON 파일 기반 영속성, CRUD 구조 | https://github.com/jennkimm/DataPersistence-SEOHYUNKIM-22072260 |
| `DataMonitor` | 모니터링 화면 레이아웃, 상태별 집계 표기 | https://github.com/jennkimm/DataMonitor-SEOHYUNKIM-22072260 |
| `DummyDataGenerator` | 테스트용 더미 데이터 생성 패턴 | https://github.com/jennkimm/DummyDataGenerator-SEOHYUNKIM-22072260 |

## 구현 계획

Phase별 상세 목표와 테스트 포인트는 **[docs/PLAN.md](docs/PLAN.md)** 참조.

| Phase | 목표 | 핵심 확인 포인트 |
|---|---|---|
| Phase 1 | 프로젝트 골격 + 시료 관리 (등록·조회·검색·JSON 영속성) | 재시작 후 데이터 유지 |
| Phase 2 | 주문 접수 + 승인/거절 (실효 재고 기반 CONFIRMED/PRODUCING 자동 분기) | 재고 충분·부족 분기, 동일 시료 생산 중 승인 차단, 선행 CONFIRMED/PRODUCING 주문 수량 반영한 실효 재고 계산 |
| Phase 3 | 모니터링 + 출고 처리 (상태별 집계, 재고 차감) | REJECTED 제외 집계, 출고 후 재고 반영 |
| Phase 4 | 생산 라인 (FIFO 큐, 생산량 공식, 완료 시 CONFIRMED 전환) | 생산 완료 → 출고까지 전체 흐름 완주 |

## 빌드 환경

- **빌드 도구**: Gradle (Gradle Wrapper 사용)
- **Java 버전**: Java 21

## 런타임 데이터

- `data/` 폴더는 `.gitignore`에 등록되어 git 추적에서 제외된다.
- 애플리케이션 실행 시 `data/` 폴더가 없으면 자동 생성된다.
- 데이터 파일 경로: `data/samples.json`, `data/orders.json`, `data/production.json`

## 빌드 및 실행 명령

```bash
# 빌드
gradlew.bat build

# 테스트 실행 (전체)
gradlew.bat test

# 컴파일만
gradlew.bat compileJava

# 특정 테스트 클래스 실행
gradlew.bat test --tests "com.ssemi.sampleorder.regression.OrderServiceRegressionTest"
gradlew.bat test --tests "com.ssemi.sampleorder.safety.OrderServiceSafetyTest"
```

## 주요 의존성

| 라이브러리 | 용도 |
|---|---|
| `jackson-databind` | JSON 직렬화/역직렬화 |
| `jackson-datatype-jsr310` | Java 날짜/시간 타입 지원 |
| `junit-jupiter` | 테스트 프레임워크 |
| `assertj-core` | 테스트 어서션 |

## 코딩 컨벤션

- 클래스명: `UpperCamelCase`
- 메서드·변수명: `lowerCamelCase`
- 상수: `UPPER_SNAKE_CASE`
- 패키지명: 소문자 (`com.ssemi.sampleorder`)
- `DataNotFoundException` 메시지 표준 형식: `"ID {id} 에 해당하는 {entity}{조사} 찾을 수 없습니다."` — 조사는 한국어 문법에 맞게 선택 (예: 시료**를**, 주문**을**)
- 단일 메서드 30줄 초과, 중첩 if/for 3단계 이상은 리팩토링 대상

## `.claude/` skill 활용

`.claude/` 아래에 정의된 모든 에이전트와 슬래시 커맨드를 적극 활용한다.

### 슬래시 커맨드 (`.claude/commands/`)

| 커맨드 | 설명 | 사용 시점 |
|---|---|---|
| `/test-driven-development` | Red-Green-Review 사이클 기반 TDD 진행 | 모든 기능 개발·버그 수정 시 구현 코드 작성 전 |

**TDD 절차 요약**: Plan.md 작성 → 사람 검토 → 테스트 작성(RED) → 최소 구현(GREEN) → 사람 검토(REVIEW) → 다음 사이클

### 서브에이전트 (`.claude/agents/`)

코드 변경 후 아래 순서대로 실행하여 품질을 유지한다.

| 에이전트 | 역할 | 사용 시점 |
|---|---|---|
| `subagent1-doc-consistency` | 코드↔문서 정합성 검증 | 메서드 시그니처 변경, PR 전 |
| `subagent2-ai-action` | 코드 자동 개선 (null 가드, 중복 제거 등) | 취약점 수정, 신규 엔티티 스캐폴딩 |
| `subagent3-test-verify` | 테스트 실행 및 커버리지 분석 | 코드 변경 후, 머지 전 |
| `subagent4-compliance-verify` | 컨벤션·보안·라이선스 준수 검증 | 의존성 추가, PR 최종 확인 |

