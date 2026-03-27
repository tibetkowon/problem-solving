---
name: architecture-docs
description: >
  DB 스키마, 엔티티 관계, 서비스 구성이 변경될 때마다 /docs/architecture.md를 자동으로 업데이트하는 스킬.
  사용자가 명시적으로 요청하지 않아도 다음 상황에서 즉시 적용한다:
  - Entity/Table 클래스를 새로 만들거나 수정했을 때
  - Repository, Service, Controller 구조가 바뀌었을 때
  - 연관관계(@OneToMany, @ManyToOne, @ManyToMany 등)가 추가되거나 변경됐을 때
  - 새 모듈이나 도메인 영역이 추가됐을 때
  - DB 마이그레이션 파일이 변경됐을 때
  - 아키텍처 문서 확인을 원할 때 ("구조 알려줘", "DB 설계 보여줘", "서비스 구성 알려줘")
  코드와 문서가 항상 동기화되도록 유지한다. 아키텍처 변경 후 문서 없이 작업을 완료하는 것은 허용하지 않는다.
---

# 아키텍처 문서화 스킬 (자동 적용)

## Trigger — 언제 이 스킬을 실행하는가

다음 중 하나라도 해당되면 사용자의 별도 언급 없이 **즉시 이 스킬을 적용**한다:

- Entity 클래스 생성·수정 (테이블 구조 변경)
- 연관관계 추가·변경 (@OneToMany, @ManyToOne, @ManyToMany, @OneToOne)
- Repository / Service / Controller 구조 변경
- 새 도메인 영역(패키지) 추가
- DB 마이그레이션 파일 변경 (Flyway, Liquibase 등)

**변경 사항을 코드에 반영한 직후** `/docs/architecture.md`를 업데이트한다.

---

## 실행 절차

### 1단계: 현재 코드 분석

변경된 파일과 관련 파일을 읽어 다음을 파악한다:

- Entity 클래스 목록과 필드, 어노테이션
- 연관관계 (방향, 다중성, cascade, fetch 전략)
- Repository 인터페이스 — 어떤 Entity를 다루는지
- Service 클래스 — 어떤 Repository를 주입받고, 주요 비즈니스 역할
- Controller — 어떤 Service를 주입받고, 담당 API 경로

### 2단계: /docs/architecture.md 업데이트

파일이 없으면 아래 전체 구조로 신규 생성한다.
파일이 있으면 변경된 섹션만 수정한다 — 전체 재작성 금지.

---

## /docs/architecture.md 파일 구조

```markdown
# 아키텍처 문서

> 마지막 업데이트: YYYY-MM-DD

---

## 도메인 구조

프로젝트의 주요 도메인(패키지) 목록과 각 도메인의 역할을 설명한다.

| 도메인 | 역할 |
|--------|------|
| user | 사용자 인증·관리 |
| product | 상품 목록·상세 관리 |

---

## DB 테이블 및 엔티티

각 Entity 클래스와 대응하는 테이블 구조를 기술한다.

### [엔티티명] (`테이블명`)

| 필드명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | Long | PK, AUTO_INCREMENT | 기본키 |
| name | String | NOT NULL | 이름 |

---

## 엔티티 관계 (ERD 요약)

텍스트로 엔티티 간 관계를 표현한다.

```
User (1) ─────── (N) Order
Order (N) ─────── (N) Product  [중간 테이블: order_product]
```

### 관계 상세

| 엔티티 A | 관계 | 엔티티 B | 방향 | 비고 |
|----------|------|----------|------|------|
| User | 1:N | Order | 단방향 | cascade=ALL |
| Order | N:M | Product | 양방향 | 중간 테이블 사용 |

---

## 서비스 구성

레이어별 클래스 목록과 의존 관계를 기술한다.

### Controller 레이어

| 클래스 | 경로 prefix | 주요 API | 의존 Service |
|--------|-------------|----------|--------------|
| UserController | /api/users | 회원가입, 로그인, 조회 | UserService |

### Service 레이어

| 클래스 | 역할 | 의존 Repository |
|--------|------|----------------|
| UserService | 사용자 비즈니스 로직 | UserRepository |

### Repository 레이어

| 인터페이스 | 대상 Entity | 주요 쿼리 메서드 |
|-----------|-------------|----------------|
| UserRepository | User | findByEmail, existsByEmail |

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| 프레임워크 | Spring Boot |
| DB | MySQL / H2 |
| ORM | JPA / Hibernate |
| 마이그레이션 | Flyway (선택) |
```

---

## 작성 규칙

- **언어**: 한국어로 작성한다. 클래스명·필드명·어노테이션 등 기술 식별자는 영어 그대로 사용한다.
- **코드에서 확인된 사실만 기록한다** — 추측이나 미래 계획은 기록하지 않는다.
- **기존 내용을 불필요하게 삭제하지 않는다** — 삭제된 Entity·관계는 제거하되, 나머지는 유지
- **마지막 업데이트 날짜**를 항상 오늘 날짜로 갱신한다
- ERD 요약은 복잡하면 주요 관계만 포함해도 된다

---

## 완료 보고

```
아키텍처 문서 업데이트 완료:
- [변경된 내용 요약 (예: Order 엔티티 추가, User-Order 1:N 관계 반영)]
```
