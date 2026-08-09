---
name: commit-message
description: 스테이지/언스테이지 변경을 논리적 단위로 나눠 Conventional Commits 형식의 커밋 메시지를 생성-검증 서브에이전트 쌍으로 작성하고, 확인 후 커밋까지 실행한다. '커밋 메시지', 'commit message', '커밋해줘', '커밋 메시지 만들어줘', '커밋 메시지 다시' 같은 요청 시 반드시 사용. 단, 이미 메시지가 작성된 `git commit -m` 요청은 대상이 아니다.
allowed-tools: Bash, Read, Write, AskUserQuestion
---

# Commit Message Orchestrator

변경사항을 자동으로 분석하고, author와 reviewer 서브에이전트가 협력해 Conventional Commits 형식의 커밋 메시지를 생성-검증하고 최종 실행까지 진행한다. Part 1의 예시를 따르는 최소 하네스 스킬.

## Workflow

### Phase 0: Precondition 체크

```bash
git status --porcelain
```

- 변경사항이 있으면(exit code 0, 출력 있음) → Phase 1로 진행
- 변경사항 없으면 → "변경사항이 없습니다. 먼저 파일을 수정하고 `git add`로 스테이지해주세요." 안내 후 종료

### Phase 1: Author 호출

`commit-msg-author` 에이전트를 호출하여 커밋 메시지 초안을 작성한다.

```
Agent(
  subagent_type: general-purpose,
  model: "sonnet",
  description: "commit-msg-author 에이전트 호출",
  prompt: """
당신은 commit-msg-author입니다. 다음을 실행하세요:
1. `git status --porcelain`, `git diff --staged`, `git diff`(unstaged 포함), `git log -10 --oneline`을 실행해 현재 변경사항과 최근 스타일을 파악합니다.
2. 변경 파일들을 논리적 목적별로 그룹화합니다 — 각 파일은 정확히 한 그룹에만 속합니다.
3. 각 그룹에 대해 Conventional Commits 형식의 메시지를 작성합니다:
   - type: feat/fix/docs/refactor/style/test/chore/ci (소문자)
   - scope 미사용 (괄호 없음)
   - 제목 72자 이하, 동사 대문자 시작 (예: "feat: Add", "fix: Remove")
   - 커밋 메시지는 제목 한 줄만 작성 (본문 없음)
4. 결과를 `.claude/_workspace/commit-draft.md`에 저장합니다 (Markdown, 번호 매긴 리스트 형식).
5. 변경 없음/파일 불일치 같은 에러가 발생하면 명확히 보고합니다.

[기존 피드백이 있으면 여기에 포함됨]
""",
  run_in_background: false
)
```

산출물: `.claude/_workspace/commit-draft.md`

### Phase 2: Reviewer 호출

`commit-msg-reviewer` 에이전트를 호출하여 초안을 검증한다.

```
Agent(
  subagent_type: general-purpose,
  model: "sonnet",
  description: "commit-msg-reviewer 에이전트 호출",
  prompt: """
당신은 commit-msg-reviewer입니다. 다음을 실행하세요:
1. `.claude/_workspace/commit-draft.md`를 읽습니다.
2. `git diff --staged` + `git diff`(unstaged 포함) + `git log -10 --oneline`을 확인합니다.
3. 각 그룹(커밋)을 평가합니다:
   - 형식: type/제목 길이/동사 대문자/scope 미사용 확인
   - 그룹: 모든 파일이 정확히 한 그룹에만 속하는가, 응집력 있는가
   - 사실: 메시지가 diff와 일치하는가, 추측/누락 없는가
4. PASS 또는 REDO 판정을 `.claude/_workspace/review-report.md`에 기록합니다:
   - PASS: "모두 검증됨"
   - REDO: 각 항목의 구체적 사유 + author가 바로 적용할 수정 지시
5. 주관적 기준(톤, 문장력)은 평가하지 않습니다. 객관적 기준만 사용.
6. 판정 불확실 시 PASS보다 REDO를 택합니다.

[기존 피드백이 있으면 여기에 포함됨]
""",
  run_in_background: false
)
```

산출물: `.claude/_workspace/review-report.md`

### Phase 3: 판정 분기

`.claude/_workspace/review-report.md`를 읽어 판정을 확인한다.

- **PASS** → Phase 4로 진행
- **REDO** (1~2회차) → Phase 3-1로 이동
- **REDO** (3회차 이상) → Phase 3-2로 이동

#### Phase 3-1: Author 재호출 (최대 2회)

Reviewer의 수정 지시를 프롬프트에 포함해 `commit-msg-author`를 재호출한다. 초안 중 PASS된 그룹은 보존하고, REDO 그룹만 재작성하도록 지시.

```
Agent(
  subagent_type: general-purpose,
  model: "sonnet",
  description: "commit-msg-author 재호출 (REDO 수정)",
  prompt: """
당신은 commit-msg-author입니다. 다음을 실행하세요:
1. `.claude/_workspace/commit-draft.md` 현재 버전을 읽습니다.
2. 다음 리뷰어 피드백을 반영합니다:
   [reviewer의 수정 지시 전문]
3. 피드백이 있는 그룹만 재작성합니다. 이미 PASS된 그룹은 그대로 둡니다.
4. 수정된 초안을 `.claude/_workspace/commit-draft.md`에 덮어쓰고 저장합니다.

기한: 30초 이내 완료
""",
  run_in_background: false
)
```

