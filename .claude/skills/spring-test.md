---
name: spring-test
description: >
  Spring Boot 프로젝트에서 기능을 구현할 때 반드시 자동으로 실행되는 TDD 테스트 스킬.
  다음 상황에서 사용자가 명시적으로 요청하지 않아도 즉시 이 스킬을 적용한다:
  - Spring Boot 프로젝트에서 새 기능(Service/Controller/Repository/Entity 등)을 구현할 때
  - 기존 Spring 코드를 수정하거나 버그를 수정할 때
  - 모든 기능 구현이 완료되어 전체 검증이 필요할 때
  테스트 없이 기능만 구현하거나, 테스트를 마지막에 몰아서 작성하는 방식은 허용하지 않는다.
---

# Spring Boot 테스트 스킬 (자동 적용)

## Trigger — 언제 이 스킬을 실행하는가

다음 중 하나라도 해당되면 사용자의 별도 언급 없이 **즉시 이 스킬을 적용**한다:

- Spring Boot 프로젝트에서 Service/Controller/Repository/Component 클래스를 새로 만들거나 수정할 때
- 비즈니스 로직, API 엔드포인트, 데이터 접근 코드를 구현할 때
- 기능 구현 중 "이제 다 됐다", "완성했다"고 하기 전
- "통합 테스트 해줘", "전체 검증해줘", "배포 전 확인해줘" 라고 할 때

**단위 테스트 없이 기능만 구현하고 완료 선언하는 것은 절대 허용하지 않는다.**

---

## 모드 선택

현재 상황에 맞는 모드를 자동으로 판단한다:

- **단위 테스트 모드**: 특정 기능/컴포넌트를 구현 중 → 해당 기능의 단위 테스트 작성 후 통과해야 완료
- **통합 테스트 모드**: 여러 기능이 모두 완성되어 전체 흐름 검증 필요 → 통합 테스트 작성 후 통과해야 완료

---

## 단위 테스트 모드

### 적용 시점
기능을 구현할 때마다 — 구현 직후 또는 구현과 동시에 단위 테스트를 작성한다.
테스트가 통과할 때까지 다음 기능으로 넘어가지 않는다.

### 단계

**1. 테스트 대상 파악**
- 구현한 클래스/메서드의 핵심 로직과 엣지 케이스 목록화
- 테스트 파일 경로: `src/test/java/` 하위 동일 패키지

**2. 단위 테스트 작성**

테스트 기본 구조 (JUnit 5 + Mockito):
```java
@ExtendWith(MockitoExtension.class)
class XxxServiceTest {

    @Mock
    private XxxRepository xxxRepository;

    @InjectMocks
    private XxxService xxxService;

    @Test
    @DisplayName("유효한 데이터로 xxx 생성 시 성공적으로 저장된다")
    void xxx_생성_유효한데이터_성공() {
        // Given (준비)

        // When (실행)

        // Then (검증)
    }
}
```

**테스트 메서드명 규칙**: 반드시 한글로 작성한다.
- 메서드명: `기능_상황_결과()` 형식 (예: `사용자_생성_이메일중복_예외발생()`)
- `@DisplayName`: 자연스러운 한글 문장 (예: `"중복된 이메일로 가입 시 DuplicateEmailException이 발생한다"`)

어노테이션 선택 기준:
- Service 순수 단위 테스트 → `@ExtendWith(MockitoExtension.class)`
- Controller 레이어 테스트 → `@WebMvcTest(XxxController.class)`
- Repository 테스트 → `@DataJpaTest`

**3. 테스트 실행**
```bash
# Maven
./mvnw test -Dtest=XxxServiceTest

# Gradle
./gradlew test --tests "패키지.XxxServiceTest"
```

**4. 결과 처리**

테스트 통과 ✅ → "단위 테스트 통과. 다음 기능 개발로 진행합니다."

테스트 실패 ❌ → 반드시 수정:
1. 실패 메시지 분석 → 테스트 오류인지 구현 오류인지 판단
2. 구현 코드 또는 테스트 수정
3. 3단계(테스트 실행)로 돌아가 재실행
4. 통과할 때까지 반복 — 실패 상태로 진행 불가

---

## 통합 테스트 모드

### 적용 시점
여러 기능의 개발이 완료되어 전체 흐름(Controller → Service → Repository → DB)을 검증할 때.

### 단계

**1. 시나리오 설계**
- Happy Path (정상 흐름)
- 주요 실패/예외 흐름
- 여러 기능이 연계되는 복합 시나리오

**2. 통합 테스트 작성**

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional  // 각 테스트 후 DB 롤백
class XxxIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("전체 흐름 통합 테스트 - 생성 후 조회까지 정상 동작한다")
    void 전체흐름_생성후조회_정상동작() throws Exception {
        // Given → When → Then
    }
}
```

**3. 통합 테스트 실행**
```bash
# Maven
./mvnw test -Dtest="*IntegrationTest"

# Gradle
./gradlew test --tests "*IntegrationTest"
```

**4. 결과 처리**

통합 테스트 통과 ✅ → "통합 테스트 통과. 기능 개발 검증 완료."

통합 테스트 실패 ❌ → 반드시 수정:
1. 스택 트레이스로 실패 레이어 파악 (Controller/Service/Repository/설정)
2. 해당 레이어 코드 수정
3. 3단계(테스트 실행)로 돌아가 재실행
4. 통과할 때까지 반복 — 실패 상태로 완료 선언 불가

---

## 공통 원칙

- 빌드 도구 자동 감지: `mvnw`/`pom.xml` → Maven, `gradlew`/`build.gradle` → Gradle
- 테스트는 독립적으로 실행 가능해야 한다 (실행 순서 의존 금지)
- 외부 서비스 의존성은 `@MockBean`으로 격리한다
- 통합 테스트는 `@Transactional` 또는 `@BeforeEach` + `deleteAll()`로 격리한다

### 테스트 의존성 확인
프로젝트에 다음이 포함되어 있는지 확인한다:
```xml
<!-- Maven -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```
없으면 추가를 제안한다.
