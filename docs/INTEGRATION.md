# Integrating with Other Mods

This document is for **third-party mod developers** who want their items or code to work with
Charm of Undying: Reborn.

Mod ID: `charmofundyingreborn`

> **Scope note.** The mod's *recognition* of totems and the *effects* they apply are data-driven —
> a tag and an item component decide both, and no code is required to integrate. The accessory-slot
> lookup, the revival flow, and the activation packet are still implemented in code and are not
> configurable through data.
>
> 中文版：[INTEGRATION.zh_CN.md](INTEGRATION.zh_CN.md)

---

## 1. Data-driven integration (no code required)

### 1.1 Add your item to `#c:totems`

The mod recognises totems through the convention tag `c:totems`. Any item carrying that tag is
treated as a totem.

Contribute to the tag from your own mod (note that the path lives in the `c` namespace):

```json
// data/c/tags/item/totems.json
{
  "replace": false,
  "values": [
    "yourmod:your_totem"
  ]
}
```

`replace: false` appends instead of replacing, so entries from other mods are preserved.

That is all that is required for the item to be recognised. Slot compatibility is handled by the
mod itself on both loaders, so you do not need to register a trinket or curio entry of your own.

### 1.2 Define the revival effects with `DEATH_PROTECTION`

Since Minecraft 26.1 the revival effects are described by an item's `minecraft:death_protection`
component. **The mod reads and executes that component directly**, so your totem can define an
entirely custom resurrection.

Component shape:

```
death_protection = {
  death_effects: [ <ConsumeEffect>, ... ]    // optional, defaults to an empty list
}
```

Available `ConsumeEffect` types:

| `type` | Fields | Effect |
|---|---|---|
| `minecraft:clear_all_effects` | none | Removes every status effect |
| `minecraft:apply_effects` | `effects` (required), `probability` (default `1.0`) | Applies status effects |
| `minecraft:remove_effects` | see vanilla docs | Removes specific status effects |
| `minecraft:teleport_randomly` | see vanilla docs | Teleports the entity |
| `minecraft:play_sound` | see vanilla docs | Plays a sound |

Each entry in `effects` is a `MobEffectInstance`:

```
{ "id": "minecraft:regeneration", "duration": 900, "amplifier": 1 }
```

| Field | Default | Notes |
|---|---|---|
| `id` | required | Status effect ID |
| `duration` | `0` | Duration in ticks |
| `amplifier` | `0` | Zero-based level (`1` = level II) |
| `ambient` | `false` | Whether the effect is treated as beacon-provided |
| `show_particles` | `true` | Whether particles are displayed |
| `show_icon` | — | Whether the HUD icon is displayed |
| `hidden_effect` | — | A nested effect hidden behind this one |

**Complete example** (equivalent to the vanilla Totem of Undying):

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

To set it in Java when registering the item:

```java
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.DeathProtection;

new Item.Properties()
    .stacksTo(1)
    .component(DataComponents.DEATH_PROTECTION, DeathProtection.TOTEM_OF_UNDYING);
```

Or build a custom list (equivalent to the JSON above):

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

### 1.3 Fallback behaviour

If an item is in the `c:totems` tag but carries **no** `death_protection` component, the mod falls
back to the vanilla Totem of Undying's effects (clear all effects, then Regeneration II /
Absorption II / Fire Resistance).

---

## 2. Code extension: `ITotemEffect`

When the data-driven path is not enough — for example you need custom consumption, particles, or
command execution — implement `ITotemEffect` and register it.

```java
package com.fragmentedchaos.charmofundyingreborn.common;

public interface ITotemEffect {

    default boolean bypassInvul() { return false; }

    void modifyStack(ItemStack stack);

    boolean applyEffects(Player player, ItemStack stack);
}
```

