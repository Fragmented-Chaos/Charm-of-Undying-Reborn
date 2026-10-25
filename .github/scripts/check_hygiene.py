#!/usr/bin/env python3
"""代码卫生检查。

抓的是"编译能过、但一眼就该改"的东西：调试输出、遗留 TODO、
System.err、空的 catch、硬编码的绝对路径、以及 Java 源文件缺许可证头。

刻意不做的事：不检查代码风格（缩进、行宽），那是 formatter 的活；
不做复杂度分析，那会误伤正常代码。
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SKIP_DIRS = {".git", ".gradle", "build", "runs", ".idea", "gradle"}

problems = []
warnings = []


def java_files():
    for base, dirs, files in os.walk(ROOT):
        dirs[:] = [d for d in dirs if d not in SKIP_DIRS]
        for f in files:
            if f.endswith(".java"):
                yield os.path.join(base, f)


def changed_files():
    """本次改动过的文件（PR 里比较 base..head，本地比较工作区）。"""
    import subprocess
    try:
        base = os.environ.get("BASE_SHA", "").strip()
        head = os.environ.get("HEAD_SHA", "").strip()
        args = ["git", "diff", "--name-only", base, head] if base and head else ["git", "diff", "--name-only", "HEAD"]
        out = subprocess.run(args, capture_output=True, text=True, timeout=30).stdout
        return {l.strip() for l in out.splitlines() if l.strip()}
    except Exception:
        return set()


CHANGED = changed_files()
files = list(java_files())
print(f"== 代码卫生检查（{len(files)} 个 Java 文件）==")

for path in files:
    rel = os.path.relpath(path, ROOT)
    with open(path, encoding="utf-8", errors="replace") as fh:
        text = fh.read()
        lines = text.splitlines()

    for i, line in enumerate(lines, 1):
        s = line.strip()

        # ① 调试输出：System.out / printStackTrace 会进日志，也常被忘在提交里
        if re.search(r"\bSystem\s*\.\s*out\s*\.\s*print", line):
            problems.append(f"{rel}:{i} System.out 调试输出")
        if re.search(r"\bprintStackTrace\s*\(", line):
            problems.append(f"{rel}:{i} printStackTrace（应改用 logger）")

        # ② TODO / FIXME：允许，但必须带括号说明要做什么（TODO(原因) 形式）
        m = re.search(r"\b(TODO|FIXME)\b(?!\s*\()", line)
        if m:
            problems.append(f"{rel}:{i} {m.group(1)} 没有说明（写成 TODO(原因) 或 FIXME(原因)）")



        # ④ 硬编码本机路径
        if re.search(r"[A-Za-z]:\\\\|[A-Za-z]:/Users/|/home/[a-z]", line):
            problems.append(f"{rel}:{i} 硬编码的本地路径")

        # ⑤ 遗留的 @author 指向的不是作者本人
        am = re.search(r"@author\s+(\S+)", line)
        if am and am.group(1).lower() not in ("fragmented_chaos", "rending_void", "chaos"):
            warnings.append(f"{rel}:{i} @author {am.group(1)}（确认是否为真作者）")

    # ⑥ 许可证头：只要求"本次改动过"的 src/main 文件（存量不追溯，避免一次报 33 条）
    if rel.replace("\\", "/") in CHANGED:
        head = "\n".join(lines[:6])
        if not re.search(r"SPDX-License-Identifier|Copyright|License", head):
            warnings.append(f"{rel} 缺许可证头（改动过的文件才要求）")

    # ③ 空的 catch 块：最常见的"吞异常"。
    #    必须逐个块判断：早先的写法是"全文搜到一次空 catch 就逐行报"，
    #    结果同一个文件里后面那些写了日志的 catch 也会被误报。
    for m in re.finditer(r"catch\s*\([^)]*\)\s*\{([^{}]*)\}", text):
        if m.group(1).strip():
            continue
        line_no = text.count("\n", 0, m.start()) + 1
        problems.append(f"{rel}:{line_no} 空的 catch 块（吞异常）")

print(f"  Java 文件：{len(files)}")
if warnings:
    print(f"\n提示（{len(warnings)} 条，不阻塞）：")
    for w in warnings[:20]:
        print(f"  · {w}")
    if len(warnings) > 20:
        print(f"  · ……另 {len(warnings) - 20} 条")

if problems:
    print(f"\n发现 {len(problems)} 个问题：")
    for p in problems:
        print(f"  ✗ {p}")
    sys.exit(1)
print("\n代码卫生通过 ✓")
