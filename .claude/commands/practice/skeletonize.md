---
allowed-tools: Bash, Read, Write, Edit, AskUserQuestion
description: >
  현재 브랜치의 완성 구현을 git 이력 보존형 학습 스켈레톤으로 변환한다.
  커밋 없이 unstaged 변경만 남겨 git diff HEAD를 정답지로 활용할 수 있다.
  Spring Boot 4 / Java 25 / JS 대상. 인수 없으면 base 브랜치는 main.
---

## Context

- 현재 브랜치: !`git branch --show-current`
- Base 브랜치 대비 변경 파일: !`git diff $ARGUMENTS...HEAD --name-status`
- 현재 unstaged 변경 없음 확인: !`git status --short`

## 핵심 원칙

이 커맨드는 **git 이력 보존형** 스켈레톤을 만든다.

- **브랜치 유지**: 현재 브랜치에서 그대로 작업한다 — 새 브랜치 생성 없음
- **커밋 없음**: 모든 변경은 unstaged 상태로 유지한다
- **정답지**: `git diff HEAD -- <파일>` 로 커밋된 완성 구현과 비교
- **초기화**: `git restore <파일>` 로 완성 구현 복원, `git restore .` 로 전체 초기화

---

## Phase 1 — 변경 파일 분류

`git diff $ARGUMENTS...HEAD --name-status` 결과를 읽고 각 파일을 분류한다.

### 제외 (처리하지 않음)

- `*Test.java` — 테스트 코드 (검증 기준으로 유지)
- `*.gradle`, `*.yaml`, `*.yml` — 빌드/설정 파일
- `*.html` — Thymeleaf 템플릿
- `import.sql`, `data.sql` — 초기 데이터
- `GUIDE.md`, `CLAUDE.md` — 가이드 문서

### SKELETON 대상 (변환)

각 파일을 Read해서 내용을 확인한 뒤 아래 기준으로 SKELETON 여부를 판단한다:

| 파일 유형            | Spring Boot 판별 기준                                                                                                |
| -------------------- | -------------------------------------------------------------------------------------------------------------------- |
| Java — 서비스        | `@Service` 클래스, `@Transactional` 메서드 포함                                                                      |
| Java — 컨트롤러      | `@RestController` / `@Controller` 클래스                                                                             |
| Java — 필터          | `OncePerRequestFilter` 구현체                                                                                        |
| Java — 보안 설정     | `@Configuration` + `SecurityFilterChain` `@Bean`                                                                     |
| Java — OAuth2        | `DefaultOAuth2UserService` 확장, `SimpleUrlAuthenticationSuccessHandler` 확장, `AuthorizationRequestRepository` 구현 |
| Java — 엔티티        | `@Entity` 클래스, 상태 변경 메서드(`update()` 등) 포함                                                               |
| Java — Repository    | `JpaRepository` 확장 인터페이스                                                                                      |
| Java — 설정 프로퍼티 | `@ConfigurationProperties` 클래스                                                                                    |
| Java — 유틸          | `static` 메서드만 있는 유틸 클래스                                                                                   |
| JS                   | fetch 헬퍼, 이벤트 핸들러, localStorage 조작                                                                         |

분류 완료 후 AskUserQuestion으로 파일 목록과 분류 결과를 보여주고 확인을 받는다.

---

## Phase 2 — 스켈레톤 변환 규칙

확인을 받은 뒤 각 파일에 아래 규칙을 적용해 Write한다.

### Java 공통 규칙

**제거 항목:**

- 클래스 선언의 Spring 스테레오타입/Lombok 어노테이션: `@Service`, `@Component`, `@Controller`, `@RestController`, `@Configuration`, `@RequiredArgsConstructor`, `@NoArgsConstructor`, `@Getter`, `@Setter`, `@Transactional` (클래스 레벨)
- 해당 어노테이션에 대응하는 import
- `extends ClassName` / `implements InterfaceName` 선언 (인터페이스·부모 클래스가 학습 목표인 경우)
- `private final` 필드 선언 (static 상수 제외)
- 모든 메서드 바디

**유지 항목:**

- `package` 선언
- 메서드 시그니처에서 참조되는 타입의 import
- `@Bean`, `@GetMapping`, `@PostMapping`, `@DeleteMapping`, `@PutMapping`, `@Override`, `@RequestBody`, `@Transactional` (메서드 레벨) 등 메서드 레벨 어노테이션
- `private static final` 상수
- 메서드 시그니처 자체

**교체 항목:**

- 비-void 메서드 바디 → 번호 매긴 로직 흐름 주석 + `return null;` (boolean은 `return false;`)
- void 메서드 바디 → 번호 매긴 로직 흐름 주석 + `// TODO(human)`

**추가 항목:**

- 클래스 선언 위: 제거한 어노테이션의 역할을 설명하는 `// TODO:` 가이드 주석
- 제거한 필드 자리: 필요한 의존성을 설명하는 `// TODO:` 가이드 주석

**로직 흐름 주석 형식:**

```java
public String createNewAccessToken(String refreshToken) {
  // 1. tokenProvider로 refreshToken의 유효성을 검증한다 — 실패하면 IllegalArgumentException 발생
  // 2. refreshToken으로 RefreshToken 엔티티를 조회해 userId를 얻는다
  // 3. userId로 User 엔티티를 조회한다
  // 4. User로 만료 2시간짜리 accessToken을 생성해 반환한다
  return null;
}
```

### Java — Repository 인터페이스

- `extends JpaRepository<E, ID>` 선언 제거 → `// TODO:` 가이드 주석으로 대체
- 파생 쿼리 메서드 선언 전체 제거 → `// TODO:` 주석으로 대체

### Java — @Entity 클래스

- 클래스 어노테이션 (`@Entity`, `@NoArgsConstructor`, `@Getter`) 제거
- 필드 어노테이션 (`@Id`, `@GeneratedValue`, `@Column`) 포함 필드 전체 제거
- 생성자 바디 제거 (시그니처 유지)
- 상태 변경 메서드 바디 → 번호 주석 + `return null;` / `// TODO(human)`

### Java — @ConfigurationProperties

- 클래스 어노테이션 (`@Setter`, `@Getter`, `@Component`, `@ConfigurationProperties(...)`) 제거
- 필드 선언은 **유지** (바인딩 대상이므로)

### JS 규칙

- 함수 바디 전체 제거 → `// TODO(human)` 한 줄
- 비-void 함수 끝에 `return null;` 추가
- 이벤트 핸들러 콜백 내부 → `// TODO:` 로직 설명 주석으로 교체
- 함수 선언 위에 `// TODO:` 로 기능 설명 주석 추가

---

## Phase 3 — 진행 상황 확인 안내

변환 완료 후 다음 명령어를 안내한다:

```bash
# 스켈레톤화된 파일 목록과 변경량 확인
git diff --stat HEAD

# 특정 파일 정답 확인
git diff HEAD -- <파일 경로>

# 특정 파일 완성 구현으로 초기화
git restore <파일 경로>

# 전체 초기화 (모든 스켈레톤 → 완성 구현 복원)
git restore .
```

권장 구현 순서도 함께 출력한다 (Spring 의존성 방향 기준):

```
@ConfigurationProperties → 정적 유틸 → @Entity → Repository
→ @Service → Filter / @Configuration → @Controller / @RestController → JS
```
