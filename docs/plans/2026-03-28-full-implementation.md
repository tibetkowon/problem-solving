# 📋 구현 계획서: 단원별 문제 풀이 및 풀이 이력 조회 API

> 작성일: 2026-03-28 | 상태: 진행중

## 요구사항 요약

단원별 문제 풀이 및 풀이 이력 조회 API. 핵심은 **랜덤 문제 제공 로직**, **부분 정답 판정**, **Redis 기반 정답률 캐싱**이다.

---

## 구현 범위

**포함**
- 단원 목록 조회
- 랜덤 문제 조회 (미풀이 + 직전 건너뜀 제외 + 정답률)
- 문제 제출 (객관식/주관식, 정답/부분정답/오답)
- 풀었던 문제 목록 / 상세 조회
- Redis 정답률 캐싱 + 5분 주기 DB 백업 스케줄러

**제외**
- 회원 가입/로그인 (userId는 요청 파라미터로 받는다고 가정)
- 단원 CRUD (조회만 구현)
- 문제 등록/수정 (관리자 기능 제외)

---

## 도메인 구조 (멀티 모듈)

```
problem-solving/
├── core/               ← 공통 도메인 (Entity, Repository, 상수)
├── api/                ← REST API (Controller, Service, DTO)
└── batch/              ← 스케줄러 (Redis → DB 백업)
```

---

## 엔티티 설계

| 엔티티 | 테이블 | 주요 필드 |
|--------|--------|-----------|
| `Chapter` | `chapter` | id, name |
| `Problem` | `problem` | id, chapterId, content, type(ENUM), explanation, answer(주관식) |
| `Choice` | `choice` | id, problemId, content, isCorrect |
| `UserProblem` | `user_problem` | id, userId, problemId, subjectiveAnswer, answerStatus(ENUM), skippedAt |
| `UserProblemChoice` | `user_problem_choice` | id, userProblemId, choiceId |
| `ProblemCorrectRate` | `problem_correct_rate` | problemId(PK), submitCount, correctCount |

**ENUM 상수**
- `ProblemType`: `OBJECTIVE_SINGLE`, `OBJECTIVE_MULTIPLE`, `SUBJECTIVE`
- `AnswerStatus`: `CORRECT`, `PARTIAL`, `WRONG`

---

## API 설계

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `GET` | `/api/chapters` | 단원 목록 조회 |
| `POST` | `/api/problems/random` | 랜덤 문제 조회 |
| `POST` | `/api/problems/submit` | 문제 제출 |
| `GET` | `/api/users/{userId}/problems` | 풀었던 문제 목록 |
| `GET` | `/api/users/{userId}/problems/{problemId}` | 풀었던 문제 상세 |

---

## 핵심 로직 상세

### 랜덤 문제 조회
1. 해당 챕터의 전체 문제 ID 조회
2. 사용자가 이미 푼 문제 ID 제외
3. 직전에 건너뛴 문제 ID 제외 (Redis: `skip:{userId}:{chapterId}`)
4. 남은 문제 중 랜덤 1개 반환
5. 없으면 `204 No Content` 반환

### 정답률 캐싱 (Redis)
- Key: `correct_rate:{problemId}` → `{submitCount, correctCount}` (Hash)
- 문제 제출 시 → Redis HINCRBY로 원자적 업데이트
- 문제 조회 시 → Redis에서 읽어 계산 (submitCount ≥ 30이면 반환, 미만이면 null)
- 스케줄러 (5분) → Redis 값을 읽어 `ProblemCorrectRate` 테이블 Upsert

### 부분 정답 판정 (객관식)
- 정답 선택지 ID 집합과 사용자 제출 선택지 ID 집합을 비교
- 교집합이 1개 이상 → `PARTIAL` 이상
- 완전 일치 → `CORRECT`
- 교집합 없음 → `WRONG`

---

## 작업 목록

| 순서 | 작업 | 대상 | 상태 |
|------|------|------|------|
| 1 | 멀티 모듈 프로젝트 초기 세팅 | `build.gradle`, `settings.gradle` | 대기 |
| 2 | 공통 상수(ENUM) 정의 | `ProblemType`, `AnswerStatus` | 대기 |
| 3 | Entity 클래스 작성 (5개) | `core` 모듈 | 대기 |
| 4 | Repository 인터페이스 작성 | `core` 모듈 | 대기 |
| 5 | Redis 설정 및 정답률 캐시 서비스 | `api` 모듈 | 대기 |
| 6 | 단원 조회 API | `ChapterController`, `ChapterService` | 대기 |
| 7 | 랜덤 문제 조회 API | `ProblemService`, `ProblemController` | 대기 |
| 8 | 문제 제출 API | `SubmitService`, `SubmitController` | 대기 |
| 9 | 풀었던 문제 목록/상세 조회 API | `HistoryController`, `HistoryService` | 대기 |
| 10 | Redis → DB 백업 스케줄러 | `batch` 모듈 | 대기 |
| 11 | H2 더미데이터 초기화 (`local` 프로파일) | `data.sql` | 대기 |
| 12 | 단위 테스트 | `*ServiceTest`, `*ControllerTest` | 대기 |
| 13 | Swagger 문서화 | 전체 Controller | 대기 |

---

## 고려사항

- **건너뛰기 Redis TTL**: 1시간
- **정답률 원자성**: HINCRBY 사용
- **주관식 정답 비교**: 정확 일치
- **멀티 모듈 의존성**: api → core, batch → core
