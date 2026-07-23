package com.fragmentedchaos.charmofundyingreborn;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * NeoForge entry point for Charm of Undying: Reborn.
 * Death handling is managed via CharmSlotTotemMixin.
 */
@Mod(Constants.MOD_ID)
public class CharmOfUndyingReborn {

    public CharmOfUndyingReborn(IEventBus eventBus) {
        Constants.LOG.info("Starting {} on NeoForge by {}", Constants.MOD_NAME, Constants.MOD_AUTHORS);

        CharmOfUndyingRebornCommon.init();
        eventBus.addListener(this::registerCuriosCapabilities);
        ChorCommand.register();

        Constants.LOG.info("{} successfully initialized on NeoForge", Constants.MOD_NAME);
    }

    private void registerCuriosCapabilities(final RegisterCapabilitiesEvent evt) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (!isTotemItem(item)) continue;

            evt.registerItem(CuriosCapability.ITEM,
                    (s, ctx) -> new TotemCurio(s), item);
        }
    }

    private static boolean isTotemItem(Item item) {
        if (item.builtInRegistryHolder().is(ModTags.TOTEMS)) return true;
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return ModConfig.isCustomTotem(id);
    }

    /**
     * Minimal ICurio implementation for totem items in Curios slots.
     */
    private record TotemCurio(ItemStack stack) implements ICurio {
        @Override
        public ItemStack getStack() {
            return stack;
        }

        @Override
        public boolean canEquipFromUse(SlotContext slotContext) {
            return true;
        }
    }
}
