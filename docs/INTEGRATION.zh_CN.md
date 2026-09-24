# 与其他模组集成

面向**其他模组开发者**，说明如何让你的物品或代码与 Charm of Undying: Reborn 协作。

模组 ID：`charmofundyingreborn`

> **能力边界说明。** 模组对图腾的**识别**与**效果**是数据驱动的——一个标签加一个物品组件即可决定，接入无需写代码。但饰品槽查找、复活流程与激活动画包仍由代码实现，无法通过数据配置。
>
> English: [INTEGRATION.md](INTEGRATION.md)

---

## 一、通过数据接入（推荐，无需写代码）

### 1. 把物品加入 `#c:totems`

模组通过约定标签 `c:totems` 识别图腾。任何带此标签的物品都会被当作图腾。

在你的模组里贡献该标签（注意路径在 `c` 命名空间下）：

```json
// data/c/tags/item/totems.json
{
  "replace": false,
  "values": [
    "yourmod:your_totem"
  ]
}
```

`replace: false` 表示追加而非替换，其他模组的内容会被保留。

到此为止就够了——槽位兼容由模组自身在两个平台上处理，你不需要另外注册 trinket / curio 条目。

### 2. 用 `DEATH_PROTECTION` 组件定义复活效果

自 Minecraft 26.1 起，复活效果由物品的 `minecraft:death_protection` 组件描述。**本模组直接读取并执行该组件**，因此你的图腾可以拥有完全自定义的复活效果。

组件结构：

```
death_protection = {
  death_effects: [ <ConsumeEffect>, ... ]    // 可选，默认为空列表
}
```

可用的 `ConsumeEffect` 类型：

| type | 字段 | 作用 |
|---|---|---|
| `minecraft:clear_all_effects` | 无 | 清除全部状态效果 |
| `minecraft:apply_effects` | `effects`（必填）、`probability`（默认 `1.0`） | 施加状态效果 |
| `minecraft:remove_effects` | 见原版文档 | 移除指定状态效果 |
| `minecraft:teleport_randomly` | 见原版文档 | 随机传送 |
| `minecraft:play_sound` | 见原版文档 | 播放音效 |

`effects` 中每一项是 `MobEffectInstance`：

```
{ "id": "minecraft:regeneration", "duration": 900, "amplifier": 1 }
```

| 字段 | 默认值 | 说明 |
|---|---|---|
| `id` | 必填 | 状态效果 ID |
| `duration` | `0` | 持续时间（tick） |
| `amplifier` | `0` | 等级，从 0 开始（`1` = II 级） |
| `ambient` | `false` | 是否视为信标效果 |
| `show_particles` | `true` | 是否显示粒子 |
| `show_icon` | — | 是否显示图标 |
| `hidden_effect` | — | 隐藏的叠加效果 |

**完整示例**（效果等价于原版不死图腾）：

```json
{
  "death_effects": [
    { "type": "minecraft:clear_all_effects" },
    {
      "type": "minecraft:apply_effects",
      "effects": [
        { "id": "minecraft:regeneration",    "duration": 900, "amplifier": 1 },
        { "id": "minecraft:absorption",      "duration": 100, "amplifier": 1 },
        { "id": "minecraft:fire_resistance", "duration": 800, "amplifier": 0 }
      ]
    }
  ]
}
```

在 Java 中注册物品时：

```java
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.DeathProtection;

new Item.Properties()
    .stacksTo(1)
    .component(DataComponents.DEATH_PROTECTION, DeathProtection.TOTEM_OF_UNDYING);
```

或构造自定义效果（与上面的 JSON 等价）：

```java
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;

new DeathProtection(List.of(
    ClearAllStatusEffectsConsumeEffect.INSTANCE,
    new ApplyStatusEffectsConsumeEffect(List.of(
        new MobEffectInstance(MobEffects.REGENERATION, 900, 1),
        new MobEffectInstance(MobEffects.ABSORPTION, 100, 1),
        new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0)
    ))
));
```

### 回退行为

如果物品在 `c:totems` 标签里但**没有** `death_protection` 组件，模组会回退到原版不死图腾的效果（清除全部效果 + 再生 II / 吸收 II / 抗火）。

---

## 二、代码扩展：`ITotemEffect`

当数据接入不够用时（例如需要自定义消耗逻辑、生成粒子、执行命令），可以实现 `ITotemEffect` 并注册。

