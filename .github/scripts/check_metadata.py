#!/usr/bin/env python3
"""元数据一致性检查。

这个模组是 common / fabric / neoforge 三模块结构，版本号、mod id、许可证
分散在 gradle.properties 和两端的元数据文件里。它们不一致时游戏能启动，
但用户看到的信息会是错的（比如 Fabric 端显示 v1.0.0、NeoForge 端显示 v1.1.0），
所以这里强制它们来自同一处。

退出码非 0 表示检查失败。
"""
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
problems = []
notes = []


def read(p):
    with open(os.path.join(ROOT, p), encoding="utf-8") as f:
        return f.read()


def props():
    """读 gradle.properties（跳过注释与空行）。"""
    out = {}
    for line in read("gradle.properties").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        out[k.strip()] = v.strip()
    return out


P = props()

# ① 关键属性必须存在
for k in ("mod_id", "mod_name", "mod_version", "license", "minecraft_version", "java_version"):
    if k not in P:
        problems.append(f"gradle.properties 缺少 {k}")

# ② 版本号格式：允许 1.1.0-alpha.2+26.3，但不允许 SNAPSHOT / 空格 / 中文
mv = P.get("mod_version", "")
if not re.fullmatch(r"\d+\.\d+\.\d+(?:[-+][0-9A-Za-z.\-+]+)*", mv):
    problems.append(f"mod_version 格式不规范：{mv!r}（应形如 1.1.0-alpha.2+26.3）")

# ③ 版本号必须带 MC 版本后缀，且和 minecraft_version 对得上。
#    不带后缀（例如 1.2.0）在双端模组里很危险：两端会打成同名 jar，
#    用户装错版本时看不出来，所以这里当成错误。
if "+" not in mv:
    problems.append(
        f"mod_version={mv!r} 缺少 MC 版本后缀（应形如 1.1.0+{P.get('minecraft_version')}）"
    )
else:
    suffix = mv.split("+", 1)[1]
    if suffix != P.get("minecraft_version"):
        problems.append(
            f"mod_version 的 +{suffix} 与 minecraft_version={P.get('minecraft_version')} 不一致"
        )

# ④ mod_version 里不该出现 -SNAPSHOT / -dev（发布物才会打 tag）
if re.search(r"snapshot|dev\b", mv, re.I):
    problems.append(f"mod_version 含有开发标记：{mv}")

# ⑤ Fabric 端元数据：占位符必须都被 build 替换，且 id 指向同一个属性
fabric = json.loads(read("fabric/src/main/resources/fabric.mod.json"))
if fabric.get("id") != "${mod_id}":
    problems.append(f"fabric.mod.json 的 id 应是 ${{mod_id}}，实际 {fabric.get('id')!r}")
if fabric.get("version") != "${mod_version}":
    problems.append(f"fabric.mod.json 的 version 应是 ${{mod_version}}，实际 {fabric.get('version')!r}")

# ⑥ Fabric 端声明的 environment 会影响服务端能否安装。
#    这个模组是护符槽 + 图腾触发，属于双端，必须是 *（不能是 client）。
env = fabric.get("environment")
if env not in ("*", None):
    notes.append(f"fabric.mod.json environment={env}（若是纯客户端模组才可设为 client）")

# ⑦ Fabric 依赖里必须有 java 和 minecraft
deps = fabric.get("depends", {})
for need in ("minecraft", "java", "fabricloader"):
    if need not in deps:
        problems.append(f"fabric.mod.json 缺少依赖声明：{need}")

# ⑧ NeoForge 端元数据是 toml，用正则抓占位符
toml = read("neoforge/src/main/resources/META-INF/neoforge.mods.toml")
for token in ("${mod_id}", "${mod_version}", "${mod_name}", "${license}"):
    if token not in toml:
        problems.append(f"neoforge.mods.toml 缺少占位符 {token}")

# ⑨ 两端声明的前置模组要一致（Fabric 用 trinkets，NeoForge 用 curios + trinkets_updated）
#    这里只做提示，因为两端生态本来就不同，不强制相同。
fc = sorted(k for k in deps if k not in ("minecraft", "java", "fabricloader", "fabric-api"))
nc = sorted(set(re.findall(r'modId\s*=\s*"([^"$]+)"', toml)) - {"neoforge", "minecraft"})
notes.append(f"Fabric 侧前置：{fc}")
notes.append(f"NeoForge 侧前置：{nc}")

# ⑩ Java 版本必须和 MC 版本匹配（26.x 需要 25）
jv = P.get("java_version")
mc = P.get("minecraft_version", "")
if mc.startswith("26.") and jv != "25":
    problems.append(f"MC {mc} 需要 Java 25，当前 {jv}")
elif mc.startswith("1.21") and jv != "21":
    problems.append(f"MC {mc} 需要 Java 21，当前 {jv}")

print("== 元数据检查 ==")
for n in notes:
    print(f"  · {n}")
if problems:
    print(f"\n发现 {len(problems)} 个问题：")
    for p in problems:
        print(f"  ✗ {p}")
    sys.exit(1)
print("\n元数据一致 ✓")
