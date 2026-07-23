package com.fragmentedchaos.charmofundyingreborn;

import com.fragmentedchaos.charmofundyingreborn.platform.CharmSlotServices;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class ChorCommand {

    private static final String COMMAND_ROOT = "chor";

    private ChorCommand() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ChorCommand::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal(COMMAND_ROOT)
                        .then(Commands.literal("reload")
                                .executes(ChorCommand::reloadConfig))
                        .then(Commands.literal("check")
                                .executes(ChorCommand::checkCharm))
        );
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> ctx) {
        try {
            ModConfig.reload();
            ctx.getSource().sendSuccess(
                    () -> Component.literal("Configuration reloaded successfully!").withColor(0x55FF55),
                    true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(
                    Component.literal("Failed to reload configuration.").withColor(0xFF5555));
            return 0;
        }
    }

    private static int checkCharm(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof Player player)) {
            source.sendFailure(Component.literal("Only players can use this command."));
            return 0;
        }

        try {
            if (!CharmSlotServices.CHARM_SLOT.hasCharmSlot(player)) {
                source.sendSuccess(
                        () -> Component.literal("You do not have a charm slot available.").withColor(0xAAAAAA),
                        false);
                return 1;
            }

            ItemStack charmStack = CharmSlotServices.CHARM_SLOT.getCharmSlot(player);
            if (charmStack == null || charmStack.isEmpty()) {
                source.sendSuccess(
                        () -> Component.literal("Your charm slot is empty.").withColor(0xAAAAAA),
                        false);
                return 1;
            }

            MutableComponent msg = TotemHelper.isTotem(charmStack)
                    ? Component.literal("You have a totem in your charm slot: " + charmStack.getHoverName().getString()).withColor(0x55FF55)
                    : Component.literal("Item in charm slot is not a totem: " + charmStack.getHoverName().getString()).withColor(0xFFFF55);
            source.sendSuccess(() -> msg, false);
            return 1;
        } catch (Exception e) {
            Constants.LOG.error("Error checking charm slot", e);
            return 0;
        }
    }
}
