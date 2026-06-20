#!/usr/bin/env python3
"""Claude Code hook: Slack notification on Stop/Notification events."""

import sys
import json
import os
import urllib.request
from datetime import datetime


def parse_transcript(path: str) -> dict:
    last_content = ""
    total_input = 0
    total_output = 0
    total_cache_read = 0
    total_cache_1h = 0
    model_name = ""
    first_ts = None
    last_ts = None

    if not path or not os.path.exists(path):
        return {
            "last_content": last_content,
            "total_input": total_input,
            "total_output": total_output,
            "total_cache_read": total_cache_read,
            "total_cache_1h": total_cache_1h,
            "model_name": model_name,
            "first_ts": first_ts,
            "last_ts": last_ts,
        }

    with open(path) as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            try:
                d = json.loads(line)

                ts_str = d.get("timestamp", "")
                if ts_str:
                    ts = datetime.fromisoformat(ts_str.replace("Z", "+00:00"))
                    if first_ts is None:
                        first_ts = ts
                    last_ts = ts

                if d.get("type") == "assistant":
                    msg = d.get("message", {})
                    usage = msg.get("usage", {})

                    if usage:
                        total_input += usage.get("input_tokens", 0)
                        total_output += usage.get("output_tokens", 0)
                        total_cache_read += usage.get("cache_read_input_tokens", 0)
                        cc = usage.get("cache_creation", {})
                        total_cache_1h += cc.get("ephemeral_1h_input_tokens", 0)

                    if msg.get("model"):
                        model_name = msg["model"]

                    content = msg.get("content", [])
                    if isinstance(content, list):
                        for block in content:
                            if isinstance(block, dict) and block.get("type") == "text":
                                text = block.get("text", "").strip()
                                if text:
                                    last_content = text
                    elif isinstance(content, str) and content.strip():
                        last_content = content.strip()
            except Exception:
                continue

    return {
        "last_content": last_content,
        "total_input": total_input,
        "total_output": total_output,
        "total_cache_read": total_cache_read,
        "total_cache_1h": total_cache_1h,
        "model_name": model_name,
        "first_ts": first_ts,
        "last_ts": last_ts,
    }


def format_duration(first_ts, last_ts) -> str:
    if not first_ts or not last_ts:
        return "N/A"
    delta = last_ts - first_ts
    total_secs = int(delta.total_seconds())
    mins = total_secs // 60
    secs = total_secs % 60
    if mins >= 60:
        hours = mins // 60
        mins = mins % 60
        return f"{hours}h {mins}m"
    if mins > 0:
        return f"{mins}m {secs}s"
    return f"{secs}s"


def load_dotenv(path: str) -> dict:
    result = {}
    if not os.path.exists(path):
        return result
    with open(path) as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, _, value = line.partition("=")
            result[key.strip()] = value.strip().strip('"').strip("'")
    return result


def main():
    env_path = os.path.join(os.path.dirname(__file__), "..", "..", ".env")
    dotenv = load_dotenv(os.path.normpath(env_path))
    webhook_url = dotenv.get("SLACK_WEBHOOK_URL") or os.environ.get("SLACK_WEBHOOK_URL", "")
    if not webhook_url:
        print("SLACK_WEBHOOK_URL not set — skipping notification", file=sys.stderr)
        sys.exit(0)

    try:
        data = json.loads(sys.stdin.read())
    except Exception:
        data = {}

    session_id = data.get("session_id", "unknown")
    transcript_path = data.get("transcript_path", "")
    hook_event = data.get("hook_event_name", "Stop")
    notif_message = data.get("message", "")

    project_name = os.path.basename(os.getcwd())

    stats = parse_transcript(transcript_path)
    duration_str = format_duration(stats["first_ts"], stats["last_ts"])

    content_preview = stats["last_content"]
    if hook_event == "Notification" and notif_message:
        content_preview = notif_message
    content_preview = content_preview[:600] + ("..." if len(content_preview) > 600 else "")

    if hook_event == "Notification":
        emoji = ":bell:"
        status = "확인 대기 중"
    else:
        emoji = ":white_check_mark:"
        status = "작업 완료"

    short_id = session_id[:8] if session_id != "unknown" else "unknown"
    model_short = stats["model_name"].replace("claude-", "") if stats["model_name"] else "N/A"

    blocks = [
        {
            "type": "header",
            "text": {"type": "plain_text", "text": f"{emoji} Claude Agent — {project_name}"},
        },
        {
            "type": "section",
            "fields": [
                {"type": "mrkdwn", "text": f"*프로젝트:*\n`{project_name}`"},
                {"type": "mrkdwn", "text": f"*상태:*\n{status}"},
                {"type": "mrkdwn", "text": f"*세션 ID:*\n`{short_id}...`"},
                {"type": "mrkdwn", "text": f"*모델:*\n{model_short}"},
            ],
        },
        {
            "type": "section",
            "fields": [
                {"type": "mrkdwn", "text": f"*소요 시간:*\n{duration_str}"},
                {"type": "mrkdwn", "text": f"*토큰 (입력/출력):*\n{stats['total_input']:,} / {stats['total_output']:,}"},
                {"type": "mrkdwn", "text": f"*캐시 읽기:*\n{stats['total_cache_read']:,} tokens"},
                {"type": "mrkdwn", "text": f"*1h 세션 캐시:*\n{stats['total_cache_1h']:,} tokens"},
            ],
        },
    ]

    if content_preview:
        blocks.append(
            {
                "type": "section",
                "text": {
                    "type": "mrkdwn",
                    "text": f"*마지막 처리 내용:*\n```{content_preview}```",
                },
            }
        )

    payload = {
        "text": f"{emoji} Claude 세션 알림 — {project_name} ({status})",
        "blocks": blocks,
    }

    req = urllib.request.Request(
        webhook_url,
        data=json.dumps(payload).encode(),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req) as resp:
            print(f"Slack notification sent [{resp.status}]")
    except Exception as e:
        print(f"Failed to send Slack notification: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
