package com.fragmentedchaos.charmofundyingreborn.common;

import com.fragmentedchaos.charmofundyingreborn.Constants;

import com.fragmentedchaos.charmofundyingreborn.platform.CharmSlotServices;
import com.fragmentedchaos.charmofundyingreborn.platform.services.ITotemUseGuard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Optional;

/** 护符槽图腾的判定与消耗。 */
public final class DeathEventHandler {

    private DeathEventHandler() {
        throw new UnsupportedOperationException("DeathEventHandler cannot be instantiated");
    }

    public static boolean hasTotemInCharm(Player player) {
        if (player == null || player.isRemoved()) return false;
        try {
            if (!CharmSlotServices.charmSlot().hasCharmSlot(player)) return false;
            ItemStack stack = CharmSlotServices.charmSlot().getCharmSlot(player);
            return stack != null && !stack.isEmpty() && TotemHelper.isTotem(stack);
        } catch (Exception e) {
            Constants.LOG.error("Error checking charm slot: {}", e.getMessage());
            return false;
        }
    }

    /** 消耗并激活护符槽图腾；damageSource 透传给平台的否决钩子。 */
    public static boolean consumeAndActivate(Player player, DamageSource damageSource) {
        if (player == null || player.isRemoved()) return false;
        try {
            ItemStack stack = CharmSlotServices.charmSlot().getCharmSlot(player);
            if (stack == null || stack.isEmpty() || !TotemHelper.isTotem(stack)) return false;

            // 手牌路径里其它模组能拿到 LivingUseTotemEvent，护符槽短路了那段，这里补一次。
            ITotemUseGuard guard = CharmSlotServices.totemGuard();
            if (guard != null && !guard.allowTotemUse(player, stack, damageSource)) return false;

            Optional<ITotemEffect> provider = TotemProviders.getEffect(stack.getItem());
            ITotemEffect effect = provider.orElse(TotemProviders.VANILLA);

            ItemStack copy = stack.copy();
            player.setHealth(1.0F);
            boolean ok = effect.applyEffects(player, copy);
            if (ok) {
                // 先确认效果生效再扣图腾，避免自定义效果失败时玩家已死、图腾也没了。
                effect.modifyStack(stack);
                player.level().broadcastEntityEvent(player, (byte) 35);
                if (player instanceof ServerPlayer sp) {
                    copy.causeUseVibration(player, GameEvent.ITEM_INTERACT_FINISH);
                    CharmSlotServices.network().sendTotemUse(sp, copy);
                    sp.awardStat(Stats.ITEM_USED.get(copy.getItem()), 1);
                    AdvancementCompat.triggerUsedTotem(sp, copy);
                }
            } else {
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
