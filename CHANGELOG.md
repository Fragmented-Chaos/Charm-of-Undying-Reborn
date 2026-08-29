# 更新日志

## 1.0.3-beta

### 新增

- NeoForge 端新增 Curios 渲染器 `TotemCurioRenderer` + `ClientSetup`，将原版不死图腾以 3D 模型渲染到玩家身上

### 修复

- Fabric 端护符槽不再显示"注册名"：物品提示栏（属性栏）现在能正确显示"可放入护符槽位"
- 护符槽改用 `#c:totems` 标签链路，任意 `c:totems` 物品都能放入护符槽
- NeoForge 移除脆弱的 `CharmSlotLimitMixin`

### 重构

- NeoForge 护符槽与"图腾判定"解耦：移除 `charmofundyingreborn:totem` 槽位校验器，槽位按 `curios:charm` 标签 / ICurio 放行；复活仍仅对 `c:totems` 物品生效，避免误判其它模组的饰品


---

## 1.0.2-beta

### 修复

- 修复 NeoForge 端导致其他模组饰品无法放入 Curios 饰品栏的问题（不再为所有物品注册 `ICurio` 能力）

### 移除

- 移除自定义配置文件（`custom_totems.json`）功能，totem 识别完全由 `c:totems` 数据包标签驱动
- 移除 NeoForge 端 `/chor reload` 命令
- 移除配置功能相关的语言文件与无效/冗余资源文件（错误路径的物品标签、重复的实体槽位数据、冗余 mixins 配置）

---

## 1.0.1-beta

### 新功能

- 新增 `TotemUsePayload` 自定义网络包
- `ClientTotemHandler` 客户端渲染图腾物品图标

### 修复

- NeoForge 配置文件新增物品无法放入 Curios 槽位的问题
