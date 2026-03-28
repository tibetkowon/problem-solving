# 📋 구현 계획서: 랜덤 문제 조회 개편 + Batch 배포

## 1. 랜덤 문제 조회 개편

### 요구사항 요약
`/api/problems/skip` 엔드포인트를 제거하고, `/api/problems/random`에 건너뛰기 기능을 통합한다. 이전에 건너뛴 문제 조회(DB) 기능은 제거한다.

### 변경 내용
| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| skip 엔드포인트 | `POST /api/problems/skip` 별도 존재 | 제거 |
| random 요청 | `{ chapterId, userId }` | `{ chapterId, userId, skipProblemId? }` |
| 건너뛰기 처리 | skip API 호출 후 random 재조회 | random 조회 시 skipProblemId 전달하면 즉시 제외 + Redis 저장 |
| DB skip 기록 | UserProblem에 skippedAt 저장 | 제거 (Redis만 사용) |

### 삭제 파일
- `SkipRequest.java`
- `SkipService.java`
- `ProblemController`의 skip 엔드포인트

### 수정 파일
- `RandomProblemRequest` — `skipProblemId` 필드 추가 (optional)
- `ProblemService` — skipProblemId 있으면 Redis 저장 후 해당 문제 제외
- `UserProblemRepository` — `findLastSkippedProblem` 쿼리 제거

## 2. Batch 배포

### 요구사항 요약
batch 모듈을 Docker Compose에 추가하고 GitHub Actions로 함께 빌드·배포한다.

### 변경 내용
| 항목 | 내용 |
|------|------|
| `Dockerfile.batch` 추가 | batch 전용 Dockerfile |
| `docker-compose.yml` | batch 서비스 추가 |
| `deploy.yml` | batch 이미지 빌드·푸시 추가 |
