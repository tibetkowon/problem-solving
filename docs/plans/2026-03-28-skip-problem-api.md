# 📋 구현 계획서: 문제 건너뛰기 API

## 요구사항 요약
사용자가 현재 문제를 풀지 않고 넘길 수 있는 건너뛰기 API를 구현한다. 건너뛴 문제는 DB에 기록되고 Redis에도 저장되어, 다음 랜덤 문제 조회 시 직전 건너뛴 문제가 제외된다.

## 구현 범위
- **포함**: 건너뛰기 API 엔드포인트, Service 로직, DTO
- **제외**: 건너뛰기 횟수 제한, 건너뛴 문제 이력 조회

## API 설계
| 메서드 | 경로 | 요청 | 응답 | 설명 |
|--------|------|------|------|------|
| POST | `/api/problems/skip` | `SkipRequest` | 204 No Content | 문제 건너뛰기 |

**SkipRequest**
```json
{
  "userId": 1,
  "problemId": 2,
  "chapterId": 1
}
```

## 작업 목록 (순서대로)
| 순서 | 작업 내용 | 대상 파일/클래스 |
|------|-----------|----------------|
| 1 | SkipRequest DTO 생성 | `api/.../problem/dto/SkipRequest.java` |
| 2 | SkipService 생성 (DB 저장 + Redis 저장) | `api/.../problem/service/SkipService.java` |
| 3 | ProblemController에 skip 엔드포인트 추가 | `ProblemController.java` |

## 고려사항 및 주의점
- `UserProblem`은 이미 `skippedAt` 필드가 있어 DB 저장 가능
- Redis 저장(`saveSkippedProblem`)도 이미 구현되어 있어 호출만 하면 됨
- 건너뛴 문제는 solved 목록에 포함되지 않으므로 다시 풀 수 있음
- 응답은 204 No Content

## 예상 커밋 단위
1. `feat: add skip problem API`
