---
name: pr-create
description: 현재 브랜치와 target 브랜치를 비교해 문서 신선도를 점검한 뒤, PR 제목/본문을 생성-검증 서브에이전트 쌍으로 작성하고, 최종 확인 후 `gh pr create`로 생성한다. 'PR 만들어줘', 'PR 생성', 'pull request 만들어줘', 'PR 다시 만들어줘' 같은 요청 시 반드시 사용. 단, 이미 만들어진 PR을 리뷰해달라는 요청은 대상이 아니다.
allowed-tools: Bash, Read, Write, AskUserQuestion
---

# PR Create Orchestrator

현재 브랜치를 바탕으로 PR을 생성한다. Author와 reviewer 서브에이전트가 협력해 문서 신선도 점검 + PR 제목/본문 생성-검증을 거친 뒤, 최종 확인을 통해 `gh pr create`를 실행한다. Part 1의 패턴을 따르는 스킬.

## Workflow

### Phase 0: Precondition 체크

1. 현재 브랜치 확인: `git branch --show-current`
2. Target 브랜치 확인:
   - 사용자 요청에서 추출 (예: "develop으로 PR" → target = develop)
   - 없으면 기본값 = `main`
3. Target 대비 커밋 확인: `git log <target>..HEAD --oneline`
   - 비어있으면 → "target 대비 커밋이 없습니다. 먼저 커밋을 만들어주세요." 안내 후 종료
   - 있으면 → Phase 1로 진행

### Phase 1: Author 호출

`pr-author` 에이전트를 호출하여 PR 제목/본문 초안 + 문서 신선도 보고를 작성한다.

```
Agent(
  subagent_type: general-purpose,
  model: "sonnet",
  description: "pr-author 에이전트 호출",
  prompt: """
당신은 pr-author입니다. 다음을 실행하세요:
1. 현재 브랜치와 target 브랜치를 파악합니다.
   - 현재: `git branch --show-current`
   - Target: {오케스트레이터가 제공}

2. `git log <target>..HEAD --oneline`로 커밋 로그를 읽습니다.

3. `git diff <target>...HEAD --stat` + full diff를 확인합니다.

4. `.github/pull_request_template.md`가 있으면 읽습니다.

5. 문서 신선도를 점검합니다:
   - 변경된 파일 경로를 기준으로 관련 CLAUDE.md / README.md를 식별합니다.
   - 새로 추가된 환경 변수, 의존성, 라우트, 디렉터리가 해당 문서에 반영되지 않았는지 확인합니다.
   - 발견사항을 명확히 기록합니다 (각 카테고리별).

6. PR 제목을 작성합니다:
   - 영어 / 72자 이하 / 명령형 현재시제
   - Scope 미사용 (예: "feat: Add", 절대 "feat(scope): Add" 금지)

7. PR 본문을 작성합니다:
   - 템플릿의 체크박스는 실제 근거가 있을 때만 체크합니다.
   - 한국어로 변경 이유(why)를 서술합니다.

8. 모든 결과를 `_workspace/pr-draft.md`에 저장합니다.
   형식: Doc Staleness 섹션 + Title + Body

[기존 피드백이 있으면 여기에 포함됨]
""",
  run_in_background: false
)
```

산출물: `_workspace/pr-draft.md`

### Phase 2: Reviewer 호출

`pr-reviewer` 에이전트를 호출하여 초안을 검증한다.

```
Agent(
  subagent_type: general-purpose,
  model: "sonnet",
  description: "pr-reviewer 에이전트 호출",
  prompt: """
당신은 pr-reviewer입니다. 다음을 실행하세요:
1. `_workspace/pr-draft.md`를 읽습니다.

2. `git diff <target>...HEAD`(원본 diff)를 확인합니다.

3. 각 섹션을 평가합니다:

   **Format:** 제목 길이(72자 이하), 영어/명령형, scope 미사용 확인

   **Fact:** 제목/본문이 실제 diff와 일치하는가, 추측/누락 없는가

   **Checkboxes:** 체크된 항목이 diff에서 실제로 근거되는가 (예: 테스트 체크 시 test 파일 변경 확인)

   **Doc Staleness:** author의 발견이 정확한가. 다시 한 번 diff 경로와 CLAUDE.md/README.md를 대조하여 검증합니다.

4. PASS 또는 REDO 판정을 `_workspace/pr-review-report.md`에 기록합니다:
   - PASS: 모든 검증 완료
   - REDO: 각 카테고리별 구체적 사유 + author가 바로 적용할 수정 지시

5. 객관적 기준만 사용합니다. 판정 불확실 시 REDO를 택합니다.

[기존 피드백이 있으면 여기에 포함됨]
""",
  run_in_background: false
)
```

산출물: `_workspace/pr-review-report.md`

### Phase 3: 판정 분기

`_workspace/pr-review-report.md`를 읽어 판정을 확인한다.

- **PASS** → Phase 4로 진행
- **REDO** (1~2회차) → Phase 3-1로 이동
- **REDO** (3회차 이상) → Phase 3-2로 이동

#### Phase 3-1: Author 재호출 (최대 2회)

Reviewer의 수정 지시를 프롬프트에 포함해 `pr-author`를 재호출한다. 초안 중 PASS된 섹션은 보존하고, REDO 섹션만 재작성하도록 지시.

```
Agent(
  subagent_type: general-purpose,
  model: "sonnet",
  description: "pr-author 재호출 (REDO 수정)",
  prompt: """
당신은 pr-author입니다. 다음을 실행하세요:
1. `_workspace/pr-draft.md` 현재 버전을 읽습니다.
2. 다음 리뷰어 피드백을 반영합니다:
   [reviewer의 수정 지시 전문]
3. 피드백이 있는 섹션만 재작성합니다. 이미 PASS된 섹션은 그대로 둡니다.
4. 수정된 초안을 `_workspace/pr-draft.md`에 덮어쓰고 저장합니다.

기한: 30초 이내 완료
""",
  run_in_background: false
)
```

