# 단원별 문제 풀이' 및 '풀이 이력 조회' API 구축

## 스킬 (자동 적용 규칙)
이 프로젝트에는 `.claude/skills/` 에 아래 스킬이 있다. 작업 시 반드시 해당 스킬의 지침을 따른다.

| 스킬 파일 | 적용 시점 |
|-----------|----------|
| `implementation-plan.md` | 기능 구현 요청 시 → 계획서 작성 후 사용자 확인, `/docs/plans/`에 저장 |
| `requirement-history.md` | 기능/개선/버그 요청 시 → `/docs/requirements.md` 기록 |
| `spring-test.md` | 코드 구현 후 → 단위/통합 테스트 작성 |
| `verify_implementation.md` | 커밋 전 → 빌드·테스트 검증 |
| `unit-commit.md` | 작업 단위 완료 시 → git commit (Entity, Service, Controller, 테스트 각각) |
| `architecture-docs.md` | Entity·서비스 구조 변경 시 → `/docs/architecture.md` 업데이트 |
| `update-readme.md` | 모든 작업 완료 후 → README.md 업데이트 |

각 스킬 파일의 전체 지침은 `.claude/skills/<파일명>`을 읽어 확인한다.

## 기술스택
1. java 21
2. spring
3. jpa
4. mysql - server profile
5. h2 - local profile
6. redis (회원 최근 조회 문제 확인 및 정답률 관리)
#### 아키텍처
	- 멀티 모듈 아키텍처를 활용한 설계
	- DDD 원칙 준수
#### 인프라 및 환경 
	- Docker Compose 를 이용한 애플리케이션, DB, Redis 구동
	- Github Actions 를 이용한 CI/CD 자동화 -> main branch push 시 동작
#### 테스트 및 문서화
	- Swagger
	- README.md
#### 프로파일
	- local - local 환경 실행용으로, h2 DB를 사용하며, 테스트용 더미데이터 초기화 필요
	- server - 배포용으로, github Actions 를 통해 Docker Compose 를 통해 실제 배포에서 사용
