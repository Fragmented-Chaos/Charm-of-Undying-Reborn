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
import java.util.function.Consumer;

/**
 * NeoForge 的槽位提供者：Curios 或 Trinkets 二选一，两个都在时两者并存。
 * 委托对象在首次使用时才解析，这样没装 Curios 的实例不会加载 Curios 的类。
 */
public class NeoForgeCharmSlotHelper implements ICharmSlotHelper {

    public static final String CURIOS = "curios";
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

    @Override
    public void modifyCharmSlot(Player player, Consumer<ItemStack> action) {
        delegate().modifyCharmSlot(player, action);
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
        // 兜底：入口点已经报过加载错误，正常走不到这里。
        if (MISSING_WARNING_LOGGED.compareAndSet(false, true)) {
            Constants.LOG.warn("Neither Curios API nor Trinkets Updated is installed, so the charm slot "
                    + "stays inactive. Install one of them to enable it.");
        }
        return InactiveCharmSlotHelper.INSTANCE;
    }

    /** 没有任何支持的饰品模组时使用。 */
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

        @Override
        public void modifyCharmSlot(Player player, Consumer<ItemStack> action) {
        }
    }
}
