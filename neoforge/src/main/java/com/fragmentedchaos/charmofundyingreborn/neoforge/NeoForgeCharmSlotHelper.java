package com.fragmentedchaos.charmofundyingreborn.neoforge;

import com.fragmentedchaos.charmofundyingreborn.Constants;
import com.fragmentedchaos.charmofundyingreborn.platform.curios.CuriosCharmSlotHelper;
import com.fragmentedchaos.charmofundyingreborn.platform.services.ICharmSlotHelper;
import com.fragmentedchaos.charmofundyingreborn.platform.trinkets.TrinketsCharmSlotHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NeoForge charm slot provider accepting <b>either</b> Curios or Trinkets Updated.
 * <p>
 * Both are declared optional in neoforge.mods.toml (NeoForge cannot express "one of"), so the
 * provider is picked at runtime:
 * <ul>
 *   <li>only Curios &rarr; Curios</li>
 *   <li>only Trinkets Updated &rarr; Trinkets</li>
 *   <li>both &rarr; {@link CompositeCharmSlotHelper}: neither provider is disabled, both keep their
 *       slots, and the first slot that actually holds a totem is used</li>
 *   <li>neither &rarr; the charm slot stays inactive (startup already reported a loading error; this
 *       is only a safety net)</li>
 * </ul>
 * The delegate is resolved on first use, so the Curios classes are only loaded when Curios is
 * really present.
 */
public class NeoForgeCharmSlotHelper implements ICharmSlotHelper {

    /** Mod id of the Curios accessory provider. */
    public static final String CURIOS = "curios";
    /** Mod id of the Trinkets Updated accessory provider. */
    public static final String TRINKETS = "trinkets_updated";

    private static final AtomicBoolean MISSING_WARNING_LOGGED = new AtomicBoolean();

    private volatile ICharmSlotHelper delegate;

    @Override
    public String getPlatformName() {
        return delegate().getPlatformName();
    }

    @Override
    public ItemStack getCharmSlot(Player player) {
        return delegate().getCharmSlot(player);
    }

    @Override
    public boolean hasCharmSlot(Player player) {
        return delegate().hasCharmSlot(player);
    }

    private ICharmSlotHelper delegate() {
        ICharmSlotHelper local = this.delegate;
        if (local == null) {
            synchronized (this) {
                local = this.delegate;
                if (local == null) {
                    local = resolve();
                    this.delegate = local;
                }
            }
        }
        return local;
    }

    private static ICharmSlotHelper resolve() {
        List<ICharmSlotHelper> providers = new ArrayList<>(2);
        if (ModList.get().isLoaded(CURIOS)) {
            providers.add(new CuriosCharmSlotHelper());
        }
        if (ModList.get().isLoaded(TRINKETS)) {
            providers.add(new TrinketsCharmSlotHelper());
        }

        if (providers.size() == 1) {
            ICharmSlotHelper only = providers.get(0);
            Constants.LOG.info("Charm slot provider: {}", only.getPlatformName());
            return only;
        }
        if (!providers.isEmpty()) {
            Constants.LOG.info("Charm slot providers: Curios + Trinkets Updated "
                    + "(both slots stay usable; the first slot holding a totem wins)");
            return new CompositeCharmSlotHelper(providers);
        }
        // Defensive only: CharmOfUndyingReborn already reports a loading error when neither mod is
        // present, so startup normally never gets this far.
        if (MISSING_WARNING_LOGGED.compareAndSet(false, true)) {
            Constants.LOG.warn("Neither Curios API nor Trinkets Updated is installed, so the charm slot "
                    + "stays inactive. Install one of them to enable it.");
        }
        return InactiveCharmSlotHelper.INSTANCE;
    }

    /** Used when no supported accessory mod is installed. */
    private static final class InactiveCharmSlotHelper implements ICharmSlotHelper {

        static final InactiveCharmSlotHelper INSTANCE = new InactiveCharmSlotHelper();

        @Override
        public String getPlatformName() {
            return "none";
        }

        @Override
        public ItemStack getCharmSlot(Player player) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean hasCharmSlot(Player player) {
            return false;
        }
    }
}
