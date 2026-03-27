# 단원별 문제 풀이 API

단원별 CS 문제를 랜덤으로 제공하고, 풀이 이력을 조회할 수 있는 REST API 서버입니다.

## 주요 기능

- 단원 목록 조회
- 미풀이 문제 중 랜덤 1개 제공 (직전 건너뛴 문제 제외)
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
| DB | MySQL (server), H2 (local) |
| Cache | Redis |
| Build | Gradle (멀티 모듈) |
| Docs | SpringDoc OpenAPI (Swagger) |
| Infra | Docker Compose, GitHub Actions |

## 모듈 구성

```
problem-solving/
├── core/    # Entity, Repository, 공통 상수
├── api/     # REST API 서버
└── batch/   # 정답률 동기화 스케줄러
```

자세한 내용은 [아키텍처 문서](docs/architecture.md)를 참고하세요.

## 실행 방법

### 로컬 환경 (H2)

```bash
# 빌드
./gradlew build

# api 서버 실행 (local 프로파일 자동 활성화)
./gradlew :api:bootRun
```

서버 기동 후 Swagger UI: http://localhost:8080/swagger-ui.html

### Docker Compose (서버 환경)

```bash
# 환경변수 설정
export DB_URL=jdbc:mysql://db:3306/problemsolving
export DB_USERNAME=root
export DB_PASSWORD=password
export REDIS_HOST=redis

docker-compose up -d
```

## API 명세

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/chapters` | 단원 목록 조회 |
| POST | `/api/problems/random` | 랜덤 문제 조회 |
| POST | `/api/problems/submit` | 문제 제출 |
| GET | `/api/users/{userId}/problems` | 풀이 이력 목록 |
| GET | `/api/users/{userId}/problems/{problemId}` | 풀이 이력 상세 |

전체 API 명세는 Swagger UI(`/swagger-ui.html`)에서 확인하세요.

## 정답 판정

| 타입 | 결과 |
|------|------|
| 완전 일치 | `CORRECT` |
| 복수 정답 일부 일치 | `PARTIAL` |
| 불일치 | `WRONG` |

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
