# 단원별 문제 풀이' 및 '풀이 이력 조회' API 구축

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
