package com.fragmentedchaos.charmofundyingreborn;

import com.fragmentedchaos.charmofundyingreborn.common.TotemHelper;
import com.fragmentedchaos.charmofundyingreborn.platform.NeoForgeNetworkHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.CuriosSlotTypes;
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
        registerTotemValidator();
        eventBus.addListener(this::registerCuriosCapabilities);
        eventBus.addListener(NeoForgeNetworkHelper::onRegisterPayloads);
        ChorCommand.register();

        Constants.LOG.info("{} successfully initialized on NeoForge", Constants.MOD_NAME);
    }

    /**
     * Registers the slot validator for the Curios charm slot:
     * any item accepted by {@link TotemHelper#isTotem(ItemStack)} (tag OR config)
     * can be placed in the charm slot. Checked dynamically at runtime,
     * so config reloads (e.g. {@code /chor reload}) take effect immediately.
     */
    private static void registerTotemValidator() {
        CuriosSlotTypes.registerPredicate(
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "totem"),
                (slotContext, stack) -> TotemHelper.isTotem(stack));
    }

    private void registerCuriosCapabilities(final RegisterCapabilitiesEvent evt) {
        // Register ICurio on ALL items so that config-added totems work at runtime.
        // Actual slot eligibility is decided dynamically by canEquip + slot validators.
        for (Item item : BuiltInRegistries.ITEM) {
            evt.registerItem(CuriosCapability.ITEM,
                    (s, ctx) -> new TotemCurio(s), item);
        }
    }

    /**
     * Dynamic ICurio: only totems (tag OR config) can be equipped into slots.
     * Evaluated at runtime so config reloads take effect immediately.
     */
    private record TotemCurio(ItemStack stack) implements ICurio {
        @Override
        public ItemStack getStack() {
            return stack;
        }

        @Override
        public boolean canEquip(SlotContext slotContext) {
            return TotemHelper.isTotem(stack);
        }

        @Override
        public boolean canEquipFromUse(SlotContext slotContext) {
            return TotemHelper.isTotem(stack);
        }
    }
}
