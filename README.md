# Charm of Undying: Reborn

[![Mod Loader](https://img.shields.io/badge/Fabric-26.1.x-blue)](https://fabricmc.net/)
[![Mod Loader](https://img.shields.io/badge/NeoForge-26.1.x-orange)](https://neoforged.net/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

将不死图腾放入护符饰品槽，死亡时自动触发原版复活效果。无需手持——图腾在饰品栏中即可生效。

## ✨ 功能

- 🔮 **护符槽**：通过饰品槽（Fabric: Trinkets / NeoForge: Curios）装备不死图腾
- 🏷️ **标签兼容**：基于 `c:totems` 标签识别图腾，任何标注此标签的物品均可使用
- ⚙️ **自定义图腾**：通过配置文件添加额外的图腾物品 ID

## 📦 安装

### 前置

| 平台 | 前置模组                                                                                                           |
|------|----------------------------------------------------------------------------------------------------------------|
| Fabric | [Fabric API](https://github.com/FabricMC/fabric-api) + [Trinkets Updated ](https://github.com/Patbox/trinkets) |
| NeoForge | [Curios API](https://github.com/TheIllusiveC4/Curios)                                                                  |

### 下载

1. 选择对应平台的 JAR 文件
2. 放入 `mods` 文件夹
3. 启动游戏

## 🎮 使用方法

1. 打开饰品界面（Trinkets / Curios 默认快捷键）
2. 将不死图腾放入 **护符（Charm）** 槽
3. 死亡时会自动消耗图腾并复活

## 📁 配置文件

自定义图腾列表位于 `config/charmofundyingreborn/custom_totems.json`：

```json
[
    "minecraft:totem_of_undying",
    "modid:custom_totem_item"
]
```

使用 `/chor reload` 热重载配置（NeoForge），`/chor check` 查看当前护符槽物品。



## 📄 许可证

MIT License © 2026 Fragmented_Chaos
