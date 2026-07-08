# Spring Boot 4 블로그 프로젝트

Spring Boot 4.0.7 / Java 25 기반의 블로그 API 실습 프로젝트.  
**JWT + Google OAuth2 인증**, Thymeleaf 뷰, Article CRUD를 직접 구현하며 Spring Security의 Stateless 인증 흐름을 익히는 것이 목표임.

---

## 기술 스택

| 분류          | 기술                                      |
| ------------- | ----------------------------------------- |
| 언어 / 런타임 | Java 25                                   |
| 프레임워크    | Spring Boot 4.0.7                         |
| 보안          | Spring Security 6, OAuth2 Client (Google) |
| 인증 토큰     | jjwt 0.9.1 (HS256)                        |
| ORM / DB      | Spring Data JPA, H2 (인메모리)            |
| 뷰 템플릿     | Thymeleaf 3                               |
| 빌드 도구     | Gradle 9.5.1 (Groovy DSL)                 |
| 코드 생성     | Lombok                                    |

---

## 아키텍처

```
BlogApiController  ─┐
BlogViewController ─┤──▶ BlogService      ──▶ BlogRepository     ──▶ Article (Entity)
UserApiController  ─┤         └─ authorizeArticleAuthor()  (SecurityContext 기반 작성자 검증)
UserViewController ─┤
TokenApiController ─┘

                         UserService      ──▶ UserRepository      ──▶ User (Entity)
                         TokenService     ──▶ RefreshTokenService ──▶ RefreshToken (Entity)
                         OAuth2UserCustomService  (Google 사용자 upsert)

매 요청: TokenAuthenticationFilter (OncePerRequestFilter)
         └─ Authorization 헤더 JWT 검증 → SecurityContextHolder 등록
```

---

## 인증 플로우

```
1. /login 페이지 접근
   └─ refresh_token 쿠키가 유효하면 → /articles 자동 리다이렉트

2. Google 로그인 버튼 클릭
   └─ /oauth2/authorization/google → Google 인증 서버

3. OAuth2SuccessHandler (인증 성공 후)
   ├─ RefreshToken 생성 (14일) → DB 저장 + 쿠키 설정
   └─ AccessToken 생성 (1일) → URL 파라미터로 전달
       예: /articles?token=eyJhbGci...

4. token.js
   └─ URL의 ?token= 파라미터 → localStorage("access_token") 저장

5. API 요청 시 (article.js)
   └─ httpRequest() 헬퍼가 Authorization: Bearer <token> 헤더 첨부
       └─ 401 응답 시 → POST /api/token 으로 자동 재발급 후 재시도

6. 로그아웃
   └─ DELETE /api/refresh-token → DB에서 RefreshToken 삭제
      + localStorage / refresh_token 쿠키 정리
```

---

## API 엔드포인트

### 블로그 API

| Method | URL                  | 설명               | 인증   |
| ------ | -------------------- | ------------------ | ------ |
| POST   | `/api/articles`      | 글 작성            | 필요   |
| GET    | `/api/articles`      | 전체 글 목록       | 불필요 |
| GET    | `/api/articles/{id}` | 단건 조회          | 불필요 |
| DELETE | `/api/articles/{id}` | 글 삭제 (작성자만) | 필요   |
| PUT    | `/api/articles/{id}` | 글 수정 (작성자만) | 필요   |

### 인증 API

| Method | URL                            | 설명                                   |
| ------ | ------------------------------ | -------------------------------------- |
| POST   | `/user`                        | 이메일 회원가입                        |
| GET    | `/oauth2/authorization/google` | Google OAuth2 인증 시작                |
| POST   | `/api/token`                   | AccessToken 재발급 (RefreshToken 필요) |
| DELETE | `/api/refresh-token`           | JWT 로그아웃                           |

### 뷰 엔드포인트 (Thymeleaf)

| Method | URL              | 템플릿             | 설명            |
| ------ | ---------------- | ------------------ | --------------- |
| GET    | `/articles`      | `articleList.html` | 글 목록         |
| GET    | `/articles/{id}` | `article.html`     | 글 상세         |
| GET    | `/new-article`   | `newArticle.html`  | 글 작성/수정 폼 |
| GET    | `/login`         | `oauthLogin.html`  | Google 로그인   |
| GET    | `/signup`        | `signup.html`      | 회원가입        |

---

## IntelliJ 개발 환경 설정

### 사전 요구사항

- **Java 25** (Amazon Corretto 25 권장)
- **IntelliJ IDEA 2024.x 이상**

### 1. Lombok 설정

IntelliJ에서 Lombok을 사용하려면 아래 두 가지를 반드시 설정해야 함.

1. `Settings` → `Plugins` → `Lombok` 설치
2. `Settings` → `Build, Execution, Deployment` → `Compiler` → `Annotation Processors`  
   → **"Enable annotation processing"** 체크