| Method | Called | Notes |
|---|---|---|
| `applyEffects(player, stack)` | On revival | `stack` is a snapshot taken **before** consumption. The player's health has **already been set to 1** when this runs. Return `false` to signal failure. |
| `modifyStack(stack)` | On revival, **after** `applyEffects` succeeds | Default implementation is `shrink(1)`. Put custom consumption (durability, etc.) here. It is not called when `applyEffects` returns `false`, so a failed resurrection does not consume the totem. |
| `bypassInvul()` | **Not called in the current version** | Reserved. Setting it has no effect today. |

### Registering

```java
import com.fragmentedchaos.charmofundyingreborn.common.TotemProviders;

TotemProviders.register("yourmod:your_totem", new YourTotemEffect());
```

The first argument is the **item registry name** (`namespace:path`), not a tag name.

Call this during your mod's initialisation. The mod registers the vanilla effect for
`minecraft:totem_of_undying` in its own bootstrap, so registering that same ID before the mod
initialises would be overwritten.

### Lookup order

1. An exact match on the item registry name in `PROVIDERS` → the registered effect is used.
2. Otherwise, if the item is in the `c:totems` tag → the vanilla effect (`TotemProviders.VANILLA`).
3. Otherwise → the item is not treated as a totem.

### Helper API

```java
import com.fragmentedchaos.charmofundyingreborn.ModTags;
import com.fragmentedchaos.charmofundyingreborn.common.TotemHelper;

TotemHelper.isTotem(itemStack);   // is the item in the c:totems tag?
ModTags.TOTEMS;                   // TagKey<Item> for c:totems
```

---

## 3. Behaviour and timing

### Server side

The revival check and the effects are applied **only on the server** (guarded by
`!player.level().isClientSide()`). The mod itself must be installed on **both client and server**,
because the totem activation animation is rendered client-side.

### Priority

A totem in the charm slot is consumed **before** a totem held in hand. If both are present, the
charm-slot one is used up first.

### When it does not apply

Damage carrying `BYPASSES_INVULNERABILITY` — `/kill`, void damage, and similar — is **not** blocked
by a charm-slot totem, matching vanilla behaviour for a hand-held totem.

### Slot scope (important)

The current implementation searches **all** accessory slots, not just the `charm` slot. The
"charm slot" wording in the README describes the intent; the behaviour is broader, and this may be
tightened in a future version. If your mod relies on charm-slot-only semantics, be aware of this.

In addition, the Fabric build registers a slot-compatibility callback that makes **any**
`c:totems` item equippable in **any** trinket slot. Because that callback returns a decisive
result, it short-circuits other mods' and datapacks' compatibility checks for those slots.

---

## 4. Perceiving or blocking a revival (NeoForge)

On NeoForge, a charm-slot revival fires `LivingUseTotemEvent` — the same event as a hand-held
totem. Cancel it to prevent the revival:

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

The `InteractionHand` carried by the event is always `MAIN_HAND`. A charm slot has no concept of a
hand, so the main hand is used as a placeholder — do not use it to infer what the player is
actually holding.

**Fabric has no equivalent event**, so a charm-slot revival cannot be blocked by other mods there.

---

## 5. Version compatibility

A single jar supports **both Minecraft 26.1 and 26.2**.

Minecraft 26.2 moved `CriteriaTriggers` from `net.minecraft.advancements` to
`net.minecraft.advancements.triggers`. The mod resolves the trigger's location at runtime instead
of linking against it at compile time, so no per-version build is required.

Your mod only needs to declare a normal dependency on `charmofundyingreborn`.

### Required dependencies

Charm of Undying: Reborn requires **Trinkets** on Fabric or **Curios** on NeoForge at runtime.

---

## 6. Breaking changes

### 1.1.0-alpha.1

The signature of `ITotemEffect#applyEffects` changed:

```java
// 1.0.x
boolean applyEffects(Player player);

// 1.1.0+
boolean applyEffects(Player player, ItemStack stack);
```

Update any implementation accordingly. The added `ItemStack` is a snapshot taken before
consumption and is used to read the item's own `death_protection` component.