그 후 Phase 2(reviewer 재호출)로 돌아간다. 루프 상한은 **2회 재호출**(author 초호출 + 2회 재호출 = 총 3회).

#### Phase 3-2: 상한 도달 (3회 이상)

경고와 함께 사용자 확인:

```
⚠️ 자동 승인 한계 도달 — 검증이 3회 이상 반복되었습니다.
마지막 초안을 그대로 진행할지, 수정할지 결정해주세요.
(수정: "고침", "다시", 또는 구체적 지시 입력 → 수동 피드백으로 author 재호출)
```

사용자 입력을 받아 진행 여부 판단 → Phase 4로 진행하거나 프로세스 중단.

### Phase 4: 문서 신선도 게이트

PASS 후 `_workspace/pr-draft.md`의 Doc Staleness 섹션을 확인한다.

- **"No stale documents found."** → Phase 5로 진행
- **발견 항목 있음** → 사용자에게 `AskUserQuestion`으로 확인:

  ```
  ⚠️ 문서 미갱신 발견:
  [발견된 항목 나열]

  다음 중 선택하세요:
  - "업데이트 후 다시" — CLAUDE.md/README.md 업데이트 후 재실행
  - "무시하고 진행" — 문서 미갱신을 허용하고 PR 생성 진행
  ```

  - 업데이트 후 다시 → Phase 0부터 재시작 (다시 author/reviewer 호출)
  - 무시하고 진행 → Phase 5로 진행

### Phase 5: 최종 확인

`_workspace/pr-draft.md`의 최종 제목/본문을 사용자에게 제시하고, 진행/취소/수정 여부를 `AskUserQuestion`으로 확인한다.

```
AskUserQuestion(
  questions: [
    {
      question: "다음 PR을 생성할까요?",
      header: "PR Preview",
      multiSelect: false,
      options: [
        {
          label: "진행 (Proceed)",
          description: "제안된 PR을 생성합니다."
        },
        {
          label: "취소 (Cancel)",
          description: "PR 생성을 중단합니다."
        },
        {
          label: "수정 (Modify)",
          description: "제목 또는 본문을 수정한 후 생성합니다."
        }
      ]
    }
  ]
)
```

- **진행** → Phase 6으로 진행
- **취소** → "PR 생성이 취소되었습니다" 메시지 후 종료
- **수정** → 사용자 입력을 받아 draft 수정 후 Phase 6 진행

### Phase 6: PR 실행

```bash
gh pr create --base <target> --title "<title>" --body "<body>"
```

- 성공: PR URL 출력
- 실패: 에러 메시지 그대로 보고, 재시도 안 함

### Phase 7: 결과 보고

```
✓ PR 생성 완료

<PR URL>

요약:
- 제목: {title}
- 대상: {target}
```

---

## 에러 핸들링

| 상황                  | 처리                                                       |
| --------------------- | ---------------------------------------------------------- |
| Target 대비 커밋 없음 | 안내 후 종료                                               |
| author 에러           | 에러 보고, Phase 0부터 재시작 여부 사용자 확인             |
| reviewer 에러         | author 초안 그대로 사용, Phase 4로 진행 (수동 검증 권고)   |
| REDO 3회 이상         | 경고 + 사용자 확인 후 진행                                 |
| 문서 미갱신 감지      | 게이트 질문 — 업데이트 후 재실행 또는 무시하고 진행        |
| gh pr create 실패     | 에러 메시지 그대로 보고 (원격 문제일 가능성, 재시도 안 함) |

---

## 테스트 시나리오

### 정상 흐름 1: 문서 최신 상태

```
# 변경: app.js (feature 구현)
→ author: 제목+본문+문서 신선도 검사
→ reviewer: PASS
→ 게이트: "No stale documents found"
→ 사용자 확인: 진행
→ PR 생성: ✓ https://github.com/.../pull/123
```

### 정상 흐름 2: 문서 미갱신, 사용자가 무시하고 진행

```
# 변경: .env.example (새 env var 추가), README.md 미갱신
→ author: "env vars: NEW_API_KEY"로 감지
→ reviewer: PASS (author 판정 정확함)
→ 게이트: "env vars 미갱신" 경고
→ 사용자: "무시하고 진행"
→ PR 생성: ✓
```

### 에러 흐름 1: Target 대비 커밋 없음

```
# (현재 브랜치가 main과 같은 상태)
→ "target 대비 커밋이 없습니다."
→ 종료
```

### 에러 흐름 2: REDO 3회 이상

```
→ author: 초안 1
→ reviewer: REDO
→ author: 초안 2 (수정)
→ reviewer: REDO
→ author: 초안 3 (수정)
→ reviewer: REDO (3회차)
→ ⚠️ "자동 승인 한계 도달"
→ 사용자 확인: "고침" 또는 "진행"
```

---

## 참고

- Author/Reviewer는 `sonnet` 모델 사용 (PR 생성마다 반복 실행되는 경량 작업)
- Workspace 위치: `./.claude/_workspace/`
- 최대 재호출: 2회 (author 초호출 + 2회 재호출 = 총 3회)
- Scope 괄호 미사용 규칙 준수 (`feat: ...`, `feat(scope): ...` 금지)
- 문서 신선도 게이트는 **재실행 트리거** — 사용자가 업데이트 후 "PR 다시 만들어줘" 명령으로 다시 호출 가능
