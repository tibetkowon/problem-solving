---
name: unit-commit
description: >
  개발 단위(Entity, Service, Controller, 테스트 등)가 완성될 때마다 자동으로 git commit을 수행하는 스킬.
  사용자가 별도로 "커밋해줘"라고 말하지 않아도 다음 상황에서 즉시 적용한다:
  - 하나의 Entity/클래스/모듈 구현이 완료됐을 때
  - 단위 테스트 작성이 완료됐을 때
  - 하나의 API(Controller + Service + Repository)가 완성됐을 때
  - 리팩토링 작업 단위가 마무리됐을 때
  - 구현 계획의 작업 목록에서 한 항목이 완료됐을 때
  "모두 다 만들고 한 번에 커밋"하는 방식은 허용하지 않는다. 작은 단위로 자주 커밋해야 변경 이력이 명확해진다.
---

# 개발 단위별 자동 커밋 스킬

## 왜 단위별로 커밋하는가

커밋은 개발 이력의 최소 단위다. 기능 전체를 하나의 커밋으로 만들면 나중에 어떤 변경이 왜 일어났는지 추적하기 어렵다.
작은 단위로 커밋하면 코드 리뷰가 쉬워지고, 문제가 생겼을 때 정확한 시점으로 되돌릴 수 있다.

---

## 커밋 타이밍 — 언제 커밋하는가

구현 계획의 **작업 목록 한 항목이 완료될 때마다** 커밋한다.

| 완료된 작업 | 커밋 예시 |
|------------|----------|
| Entity 클래스 작성 | `feat(domain): add Chapter entity` |
| Repository 작성 | `feat(domain): add ChapterRepository` |
| Service 구현 | `feat(chapter): implement chapter list service` |
| Controller 구현 | `feat(chapter): add GET /api/chapters endpoint` |
| 단위 테스트 작성 | `test(chapter): add ChapterService unit tests` |
| 스케줄러 구현 | `feat(batch): add Redis→DB correct rate sync scheduler` |
| 설정 파일 변경 | `chore(config): add Redis configuration` |
| 더미데이터 추가 | `chore(data): add local profile seed data` |

---

## 커밋 메시지 형식

```
<type>(<scope>): <한국어 또는 영어 간결한 설명>
```

**type 분류:**
- `feat` — 새 기능 구현
- `test` — 테스트 코드 작성
- `fix` — 버그 수정
- `refactor` — 리팩토링 (기능 변경 없음)
- `chore` — 설정, 의존성, 빌드 스크립트 등
- `docs` — 문서 변경

**scope:** 변경된 도메인 또는 레이어 (chapter, problem, submit, history, redis, batch 등)

**예시:**
```
feat(problem): add Problem and Choice entities
test(problem): add ProblemService random query unit test
chore(redis): configure Redis connection for correct rate cache
feat(submit): implement answer submission with partial correct judgment
```

---

## 커밋 실행 절차

### 1단계: 변경 파일 확인
```bash
git status
git diff --stat
```
커밋 대상이 이번 작업과 관련된 파일인지 확인한다.
관련 없는 파일이 섞여 있으면 해당 파일은 제외하고 필요한 파일만 스테이징한다.

### 2단계: 스테이징
```bash
# 특정 파일만 스테이징 (권장)
git add <파일경로1> <파일경로2> ...

# 해당 디렉토리 전체가 이번 작업 단위인 경우
git add <디렉토리경로>/
```

민감한 파일(.env, 시크릿 키 등)이 포함되지 않도록 주의한다.

### 3단계: 커밋
```bash
git commit -m "feat(scope): 작업 내용 요약"
```

### 4단계: 커밋 완료 보고
```
커밋 완료: feat(chapter): add Chapter entity and repository
다음 작업: [다음 작업 목록 항목]
```

---

## 커밋하지 않는 경우

- 빌드가 실패하는 상태 → 먼저 빌드를 고친 후 커밋
- 테스트가 실패하는 상태 → 먼저 테스트를 통과시킨 후 커밋
- 작업이 절반만 완료된 상태 → 작업 단위가 완성될 때까지 기다림

빌드/테스트 상태는 `verify_implementation` 스킬이 담당한다. 검증이 통과된 후 이 스킬이 커밋을 수행한다.
