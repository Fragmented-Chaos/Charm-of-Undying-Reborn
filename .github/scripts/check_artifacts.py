#!/usr/bin/env python3
"""产物检查：两端都要真的产出 jar，且 jar 里的元数据能被游戏读到。

很多"构建成功但用户装了没反应"的情况，都是 jar 里缺文件或版本号没被替换。
Gradle 的 build 成功只说明代码编过了，不代表产物可用，所以单独查一遍。
"""
import glob
import json
import os
import sys
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
problems = []

# 只检查"本次构建产出"的 jar。
# 为什么不能直接扫所有 libs：本地常留着旧版本产物（这里就有 26.1 的旧包），
# 扫全目录会让检查拿旧 jar 冒充新构建，等于没检查。
# 判定方式：只认最近 N 分钟内被修改过的文件（CI 里 build 刚跑完，一定是新的）。
import time

# 判定"哪些是本次构建产出的"：
#   优先读 ARTIFACT_SINCE_FILE（workflow 在 build 前写入的时间戳）。
#   不能用固定的时间窗：Gradle 判定任务 up-to-date 时不会重写 jar，
#   第二次构建产物的 mtime 还是旧的，固定窗口会把新包当旧包丢掉。
since_file = os.environ.get("ARTIFACT_SINCE_FILE", "").strip()
if since_file and os.path.exists(since_file):
    with open(since_file) as fh:
        since = float(fh.read().strip())
    print(f"  只认 {time.strftime('%H:%M:%S', time.localtime(since))} 之后产出的包")
else:
    since = time.time() - int(os.environ.get("ARTIFACT_MAX_AGE", "1800"))
now = time.time()

all_jars = [
    p for p in glob.glob(os.path.join(ROOT, "**", "libs", "*.jar"), recursive=True)
    if "/runs/" not in p and "/.gradle/" not in p
]

jars = [p for p in all_jars if os.path.getmtime(p) >= since]

stale = [p for p in all_jars if p not in jars]
if stale:
    print(f"  （忽略 {len(stale)} 个旧产物：{[os.path.basename(p) for p in stale[:4]]}）")

print("== 产物检查 ==")
print(f"  新产出的 jar：{len(jars)} 个")

if not jars:
    problems.append("本次构建没有产出任何 jar（build 没跑 assemble 或失败了）")

# ── universal 单包：README 让用户下载的就是这个，必须单独验 ──
# 两端各自出包不代表单包能用；单包是两个 mods.toml/json 塞进一个 jar，
# 任何一边的占位符没替换，用户装了就是"模组列表里显示 ${mod_version}"。
universal = next((j for j in jars if "universal" in os.path.basename(j)), None)
if not universal:
    problems.append("没有 universal 单包（README 让用户下载的就是它）")
else:
    import re
    with zipfile.ZipFile(universal) as z:
        names = z.namelist()
        print(f"\n  universal: {os.path.basename(universal)}"
              f"（{os.path.getsize(universal) / 1024:.0f} KB，{len(names)} 个条目）")
        for need in ("fabric.mod.json", "META-INF/neoforge.mods.toml"):
            if need not in names:
                problems.append(f"universal 包里缺 {need}")
        # 两边的版本必须一致，且都不能残留占位符
        versions = {}
        if "fabric.mod.json" in names:
            raw = z.read("fabric.mod.json").decode("utf-8")
            if "${" in raw:
                problems.append("universal 包的 fabric.mod.json 有未替换占位符")
            versions["fabric"] = json.loads(raw).get("version")
        if "META-INF/neoforge.mods.toml" in names:
            raw = z.read("META-INF/neoforge.mods.toml").decode("utf-8")
            if "${" in raw:
                problems.append("universal 包的 neoforge.mods.toml 有未替换占位符")
            m = re.search(r'version\s*=\s*"([^"]+)"', raw)
            versions["neoforge"] = m.group(1) if m else None
        print(f"     fabric={versions.get('fabric')} neoforge={versions.get('neoforge')}")
        if len(set(v for v in versions.values() if v)) > 1:
            problems.append(f"universal 包两端版本不一致：{versions}")

fabric_jar = next((j for j in jars if "fabric" in j and "sources" not in j and "javadoc" not in j), None)
neoforge_jar = next((j for j in jars if "neoforge" in j and "sources" not in j and "javadoc" not in j), None)

for label, jar in (("Fabric", fabric_jar), ("NeoForge", neoforge_jar)):
    if not jar:
        problems.append(f"{label} 端没有产物 jar")
        continue
    with zipfile.ZipFile(jar) as z:
        names = z.namelist()
        size = os.path.getsize(jar)
        print(f"\n  {label}: {os.path.basename(jar)}（{size / 1024:.0f} KB，{len(names)} 个条目）")

        if label == "Fabric":
            if "fabric.mod.json" not in names:
                problems.append("Fabric jar 里缺 fabric.mod.json")
            else:
                raw = z.read("fabric.mod.json").decode("utf-8")
                meta = json.loads(raw)
                for field in ("id", "version", "name"):
                    val = meta.get(field, "")
                    # 占位符没被替换 = 游戏里会显示 ${mod_version}
                    if isinstance(val, str) and "${" in val:
                        problems.append(f"fabric.mod.json 的 {field} 未替换：{val}")
                if "${" in raw:
                    problems.append("fabric.mod.json 里仍有未替换的占位符")
                print(f"     id={meta.get('id')} version={meta.get('version')}")
        else:
            toml = next((n for n in names if n.endswith("neoforge.mods.toml")), None)
            if not toml:
                problems.append("NeoForge jar 里缺 META-INF/neoforge.mods.toml")
            else:
                raw = z.read(toml).decode("utf-8")
                if "${" in raw:
                    bad = [t for t in ("${mod_version}", "${mod_id}", "${mod_name}") if t in raw]
                    problems.append(f"neoforge.mods.toml 里仍有未替换的占位符：{bad}")
                import re
                ver = re.search(r'version\s*=\s*"([^"]+)"', raw)
                mid = re.search(r'modId\s*=\s*"([^"$]+)"', raw)
                print(f"     modId={mid.group(1) if mid else '?'} version={ver.group(1) if ver else '?'}")

        # 依赖会被 remap 进 jar 的东西不该出现（那是开发期依赖）
        leaked = [n for n in names if n.startswith("net/fabricmc/") or n.startswith("net/neoforged/")]
        if leaked:
            problems.append(f"{label} jar 里打进了平台框架类：{leaked[:3]}")

if problems:
    print(f"\n发现 {len(problems)} 个问题：")
    for p in problems:
        print(f"  ✗ {p}")
    sys.exit(1)
print("\n产物检查通过 ✓")