그 후 Phase 2(reviewer 재호출)로 돌아간다. 루프 상한은 **2회 재호출**(author 초호출 + 2회 재호출 = 총 3회).

#### Phase 3-2: 상한 도달 (3회 이상)

경고와 함께 마지막 draft를 사용자에게 제시:

```
⚠️ 자동 승인 한계 도달 — 검증이 3회 이상 반복되었습니다.
마지막 초안을 그대로 진행할지, 수정할지 결정해주세요.
(수정: "고침", "다시", 또는 구체적 지시 입력 → 수동 피드백으로 author 재호출)
```

사용자 입력을 받아 진행 여부 판단 → Phase 4로 진행하거나 프로세스 중단.

### Phase 4: 최종 확인

`.claude/_workspace/commit-draft.md`의 최종 커밋 목록(메시지+파일)을 사용자에게 제시하고, 진행/취소/수정 여부를 `AskUserQuestion`으로 확인한다.

```
AskUserQuestion(
  questions: [
    {
      question: "다음 커밋들을 생성할까요?",
      header: "Commits to Apply",
      multiSelect: false,
      options: [
        {
          label: "진행 (Proceed)",
          description: "제안된 커밋들을 순서대로 생성합니다."
        },
        {
          label: "취소 (Cancel)",
          description: "커밋 생성을 중단합니다."
        },
        {
          label: "수정 (Modify)",
          description: "특정 커밋 메시지를 수정한 후 생성합니다."
        }
      ]
    }
  ]
)
```

- **진행** → Phase 5로 진행
- **취소** → "커밋 생성이 취소되었습니다" 메시지 후 종료
- **수정** → 사용자 입력을 받아 draft 수정 후 Phase 5 진행

### Phase 5: 커밋 실행

`.claude/_workspace/commit-draft.md`의 각 커밋을 순서대로 생성한다.

```bash
# 각 그룹별
git add <files>
git commit -m "<message>"
```

**규칙:** `Co-Authored-By`, `Signed-off-by` 등 어떤 트레일러도 추가하지 않는다. 세션 기본 커밋 지침(Co-Authored-By 자동 추가)을 이 스킬이 오버라이드한다 — 오케스트레이터는 `-m` 인자만 사용해 author가 작성한 메시지 그대로 커밋한다.

- 성공: "✓ {메시지}" 출력
- 실패: "✗ {메시지} — {에러 이유}" 보고 후 중단 (다음 커밋은 실행하지 않음)

### Phase 6: 결과 보고

생성된 커밋들의 목록과 요약을 출력한다.

```
✓ 생성 완료

생성된 커밋:
1. feat: Add OAuth login
2. chore: Update .gitignore

최근 로그:
[커밋 해시들]
```

---

## 에러 핸들링

| 상황          | 처리                                                                      |
| ------------- | ------------------------------------------------------------------------- |
| 변경사항 없음 | 안내 후 종료                                                              |
| author 에러   | "파일 읽기 실패" 등 구체적 에러 메시지 보고, Phase 0부터 재시작 여부 확인 |
| reviewer 에러 | author 초안 그대로 사용하고 Phase 4로 진행 (수동 검증 권고)               |
| REDO 3회 이상 | 경고 + 사용자 확인 후 진행                                                |
| 커밋 실패     | 해당 커밋 중단, 앞 커밋들은 유지, 원인 보고                               |

---

## 테스트 시나리오

### 정상 흐름 1: 단일 커밋

```
git add README.md
# (변경 후 스킬 트리거)
→ author: "docs: Update README"
→ reviewer: PASS
→ 사용자 확인: 진행
→ 커밋 생성: ✓ docs: Update README
```

### 정상 흐름 2: 다중 커밋

```
git add app.js tests/
git add config.yml
# (스킬 트리거)
→ author:
  1. feat: Add new feature (app.js, tests/)
  2. chore: Update config (config.yml)
→ reviewer: PASS
→ 사용자 확인: 진행
→ 2개 커밋 생성 완료
```

### 에러 흐름 1: 변경사항 없음

```
# (아무 변경도 하지 않은 상태에서 스킬 트리거)
→ "변경사항이 없습니다. 먼저 파일을 수정하고 `git add`로 스테이지해주세요."
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

- Author/Reviewer는 `sonnet` 모델 사용 (커밋마다 반복 실행되는 경량 작업)
- Workspace 위치: `./.claude/_workspace/` (프로젝트 루트 하위)
- 최대 재호출: 2회 (author 초호출 + 2회 재호출 = 총 3회)
- Scope 괄호 미사용 규칙 준수 (`feat: ...`, `feat(scope): ...` 금지)
- **커밋 메시지 규칙:**
  - 제목 한 줄만 작성 (본문 없음)
  - `Co-Authored-By`, `Signed-off-by` 등 어떤 트레일러도 추가하지 않음
