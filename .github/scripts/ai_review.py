#!/usr/bin/env python3
"""AI 代码审查：读 PR 的 diff，让模型找"能编译但会出事"的问题。

审查范围刻意收窄到这类模组最容易踩的坑，而不是泛泛地让它"点评代码"：
  - common / fabric / neoforge 三层的职责边界（平台特有 API 不能漏进 common）
  - 事件注册与生命周期（重复注册、客户端/服务端侧搞反）
  - 图腾触发逻辑的边界（伤害来源、致死伤害、多人、取消事件）
  - 资源与元数据（配方、语言文件、mixins 配置）
  - 线程与状态（服务端线程里碰客户端类）

输出到 review.md，由 workflow 贴成 PR 评论。
没有 DEEPSEEK_API_KEY 时直接退出 0（不阻塞）。
"""
import json
import os
import subprocess
import sys
import urllib.request

API = "https://api.deepseek.com/chat/completions"
MODEL = os.environ.get("REVIEW_MODEL", "deepseek-chat")
MAX_DIFF_CHARS = 60000  # 太大的 PR 只截前面的，避免超 token

KEY = os.environ.get("DEEPSEEK_API_KEY", "").strip()
if not KEY:
    print("没有 DEEPSEEK_API_KEY，跳过")
    sys.exit(0)


def sh(*args):
    return subprocess.run(args, capture_output=True, text=True, check=True).stdout


# ---------- 取 diff ----------
base = os.environ.get("BASE_SHA", "").strip()
head = os.environ.get("HEAD_SHA", "").strip()

if base and head:
    diff = sh("git", "diff", "--unified=3", base, head)
    names = sh("git", "diff", "--name-only", base, head).splitlines()
else:
    # 本地试跑：跟 HEAD~1 比
    diff = sh("git", "diff", "--unified=3", "HEAD~1", "HEAD")
    names = sh("git", "diff", "--name-only", "HEAD~1", "HEAD").splitlines()

# 只审代码和资源，不审构建产物与存档
INTERESTING = (".java", ".json", ".toml", ".gradle", ".properties", ".md", ".yml", ".yaml")
names = [n for n in names if n.endswith(INTERESTING) and "/runs/" not in n and "build/" not in n]
diff = "\n".join(
    blk for blk in diff.split("\n") if not any(x in blk for x in ("/runs/", "/build/"))
)

if not names or not diff.strip():
    print("没有可审的改动")
    sys.exit(0)

print(f"审查 {len(names)} 个文件，diff {len(diff)} 字符")
truncated = False
if len(diff) > MAX_DIFF_CHARS:
    diff = diff[:MAX_DIFF_CHARS]
    truncated = True

title = os.environ.get("PR_TITLE", "(无标题)")
body = (os.environ.get("PR_BODY", "") or "")[:2000]

SYSTEM = """你是 Minecraft Fabric/NeoForge 多加载器模组的资深审查者。项目是 Charm of Undying: Reborn：
护符槽里放不死图腾，玩家受到致死伤害时自动消耗图腾挡死。结构为 common / fabric / neoforge 三层。

只报告"能编译但运行时或用户侧会出问题"的缺陷。重点关注：

1. 分层：common 里是否漏进了平台专属 API（net.minecraft.client 之外，比如 net.fabricmc.*、net.neoforged.* 出现在 common 就是错）。
2. 事件与注册：注册是否会在每次重载/每次进世界时重复；客户端专用代码是否可能在服务端线程执行；Fabric 与 NeoForge 两端行为是否一致。
3. 图腾触发：只应拦截"会导致死亡"的伤害；要处理伤害来源、多人同时触发、事件被取消、创造模式/无敌帧、图腾数量扣减与触发后的效果（血量/吸收/音效）是否与预期一致。
4. 资源与元数据：配方、语言文件键是否两端都有、mixins 配置是否与包名匹配、版本占位符是否会被正确替换。
5. 边界：null 检查、可空返回值、集合遍历时修改。

不要报告：命名风格、缩进、注释多少、文档缺失、性能微优化、以及"建议加测试"这类泛泛意见。
没有问题就明确说没有，不要为了凑数编造问题。

输出格式（Markdown，中文）：
## AI 审查
一句话结论（通过 / 有 N 个需要处理的问题）

### 需要处理
- **文件:行** —— 问题是什么，为什么会在运行或用户侧出问题。建议怎么改。

### 可以留意
（不确定但值得看一眼的，最多 3 条；没有就删掉这一节）
"""

user = f"""PR 标题：{title}

PR 描述：
{body or "（空）"}

改动的文件：
{chr(10).join("- " + n for n in names[:60])}

diff（{'已截断' if truncated else '完整'}）：
```diff
{diff}
```"""

payload = {
    "model": MODEL,
    "messages": [
        {"role": "system", "content": SYSTEM},
        {"role": "user", "content": user},
    ],
    "temperature": 0.2,
    "max_tokens": 2000,
}

req = urllib.request.Request(
    API,
    data=json.dumps(payload).encode("utf-8"),
    headers={"Content-Type": "application/json", "Authorization": f"Bearer {KEY}"},
)

try:
    with urllib.request.urlopen(req, timeout=180) as resp:
        data = json.loads(resp.read().decode("utf-8"))
    review = data["choices"][0]["message"]["content"].strip()
except Exception as e:  # 网络/额度问题不该让整个 CI 红
    print(f"AI 审查失败：{e}", file=sys.stderr)
    review = f"## AI 审查\n\n审查服务暂时不可用（{type(e).__name__}），本次跳过。这**不代表**代码没有问题。\n"

footer = "\n\n---\n<sub>由 AI 自动生成，仅供参考；合并前请以人工判断为准。</sub>"
with open("review.md", "w", encoding="utf-8") as f:
    f.write(review + footer)

print("已写入 review.md")
print(review[:500])
