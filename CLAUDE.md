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

MockMvc 테스트는 `@SpringBootTest` + `@AutoConfigureMockMvc`(Spring Boot WebMVC Test 모듈)를 사용하며, `WebApplicationContext`로 `MockMvc`를 직접 초기화한다.

```java
@BeforeEach
public void mockMvcSetUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
}
```
