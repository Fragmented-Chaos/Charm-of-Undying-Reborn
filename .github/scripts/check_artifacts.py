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

RECENT_SECONDS = int(os.environ.get("ARTIFACT_MAX_AGE", "1800"))  # 默认 30 分钟
now = time.time()

all_jars = [
    p for p in glob.glob(os.path.join(ROOT, "**", "libs", "*.jar"), recursive=True)
    if "/runs/" not in p and "/.gradle/" not in p
]

jars = [p for p in all_jars if now - os.path.getmtime(p) < RECENT_SECONDS]

stale = [p for p in all_jars if p not in jars]
if stale:
    print(f"  （忽略 {len(stale)} 个旧产物：{[os.path.basename(p) for p in stale[:4]]}）")

print("== 产物检查 ==")
print(f"  libs 下的 jar：{[os.path.relpath(j, ROOT) for j in jars] or '（无）'}")

if not jars:
    problems.append(f"最近 {RECENT_SECONDS // 60} 分钟内没有任何新 jar 产物（build 没跑 assemble 或失败了）")

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
