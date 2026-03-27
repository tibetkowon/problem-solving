---
name: verify-implementation
description: >
  어떤 언어/스택이든 코드를 작성하거나 수정한 후 커밋·완료 선언 전에 자동으로 빌드·테스트·린트를 검증하는 스킬.
  사용자의 별도 언급 없이 다음 상황에서 즉시 자동 적용한다:
  - 어떤 언어든 코드를 작성하거나 수정한 직후 (Java, Go, Python, TypeScript, Kotlin 등 무관)
  - git commit 또는 "완료했습니다" 선언 직전
  - 빌드 오류나 테스트 실패가 의심될 때
  검증 없이 작업 완료를 선언하는 것은 허용하지 않는다.
---

# 구현 검증 스킬 (커밋 전 자동 실행)

## 언제 실행하는가

코드를 변경한 직후, 커밋이나 작업 완료 선언 **직전**에 반드시 실행한다.
언어나 프레임워크에 관계없이 모든 프로젝트에 적용한다.

---

## 1단계: 스택 자동 감지

프로젝트 루트의 파일을 확인해 사용 중인 스택을 파악한다.

| 감지 파일 | 스택 |
|-----------|------|
| `build.gradle` / `build.gradle.kts` | Java / Kotlin (Gradle) |
| `pom.xml` | Java / Kotlin (Maven) |
| `go.mod` | Go |
| `package.json` | Node.js / React / Vue / Next.js |
| `requirements.txt` / `pyproject.toml` / `setup.py` | Python |
| `Cargo.toml` | Rust |
| `pubspec.yaml` | Flutter / Dart |

여러 스택이 혼재하면(예: Spring + React 모노레포) 변경된 파일이 속한 스택을 모두 검증한다.

---

## 2단계: 스택별 검증 명령

### Java / Kotlin — Gradle

```bash
./gradlew build -x test          # 빌드 확인
./gradlew test                   # 테스트 실행 (테스트 파일 있을 때)
```

로컬 환경이면 `-Dspring.profiles.active=local` 옵션을 추가해 H2 DB를 사용한다.

### Java / Kotlin — Maven

```bash
./mvnw compile                   # 빌드 확인
./mvnw test                      # 테스트 실행
```

### Go

```bash
go fmt ./...                     # 포맷 정리
go build ./...                   # 빌드 확인
go test ./...                    # 테스트 실행 (테스트 파일 있을 때)
```

### Node.js / React / Vue / Next.js

```bash
npm run lint   # 또는 pnpm lint  # 린트 검사
npm run build  # 또는 pnpm build # 빌드 확인
npm test       # 테스트 파일 있을 때
```

### Python

```bash
python -m py_compile **/*.py     # 문법 오류 확인
pytest                           # 테스트 파일 있을 때 (pytest 설치된 경우)
```

### Rust

```bash
cargo build                      # 빌드 확인
cargo test                       # 테스트 실행
```

### Flutter / Dart

```bash
flutter analyze                  # 정적 분석
flutter test                     # 테스트 실행 (테스트 파일 있을 때)
```

---

## 3단계: 자동 수정 루프

어느 단계에서든 실패하면:
1. 에러 메시지를 읽고 원인을 파악한다
2. 코드를 수정한다
3. 실패한 단계를 다시 실행한다
4. 모든 검증이 통과할 때까지 반복한다

수정이 어려운 경우에는 사용자에게 에러 내용과 함께 도움을 요청한다.

---

## 4단계: 완료 보고

모든 검증이 통과하면 간결하게 보고한다:

```
✅ 코드 검증 완료 ([감지된 스택])
- 빌드: 성공
- 테스트: N개 통과 (테스트 없으면 생략)
```