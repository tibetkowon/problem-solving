# 📋 구현 계획서: Docker Compose + GitHub Actions CI/CD

## 요구사항 요약
`docker compose up -d` 한 번으로 앱(Spring Boot) + MySQL + Redis를 실행하고,
main 브랜치 push 시 GitHub Actions로 자동 빌드·테스트·배포한다.

## 구현 범위
- **포함**: `Dockerfile`, `docker-compose.yml`, `.env.example`, GitHub Actions 워크플로우
- **제외**: 모니터링, 로그 수집

## Docker Compose 구성
| 서비스 | 이미지 | 포트 |
|--------|--------|------|
| api | 직접 빌드 (Dockerfile) | 8080 |
| mysql | mysql:8.0 | 3306 |
| redis | redis:latest | 6379 |

## GitHub Actions 워크플로우
- **트리거**: main 브랜치 push
- **단계**: 빌드 → 테스트 → Docker 이미지 빌드 → 서버 배포 (SSH)

## 작업 목록
| 순서 | 작업 내용 | 파일 |
|------|-----------|------|
| 1 | Dockerfile 작성 | `Dockerfile` |
| 2 | docker-compose.yml 작성 | `docker-compose.yml` |
| 3 | .env.example 작성 | `.env.example` |
| 4 | GitHub Actions 워크플로우 작성 | `.github/workflows/deploy.yml` |
| 5 | .gitignore에 .env 추가 | `.gitignore` |

## 고려사항
- server 프로파일 사용 (DB_URL, DB_USERNAME 등 환경변수 주입)
- mysql 헬스체크 후 api 기동 (depends_on condition)
- GitHub Actions Secrets으로 민감 정보 관리
