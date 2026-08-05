# 更新日志

## v1.0.1-beta

### ✨ 新功能

- **ITotemEffect 可扩展图腾效果体系**
  - 新增 `ITotemEffect` 接口：`modifyStack()` 自定义消耗方式、`applyEffects()` 自定义复活效果
  - `VanillaTotemEffect` 默认实现（原版药水效果）
  - `TotemProviders` 注册表，其他模组可通过 `TotemProviders.register("modid:item", effect)` 接入
  - 图腾三级接入机制：`c:totems` 标签（免代码）→ `custom_totems.json` 配置（免代码）→ API 注册（全功能）

- **自定义图腾复活动画**（1.0.0 仅原版 `broadcastEntityEvent(35)` 音效粒子，本版本新增图标）
  - 新增 `TotemUsePayload` 自定义网络包（基于原版 `CustomPacketPayload`，双平台兼容）
  - `ClientTotemHandler` 客户端渲染图腾物品图标
  - `INetworkHelper` 平台抽象，Fabric/NeoForge 各自实现

- **NeoForge（Curios）护符槽动态准入**
  - 槽位 JSON 新增 `validators`，配合 `CuriosSlotTypes.registerPredicate` 运行时动态判断
  - 配置文件（`custom_totems.json`）新增物品可立即放入护符槽，无需重启

- **NeoForge（Curios）护符槽单物品放入**
  - 新增 `CharmSlotLimitMixin`：拖入可堆叠物品时自动只放入 1 个，其余返回背包

### 修复

- NeoForge 配置文件新增物品无法放入 Curios 槽位的问题