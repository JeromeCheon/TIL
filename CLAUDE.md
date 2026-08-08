# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 저장소 개요

개인 학습 기록 저장소(TIL). 실습 코드는 기술 스택별 하위 디렉토리로 관리한다.

## springboot4-practice

Spring Boot 4.0.7 / Java 25 기반 실습 프로젝트. 빌드 도구는 Gradle(Groovy DSL).

### 주요 명령어

```bash
# 프로젝트 루트는 springboot4-practice/
cd springboot4-practice

# 빌드
./gradlew build

# 전체 테스트
./gradlew test

# 단일 클래스 테스트
./gradlew test --tests "me.jeromecheon.springboot4practice.JUnitCycleTest"

# 단일 메서드 테스트
./gradlew test --tests "me.jeromecheon.springboot4practice.JUnitCycleTest.test1"

# 애플리케이션 실행
./gradlew bootRun
```

### 아키텍처

```
TestController → TestService → MemberRepository (Spring Data JPA)
                                      ↓
                               Member (Entity, H2 in-memory)
```

- `data.sql`: 애플리케이션 실행 시 H2에 초기 데이터 삽입 (`defer-datasource-initialization: true`로 테이블 생성 후 실행)
- 테스트 환경은 `src/test/resources/application.yml`에서 `spring.sql.init.mode: never`로 `data.sql` 실행을 비활성화함 — 테스트마다 `@BeforeEach`에서 직접 데이터를 삽입하고 `@AfterEach`에서 `deleteAll()`로 정리하는 패턴 사용

### 테스트 패턴

**MockMvc 통합 테스트** (`@SpringBootTest` + `@AutoConfigureMockMvc`): 전체 Spring Context를 띄워 REST API를 테스트한다. `WebApplicationContext`로 `MockMvc`를 직접 초기화한다.

```java
@BeforeEach
public void mockMvcSetUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
}
```

**JPA 슬라이스 테스트** (`@DataJpaTest`): JPA 레이어만 로드해 Repository를 빠르게 테스트한다. `@Sql`로 각 테스트마다 픽스처를 주입하고 `@AfterEach`에서 `deleteAll()`로 정리한다. SQL 픽스처는 `src/test/resources/`에 위치한다.

```java
@DataJpaTest
class MemberRepositoryTest {
    @AfterEach
    public void cleanUp() { this.repository.deleteAll(); }

    @Sql("/insert-members.sql")
    @Test
    void getMemberById() { ... }
}
```

## 커스텀 커맨드 (.claude/commands/)

### /practice/skeletonize

Spring Boot 4 / Java 25 기반 프로젝트의 완성된 구현을 **git 이력 보존형 학습 스켈레톤**으로 변환한다.

```bash
# 기본 사용 (base = main)
/practice/skeletonize

# 특정 base 브랜치 지정
/practice/skeletonize feature/branch-name
```

**특징:**

- 커밋 없이 unstaged 변경만 유지 — 브랜치 이력 보존
- `git diff HEAD -- <파일>` 로 정답(완성 구현) 확인
- `git restore <파일>` 로 언제든 복구 가능

**변환 규칙:**

- Spring 스테레오타입 어노테이션 제거 (학습 목표) — `// TODO:` 가이드 주석으로 대체
- 필드·메서드 바디 제거 — 번호 매긴 로직 흐름 주석으로 대체
- 메서드 시그니처·메서드 레벨 어노테이션은 유지

---

## spring4-blog-project

Spring Boot 4.0.7 / Java 25 기반 블로그 API 실습 프로젝트. 빌드 도구는 Gradle(Groovy DSL).

**기능:** Article CRUD REST API + Thymeleaf 뷰 (목록/상세/생성/수정/삭제) + 댓글 작성/조회 + Spring Security 로그인/회원가입 + JWT 인증 (액세스/리프레시 토큰) + DTO 검증 + 전역 예외 처리

### 주요 명령어