```java
package com.fragmentedchaos.charmofundyingreborn.common;

public interface ITotemEffect {

    default boolean bypassInvul() { return false; }

    void modifyStack(ItemStack stack);

    boolean applyEffects(Player player, ItemStack stack);
}
```

| 方法 | 调用时机 | 说明 |
|---|---|---|
| `applyEffects(player, stack)` | 复活时 | `stack` 是**消耗前**的快照；**调用前玩家血量已被设为 1**；返回 `false` 表示失败 |
| `modifyStack(stack)` | 复活时，**`applyEffects` 成功之后** | 默认实现为 `shrink(1)`；自定义消耗（如消耗耐久）在此实现。若 `applyEffects` 返回 `false` 则不会调用，因此复活失败不会消耗图腾 |
| `bypassInvul()` | **当前版本未被调用** | 预留接口，现阶段设置它没有任何效果 |

### 注册

```java
import com.fragmentedchaos.charmofundyingreborn.common.TotemProviders;

TotemProviders.register("yourmod:your_totem", new YourTotemEffect());
```

第一个参数是**物品注册名**（`namespace:path`），不是标签名。

请在模组初始化阶段调用。模组自身会在引导时注册 `minecraft:totem_of_undying` 的原版效果，若在那个时刻之前注册同一 ID，会被覆盖。

### 查找顺序

1. `PROVIDERS` 中精确匹配物品注册名 → 使用注册的效果
2. 未命中，且物品在 `c:totems` 标签中 → 使用原版效果（`TotemProviders.VANILLA`）
3. 都不命中 → 该物品不会被当作图腾

### 辅助 API

```java
import com.fragmentedchaos.charmofundyingreborn.ModTags;
import com.fragmentedchaos.charmofundyingreborn.common.TotemHelper;

TotemHelper.isTotem(itemStack);   // 物品是否在 c:totems 标签中
ModTags.TOTEMS;                   // TagKey<Item>，即 c:totems
```

---

## 三、行为与时机

### 服务端

复活判定与效果施加**只在服务端执行**（`!player.level().isClientSide()` 保护）。但模组本体需要**同时安装在客户端与服务端**——图腾激活动画由客户端渲染。

### 优先级

护符槽中的图腾**优先于手持图腾**被消耗。若同时存在，护符槽里的会先被用掉。

### 不生效的情况

`/kill`、虚空伤害等带 `BYPASSES_INVULNERABILITY` 的伤害**不会被图腾阻挡**，与原版手持行为一致。

### 槽位范围（重要）

当前实现会搜索玩家的**所有饰品槽**，而不只是 `charm` 槽。README 中"护符槽"是设计意图，实际行为更宽松，后续版本可能收紧。如果你的模组依赖"只有 charm 槽生效"这一前提，请注意这一点。

此外，Fabric 端注册了槽位兼容回调，会让**任何** `c:totems` 物品在**任意** trinket 槽中可装备——该回调返回的是决定性结果，会短路其他模组或数据包对这些槽位的兼容性判定。

---

## 四、感知或阻止复活（NeoForge）

NeoForge 端，护符槽复活会触发 `LivingUseTotemEvent`（与手持图腾走同一个事件）。取消该事件即可阻止复活：

```java
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;

@EventBusSubscriber(modid = "yourmod")
public final class YourTotemListener {

    @SubscribeEvent
    public static void onUseTotem(LivingUseTotemEvent event) {
        if (shouldBlock(event.getEntity())) {
            event.setCanceled(true);
        }
    }
}
```

事件携带的 `InteractionHand` 恒为 `MAIN_HAND`——饰品槽没有"手"的概念，主手只是占位值，请不要据此判断玩家真正手持的物品。

**Fabric 端没有对应事件**，护符槽复活无法被其他模组阻止。

---

## 五、运行前置

Charm of Undying: Reborn 运行期需要 Fabric 端的 **Trinkets** 或 NeoForge 端的 **Curios**。

---

## 六、破坏性变更

### 1.1.0-alpha.1

`ITotemEffect#applyEffects` 的签名已变更：

```java
// 1.0.x
boolean applyEffects(Player player);

// 1.1.0+
boolean applyEffects(Player player, ItemStack stack);
```

如果你实现过这个接口，需要同步修改。新增的 `ItemStack` 参数是消耗前的快照，用于读取物品自身的 `death_protection` 组件。