### 2. 환경변수 설정 (.env)

프로젝트 루트의 `.env` 파일에 아래 값을 채워넣음.  
(`.gitignore`에 포함되어 있으므로 커밋되지 않음.)

```dotenv
GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-client-secret
JWT_ISSUER=your-issuer-name
JWT_SECRET_KEY=your-base64-secret-key
```

> **Google Cloud Console 설정**  
> OAuth 2.0 클라이언트 ID 생성 시 승인된 리다이렉션 URI에 아래를 추가해야 함.  
> `http://localhost:8080/login/oauth2/code/google`

#### IntelliJ에서 .env 주입하기

IntelliJ는 `.env` 파일을 자동으로 읽지 않으므로 아래 방법 중 하나를 사용해야 함.

**방법 A — EnvFile 플러그인 (권장)**

1. `Settings` → `Plugins` → **"EnvFile"** 플러그인 설치
2. `Run/Debug Configurations` → 실행 설정 선택 → `EnvFile` 탭 → `.env` 파일 경로 추가

**방법 B — 환경변수 직접 입력**

1. `Run/Debug Configurations` → 실행 설정 선택
2. `Environment variables` 항목에 `.env` 내용을 직접 붙여넣기

### 3. 실행 방법 (IntelliJ)

- **Gradle 탭** → `:bootRun` 더블클릭
- 또는 `Spring4BlogProjectApplication.java` 파일을 열고 메인 메서드 옆 ▶ 클릭

앱이 뜨면 아래 주소로 확인할 수 있음.

| 목적          | URL                              |
| ------------- | -------------------------------- |
| 블로그 앱     | http://localhost:8080/articles   |
| Google 로그인 | http://localhost:8080/login      |
| H2 콘솔       | http://localhost:8080/h2-console |

> H2 콘솔 접속 정보: JDBC URL `jdbc:h2:mem:testdb`, Username `sa`, Password 없음

---

## 주요 Gradle 명령어

```bash
# 프로젝트 루트에서 실행
cd spring4-blog-project

# 빌드
./gradlew build

# 전체 테스트
./gradlew test

# 애플리케이션 실행
./gradlew bootRun
```

---

## 작업 진행도

### 기본 기능

- [x] Article CRUD REST API (`POST` / `GET` / `DELETE` / `PUT`)
- [x] Thymeleaf 뷰 (목록, 상세, 작성/수정 폼)
- [x] H2 인메모리 DB 및 `import.sql` 초기 데이터 (글 3건)

### 인증 · 보안

- [x] Spring Security 설정 — Stateless, CSRF·세션 비활성화 (`WebOAuthSecurityConfig`)
- [x] JWT 토큰 생성/검증 (`TokenProvider`, HS256)
- [x] `TokenAuthenticationFilter` — 매 요청 JWT 검증 후 SecurityContext 등록
- [x] RefreshToken 엔티티 및 DB 저장소
- [x] AccessToken 재발급 API (`POST /api/token`)
- [x] Google OAuth2 의존성 추가 및 `CookieUtil` 구현
- [x] OAuth2 Authorization Request 쿠키 기반 저장소
- [x] OAuth2 로그인 성공 핸들러 — JWT 발급 + `/articles` 리다이렉트
- [x] `OAuth2UserCustomService` — Google 사용자 정보 DB upsert
- [x] JWT 로그아웃 API (`DELETE /api/refresh-token`)
- [x] 인증된 사용자의 `/login` 접근 시 자동 리다이렉트

### 프론트엔드 (JS)

- [x] `token.js` — URL 파라미터에서 AccessToken 추출 → `localStorage` 저장
- [x] `article.js` — CRUD 버튼 이벤트 + 401 시 자동 토큰 갱신 로직 + 로그아웃

### 작성자 기반 인가

- [x] `Article`에 `author` 필드 추가 (OAuth2 로그인 이메일로 저장)
- [x] `User`에 `nickname` 필드 추가 (Google 프로필 이름 저장)
- [x] `BlogService.authorizeArticleAuthor()` — 작성자 불일치 시 예외 발생

### 버그 수정

- [x] `CookieUtil.deleteCookie()` null guard 반전 버그 수정
- [x] Google OAuth2 scope 오류 수정 (`profile`, `email`로 제한)
- [x] 로그아웃 버튼 `onclick` 속성 충돌 버그 수정

---

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 클래스만 테스트
./gradlew test --tests "me.jeromecheon.spring4blogproject.controller.BlogApiControllerTest"
```

| 테스트 클래스            | 설명                                   |
| ------------------------ | -------------------------------------- |
| `BlogApiControllerTest`  | Article CRUD API 통합 테스트 (MockMvc) |
| `TokenApiControllerTest` | AccessToken 재발급 API 통합 테스트     |
| `TokenProviderTest`      | JWT 생성/검증/파싱 단위 테스트         |