```bash
# 프로젝트 루트는 spring4-blog-project/
cd spring4-blog-project

# 빌드
./gradlew build

# 전체 테스트
./gradlew test

# 애플리케이션 실행
./gradlew bootRun
```

### 아키텍처

```
BlogApiController  ┐
BlogViewController ┤→ BlogService      → BlogRepository (Spring Data JPA)     → Article (Entity)
UserApiController  ┤                            ↓                                 ↓
UserViewController ┤                   CommentRepository                    Comment (Entity)
TokenApiController ┘                            ↓
                                    GlobalExceptionHandler (@ControllerAdvice)
                   UserService      → UserRepository      → User (Entity)
                   TokenService     → RefreshTokenRepository → RefreshToken (Entity)
                   OAuth2UserCustomService (Google 사용자 upsert)
```

- `import.sql`: 애플리케이션 실행 시 H2에 초기 데이터 삽입 (Spring Boot 기본 지원)
- H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`)
- `WebOAuthSecurityConfig`: Spring Security 필터 체인 설정 — CSRF·세션 비활성화(Stateless), `/api/token` 허용, `/api/**` 인증 필요, 나머지 permitAll. H2 콘솔·정적 리소스는 Security 제외. Google OAuth2 로그인 설정 포함.
- `TokenAuthenticationFilter`: 매 요청마다 Authorization 헤더의 JWT를 검증하고 `SecurityContextHolder`에 인증 정보를 등록하는 `OncePerRequestFilter` 구현체.
- `OAuth2UserCustomService`: Google에서 받은 사용자 정보로 DB에 upsert 처리 (`DefaultOAuth2UserService` 확장).
- `OAuth2SuccessHandler`: OAuth2 로그인 성공 후 JWT 발급, refresh token 쿠키 설정, `/articles?token=...`으로 리다이렉트.
- `GlobalExceptionHandler`: `@ControllerAdvice` 기반 전역 예외 처리. `ErrorCode` 열거형과 `ErrorResponse` DTO로 API 전체에 일관된 에러 응답 포맷 제공.
- `Article`-`Comment` 1:N 관계: Article 삭제 시 연관된 Comment도 자동 삭제 (cascade REMOVE).

### API 엔드포인트

| Method | URL                  | 설명              |
| ------ | -------------------- | ----------------- |
| POST   | `/api/articles`      | 글 생성           |
| GET    | `/api/articles`      | 전체 글 목록 조회 |
| GET    | `/api/articles/{id}` | 단건 조회         |
| DELETE | `/api/articles/{id}` | 글 삭제           |
| PUT    | `/api/articles/{id}` | 글 수정           |
| POST   | `/api/comments`      | 댓글 작성         |

### 뷰 엔드포인트 (Thymeleaf)

| Method | URL              | 템플릿           | 설명                                                            |
| ------ | ---------------- | ---------------- | --------------------------------------------------------------- |
| GET    | `/articles`      | articleList.html | 글 목록 페이지                                                  |
| GET    | `/articles/{id}` | article.html     | 글 상세 페이지                                                  |
| GET    | `/new-article`   | newArticle.html  | 글 생성/수정 폼                                                 |
| GET    | `/login`         | oauthLogin.html  | Google 로그인 페이지 (인증된 사용자는 `/articles`로 리다이렉트) |
| GET    | `/signup`        | signup.html      | 회원가입 페이지                                                 |

### 인증 API 엔드포인트

| Method | URL                            | 설명                                                              |
| ------ | ------------------------------ | ----------------------------------------------------------------- |
| POST   | `/user`                        | 회원가입 (BCrypt 암호화 후 저장)                                  |
| GET    | `/oauth2/authorization/google` | Google OAuth2 인증 시작                                           |
| POST   | `/api/token`                   | 액세스 토큰 재발급 (리프레시 토큰으로 요청)                       |
| DELETE | `/api/refresh-token`           | JWT 로그아웃 — DB에서 refresh token 삭제 (access_token 인증 필요) |

- `static/js/token.js`: OAuth2 로그인 후 URL 파라미터의 access_token을 localStorage에 저장
- `static/js/article.js`: 글 CRUD fetch 처리. `httpRequest()` JWT 헬퍼(401 시 토큰 갱신 후 재시도) 및 로그아웃 핸들러 포함

### 테스트 패턴

**MockMvc 통합 테스트** (`BlogApiControllerTest`): `@SpringBootTest` + `@AutoConfigureMockMvc`로 전체 Context를 띄워 각 API를 검증한다. `@BeforeEach`에서 `blogRepository.deleteAll()`로 상태를 초기화한다.

```java
@BeforeEach
public void mockMvcSetup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    this.blogRepository.deleteAll();
}
```

---

## harness-engineering-with-claude

"하네스 엔지니어링 with 클로드 코드" 도서를 따라 AI 에이전트 팀 설계 및 하네스 엔지니어링 실전 학습.

**주제**: 하네스 엔지니어링 (Harness Engineering) · Claude Code · AI 에이전트 오케스트레이션 · 멀티에이전트 아키텍처

**학습 목표**:

- ATVO(권한·도구·검증·관측)를 설계하는 하네스 엔지니어링 방법론
- AI 에이전트를 단순 도구가 아닌 스스로 일하는 팀으로 설계·운영
- 메타스킬, 아키텍처 패턴, 에이전트 팀 구성 사례

**참고 도서**: 하네스 엔지니어링 with 클로드 코드 — 황민호 (로빈 황) 저

- 교보문고: https://ebook-product.kyobobook.co.kr/dig/epd/ebook/E000013110065
- 알라딘: https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=393763673

**콘텐츠 구조**:

- Part 1: 하네스 엔지니어링의 기초 개념
  - `part1/01-what-is-harness.md`: 하네스란 무엇인가 — 단일 에이전트 한계, 환경 설계의 중요성, 하네스의 정의 및 오해 해소
  - `part1/02-quick-start.md`: 30분 퀵스타트 가이드

- Part 2: 에이전트·스킬·오케스트레이터 설계
  - `part2/03-srp-for-agent-skill-orchestrator.md`: 에이전트·스킬·오케스트레이터의 책임 분리
  - `part2/04-a-thing-that-defines-agent.md`: 에이전트를 정의한다는 것 — 역할 계약서 관점
  - `part2/05-a-technique-how-to-design-skills.md`: 스킬 설계의 기술 — description 트리거링
  - `part2/06-orchestrator.md`: 오케스트레이터 — TeamCreate·TaskCreate·SendMessage

- Part 3: 메타 하네스 스킬과 팀 아키텍처
  - `.claude/skills/harness/SKILL.md`: 메타 하네스 스킬 — 하네스 구성을 체계화하는 6단계 파이프라인
  - `part3/07-meta-harness-skills.md`: 메타 하네스 스킬과 6단계 파이프라인 학습 노트
  - `part3/08-six-architecture-patterns.md`: 여섯 가지 아키텍처 패턴 — 파이프라인·팬아웃·전문가풀·생성-검증·감독자·계층적위임
  - `part3/09-team-subagent-hybrid.md`: 팀 / 서브에이전트 / 하이브리드 실행 모드 선택 기준
  - `part3/10-harness-registration-evolution.md`: 하네스 등록과 진화 — CLAUDE.md 포인터와 생명주기

- Part 4: 하네스 실전 예제
  - `part4/ch11-example-code-review-team/`: 코드 리뷰 자동화 하네스 — 4 에이전트(정적분석·설계검토·보안감사·리팩터링) + 1 스킬
  - `part4/ch12-full-stack-development-team/`: 풀스택 개발 팀 하네스 — 8 에이전트(기획·API설계·UI설계·DB마이그레이션·백엔드·프론트엔드·테스트·경계검증) 서브하네스
