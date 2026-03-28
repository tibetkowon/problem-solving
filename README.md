# 단원별 문제 풀이 API

단원별 CS 문제를 랜덤으로 제공하고, 풀이 이력을 조회할 수 있는 REST API 서버입니다.

## 주요 기능

- 단원 목록 조회
- 미풀이 문제 중 랜덤 1개 제공 (문제 넘기기 지원, 직전 건너뛴 2문제 제외)
- 챕터별 전체 문제 수 / 푼 문제 수 제공 (클라이언트 스킵 버튼 제어용)
- 객관식(단일/복수) / 주관식 답안 제출 및 즉시 채점
- 풀이 이력 목록 및 상세 조회
- Redis 기반 실시간 정답률 캐싱 (30명 이상 제출 시 노출)
- 5분 주기 배치로 Redis → DB 정답률 동기화

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.4 |
| ORM | Spring Data JPA |
| DB | MySQL 8.0 (server), H2 (local) |
| Cache | Redis |
| Build | Gradle (멀티 모듈) |
| Docs | SpringDoc OpenAPI (Swagger) |
| Infra | Docker Compose, GitHub Actions |

## 모듈 구성

```
problem-solving/
├── core/    # Entity, Repository, 공통 상수
├── api/     # REST API 서버 (포트 8080)
└── batch/   # 정답률 동기화 스케줄러 (5분 주기)
```

## 실행 방법

### 로컬 환경 (H2 + local Redis)

```bash
# Redis 실행 (Docker 사용 시)
docker run -d --name redis -p 6379:6379 redis:latest

# api 서버 실행 (local 프로파일 자동 활성화)
./gradlew :api:bootRun
```

서버 기동 후 Swagger UI: http://localhost:8080/swagger-ui.html

### Docker Compose (전체 스택 실행)

```bash
# 1. 환경변수 파일 생성
cp .env.example .env
# .env 파일을 열어 값 수정

# 2. 전체 서비스 실행 (api + batch + mysql + redis)
docker compose up -d
```

### 환경변수 (.env)

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `DOCKER_USERNAME` | Docker Hub 아이디 | `myusername` |
| `MYSQL_ROOT_PASSWORD` | MySQL root 비밀번호 | `rootpassword` |
| `MYSQL_DATABASE` | DB 이름 | `problemsolving` |
| `MYSQL_USER` | DB 유저명 | `appuser` |
| `MYSQL_PASSWORD` | DB 비밀번호 | `apppassword` |

## API 명세

### 단원 (Chapter)
| Method | URL | 설명 | 응답 |
|--------|-----|------|------|
| GET | `/api/chapters` | 단원 목록 조회 | 200 |

### 문제 (Problem)
| Method | URL | 설명 | 응답 |
|--------|-----|------|------|
| POST | `/api/problems/random` | 랜덤 문제 조회 | 200 |
| POST | `/api/problems/submit` | 문제 제출 및 채점 | 200 |

#### 랜덤 문제 조회 요청/응답

```json
// 요청 (skipProblemId는 선택 — 문제 넘기기 시 현재 문제 ID 전달)
{
  "chapterId": 1,
  "userId": 1,
  "skipProblemId": 2
}

// 응답
{
  "problemId": 3,
  "content": "문제 내용",
  "problemType": "SUBJECTIVE",
  "choices": [],
  "answerCorrectRate": 67,
  "totalCount": 10,
  "solvedCount": 3
}
```

### 풀이 이력 (History)
| Method | URL | 설명 | 응답 |
|--------|-----|------|------|
| GET | `/api/users/{userId}/problems` | 풀이 이력 목록 | 200 |
| GET | `/api/users/{userId}/problems/{problemId}` | 풀이 이력 상세 | 200 |

전체 API 명세는 Swagger UI(`/swagger-ui.html`)에서 확인하세요.

## 정답 판정

| 타입 | 결과 |
|------|------|
| 완전 일치 | `CORRECT` |
| 복수 정답 일부 일치 | `PARTIAL` |
| 불일치 | `WRONG` |

## CI/CD

`main` 브랜치에 push 시 GitHub Actions가 자동으로 실행됩니다.

**필요한 GitHub Secrets:**

| Secret | 설명 |
|--------|------|
| `DOCKER_USERNAME` | Docker Hub 아이디 |
| `DOCKER_PASSWORD` | Docker Hub 비밀번호 |
| `SERVER_HOST` | 배포 서버 IP |
| `SERVER_USERNAME` | 서버 SSH 유저명 |
| `SERVER_SSH_KEY` | 서버 SSH 개인키 |

**배포 흐름:**
```
main push → 빌드 & 테스트 → Docker 이미지 빌드 & 푸시 → 서버 SSH 배포
```

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# api 모듈만
./gradlew :api:test
```

## 더미 데이터 (local 프로파일)

H2 실행 시 `data.sql`이 자동으로 적용됩니다.

| 단원 | 문제 수 | 타입 |
|------|---------|------|
| 자료구조 | 3 | 객관식단일, 객관식복수, 주관식 |
| 알고리즘 | 2 | 객관식단일, 주관식 |
| 운영체제 | 1 | 객관식단일 |
