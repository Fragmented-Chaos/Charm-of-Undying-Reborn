package com.fragmentedchaos.charmofundyingreborn.common;

import com.fragmentedchaos.charmofundyingreborn.Constants;
import com.fragmentedchaos.charmofundyingreborn.platform.CharmSlotServices;
import com.fragmentedchaos.charmofundyingreborn.platform.services.ITotemUseGuard;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

/** 护符槽图腾的判定与消耗。 */
public final class DeathEventHandler {

    private DeathEventHandler() {
        throw new UnsupportedOperationException("DeathEventHandler cannot be instantiated");
    }

    /**
     * 消耗并激活护符槽图腾。
     *
     * @param damageSource 致死伤害，透传给平台否决钩子
     */
    public static boolean consumeAndActivate(Player player, DamageSource damageSource) {
        if (player == null || player.isRemoved()) return false;
        try {
            ItemStack stack = CharmSlotServices.charmSlot().getCharmSlot(player);
            if (stack == null || stack.isEmpty() || !TotemHelper.isTotem(stack)) return false;

            // 手牌路径里其它模组能拿到 LivingUseTotemEvent，护符槽短路了那段，这里补一次。
            ITotemUseGuard guard = CharmSlotServices.totemGuard();
            if (guard != null && !guard.allowTotemUse(player, stack, damageSource)) return false;

            ITotemEffect effect = TotemProviders.getEffect(stack.getItem()).orElse(TotemProviders.VANILLA);

            ItemStack copy = stack.copy();
            // 契约：applyEffects 调用前生命值已是 1。这里的 healthBefore 本来就 ≤ 0（原版只在
            // isDeadOrDying 时才调 checkTotemDeathProtection）。失败时还原它是故意的：返回 false 后
            // 原版继续走 die()，玩家应当以 ≤ 0 的血量正常死亡，而不是带着 1 点血进死亡流程变成"活死人"。
            float healthBefore = player.getHealth();
            player.setHealth(1.0F);
            boolean ok;
            try {
                ok = effect.applyEffects(player, copy);
            } catch (Exception e) {
                // 不向上抛：checkTotemDeathProtection 处于 hurt 调用栈中，原版不会捕获。
                player.setHealth(healthBefore);
                Constants.LOG.error("Totem effect {} threw; the charm-slot totem was left untouched",
                        effect.getClass().getName(), e);
                return false;
            }
            if (ok) {
                // 先确认效果生效再扣图腾，避免自定义效果失败时玩家已死、图腾也没了。
                // 必须走 modifyCharmSlot：Curios 17 每次读取都重建 ItemStack。
                CharmSlotServices.charmSlot().modifyCharmSlot(player, effect::modifyStack);
                player.level().broadcastEntityEvent(player, (byte) 35);
                if (player instanceof ServerPlayer sp) {
                    copy.causeUseVibration(player, GameEvent.ITEM_INTERACT_FINISH);
                    CharmSlotServices.network().sendTotemUse(sp, copy);
                    sp.awardStat(Stats.ITEM_USED.get(copy.getItem()), 1);
                    CriteriaTriggers.USED_TOTEM.trigger(sp, copy);
                }
            } else {
                player.setHealth(healthBefore);
                Constants.LOG.warn("Totem effects did not apply; "
                        + "the totem in the charm slot was left untouched");
            }
            return ok;
        } catch (Exception e) {
            Constants.LOG.error("Error consuming totem", e);
            return false;
        }
    }
}
