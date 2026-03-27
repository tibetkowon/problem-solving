# 아키텍처 문서

## 멀티 모듈 구성

```
problem-solving/
├── core/          # 공통 도메인 (Entity, Repository, 상수)
├── api/           # REST API 애플리케이션
└── batch/         # 스케줄 배치 애플리케이션
```

| 모듈 | 역할 | 의존 |
|------|------|------|
| `core` | Entity, Repository, 공통 상수 | Spring Data JPA, Redis, H2/MySQL |
| `api` | REST API, Service, DTO | core |
| `batch` | 정답률 동기화 스케줄러 | core |

---

## 도메인 모델

### Entity 관계

```
Chapter (단원)
  └── Problem (문제) [ManyToOne → Chapter]
        └── Choice (선택지) [ManyToOne → Problem]

UserProblem (사용자 풀이 이력)
  ├── Problem [ManyToOne]
  └── UserProblemChoice [OneToMany]
        └── Choice [ManyToOne]

ProblemCorrectRate (문제 정답률 집계)
  └── problemId (PK, Problem 참조)
```

### 주요 Entity 설명

| Entity | 테이블 | 설명 |
|--------|--------|------|
| `Chapter` | `chapter` | 단원 (자료구조, 알고리즘 등) |
| `Problem` | `problem` | 문제 (타입: 객관식단일/복수/주관식) |
| `Choice` | `choice` | 객관식 선택지 (isCorrect로 정답 구분) |
| `UserProblem` | `user_problem` | 사용자의 문제 풀이 기록 |
| `UserProblemChoice` | `user_problem_choice` | 사용자가 선택한 선택지 |
| `ProblemCorrectRate` | `problem_correct_rate` | 문제별 전체 제출/정답 횟수 집계 |

---

## API 서비스 구성

### 패키지 구조 (api 모듈)

```
com.problemsolving.api
├── config/
│   ├── RedisConfig.java          # RedisTemplate Bean 설정
│   └── SwaggerConfig.java        # OpenAPI 문서 설정
├── common/
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       ├── NoMoreProblemsException.java
│       └── ResourceNotFoundException.java
├── redis/
│   └── CorrectRateRedisService.java  # 정답률 캐시 / 건너뛰기 관리
├── chapter/
│   ├── controller/ChapterController.java
│   ├── service/ChapterService.java
│   └── dto/ChapterResponse.java
├── problem/
│   ├── controller/ProblemController.java
│   ├── service/ProblemService.java
│   └── dto/{RandomProblemRequest, RandomProblemResponse}.java
├── submit/
│   ├── controller/SubmitController.java
│   ├── service/SubmitService.java
│   └── dto/{SubmitRequest, SubmitResponse}.java
└── history/
    ├── controller/HistoryController.java
    ├── service/HistoryService.java
    └── dto/{HistoryListResponse, HistoryDetailResponse}.java
```

---

## Redis 설계

### 정답률 캐시

```
Key   : correct_rate:{problemId}
Type  : Hash
Field : submit  → 총 제출 횟수
        correct → 정답 횟수
```

- 문제 제출 시 `HINCRBY`로 원자적 증가
- 30명 이상 제출 시에만 정답률(%) 노출, 미만이면 `null` 반환
- 5분 주기로 `batch` 스케줄러가 DB(`problem_correct_rate`)에 동기화

### 건너뛰기 추적

```
Key   : skip:{userId}:{chapterId}
Type  : String (problemId 값)
TTL   : 1시간
```

- 랜덤 문제 조회 시 직전 건너뛴 문제 ID를 제외하여 반복 출제 방지

---

## 정답 판정 로직

| 문제 타입 | 판정 방식 | 결과 |
|-----------|-----------|------|
| `OBJECTIVE_SINGLE` | 제출 ID 집합 = 정답 ID 집합 | CORRECT / WRONG |
| `OBJECTIVE_MULTIPLE` | 교집합 존재 여부 + 완전 일치 여부 | CORRECT / PARTIAL / WRONG |
| `SUBJECTIVE` | 정답 문자열과 trim 후 정확 일치 | CORRECT / WRONG |

---

## 프로파일별 환경

| 항목 | local | server |
|------|-------|--------|
| DB | H2 in-memory (MySQL 호환 모드) | MySQL (환경변수) |
| DDL | `create-drop` | `validate` |
| 더미데이터 | `data.sql` 자동 적용 | 없음 |
| Redis | localhost:6379 | 환경변수(`REDIS_HOST`) |
| Swagger | `/swagger-ui.html` 노출 | 동일 |

---

## CI/CD

- **GitHub Actions** : `main` 브랜치 push 시 자동 빌드·배포
- **Docker Compose** : `api`, `batch`, MySQL, Redis 컨테이너 구동
