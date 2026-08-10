package com.fragmentedchaos.charmofundyingreborn;

import com.fragmentedchaos.charmofundyingreborn.common.TotemHelper;
import com.fragmentedchaos.charmofundyingreborn.platform.NeoForgeNetworkHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosSlotTypes;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.HashSet;
import java.util.Set;

/**
 * NeoForge entry point for Charm of Undying: Reborn.
 * Death handling is managed via CharmSlotTotemMixin.
 */
@Mod(Constants.MOD_ID)
public class CharmOfUndyingReborn {

    /**
     * Shared {@link ICurioItem}: dynamically reports whether a stack is a totem.
     */
    private static final ICurioItem TOTEM_CURIO_ITEM = new TotemCurioItem();

    /**
     * Items already registered through {@link CuriosApi#registerCurio(Item, ICurioItem)}.
     */
    private static final Set<Item> REGISTERED_CURIO_ITEMS = new HashSet<>();

    public CharmOfUndyingReborn(IEventBus eventBus) {
        Constants.LOG.info("Starting {} on NeoForge by {}", Constants.MOD_NAME, Constants.MOD_AUTHORS);

        CharmOfUndyingRebornCommon.init();
        registerTotemValidator();
        eventBus.addListener(NeoForgeNetworkHelper::onRegisterPayloads);
        // Register totem ICurioItems only once the server is fully started:
        // creating default ItemStacks requires DataComponents to be bound and
        // item tags to be loaded, which is guaranteed at this point.
        NeoForge.EVENT_BUS.addListener(CharmOfUndyingReborn::onServerStarted);
        ChorCommand.register();

        Constants.LOG.info("{} successfully initialized on NeoForge", Constants.MOD_NAME);
    }

    /**
     * Registers the slot validator for the Curios charm slot:
     * any item accepted by {@link TotemHelper#isTotem(ItemStack)} (the
     * {@code c:totems} tag) can be placed in the charm slot. Checked dynamically
     * at runtime, so datapack tag changes apply without restarting.
     *
     * <p>Note: an {@code ICurio} capability is intentionally NOT registered on every
     * item. Curios treats an existing {@code ICurio} as the authoritative equip check
     * ({@code canEquip}) for every slot; registering one on all items would block
     * non-totem items - including other mods' curios - from being equipped into
     * any curio slot. Slot eligibility is instead handled through this validator
     * (plus the {@code curios:charm} item tag).
     */
    private static void registerTotemValidator() {
        CuriosSlotTypes.registerPredicate(
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "totem"),
                (slotContext, stack) -> TotemHelper.isTotem(stack));
    }

    /**
     * Registers a dynamic {@link ICurioItem} on every item that carries the
     * {@code c:totems} tag, so totems can be auto-equipped from the hotbar
     * (right-click). Only totem items are registered, so other mods' items are
     * never intercepted or overridden.
     * <p>
     * Idempotent: items are only registered once. Evaluated at runtime through
     * {@link TotemCurioItem#hasCurioCapability(ItemStack)}.
     */
    public static void registerTotemCurios() {
        for (Item item : BuiltInRegistries.ITEM) {
            if (TotemHelper.isTotem(item.getDefaultInstance()) && REGISTERED_CURIO_ITEMS.add(item)) {
                CuriosApi.registerCurio(item, TOTEM_CURIO_ITEM);
                Constants.LOG.debug("Registered dynamic ICurioItem for totem item {}",
                        BuiltInRegistries.ITEM.getKey(item));
            }
        }
    }

    /**
     * Server fully started: DataComponents are bound and item tags are loaded,
     * so iterating items and creating default stacks is safe here. Catches both
     * config-based and tag-based totems (the c:totems tag only resolves at this
     * point in dedicated servers / single player).
     */
    private static void onServerStarted(ServerStartedEvent event) {
        registerTotemCurios();
    }

    /**
     * Dynamic {@link ICurioItem}: only totem stacks (tag OR config) report a curio
     * capability. Evaluated at runtime so config reloads take effect immediately.
     */
    private static class TotemCurioItem implements ICurioItem {

        @Override
        public boolean hasCurioCapability(ItemStack stack) {
            return TotemHelper.isTotem(stack);
        }

        @Override
        public boolean canEquip(SlotContext slotContext, ItemStack stack) {
            return TotemHelper.isTotem(stack);
        }

        @Override
        public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
            return TotemHelper.isTotem(stack);
        }
    }
}
