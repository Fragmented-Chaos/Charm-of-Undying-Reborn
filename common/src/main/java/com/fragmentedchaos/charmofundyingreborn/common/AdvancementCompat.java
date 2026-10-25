package com.fragmentedchaos.charmofundyingreborn.common;

import com.fragmentedchaos.charmofundyingreborn.Constants;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 反射访问 {@code CriteriaTriggers.USED_TOTEM}，让同一个 jar 能在 26.1 与 26.2 上跑。
 * <p>
 * 26.2 把 criteria/trigger 移到了 {@code net.minecraft.advancements.triggers}，静态 import 只能绑一个路径；
 * 而 26.1 起 Fabric 不再发 intermediary 映射，{@code net.minecraft.*} 就是运行时类名，所以字符串能直接解析。
 * 只在一次 JVM 生命周期内解析一次，之后每次都是 volatile 读 + 一次反射调用。
 */
public final class AdvancementCompat {

    /** {@code CriteriaTriggers} 的已知位置，新版本在前。 */
    private static final String[] CRITERIA_TRIGGERS_CANDIDATES = {
            "net.minecraft.advancements.triggers.CriteriaTriggers", // 26.2+
            "net.minecraft.advancements.CriteriaTriggers"           // 26.1
    };

    private static final String TRIGGER_FIELD = "USED_TOTEM";
    private static final String TRIGGER_METHOD = "trigger";

    /** 用 volatile 是为了让常见路径不每次进 monitor，同时安全发布下面两个字段。 */
    private static volatile boolean resolved;

    private static Object usedTotemTrigger;
    private static Method triggerMethod;

    private AdvancementCompat() {
        throw new UnsupportedOperationException("AdvancementCompat cannot be instantiated");
    }

    /** 触发原版 {@code minecraft:used_totem} 进度，效果等同手牌图腾。 */
    public static void triggerUsedTotem(ServerPlayer player, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()) return;

        if (!resolved) resolve();

        Object trigger = usedTotemTrigger;
        Method method = triggerMethod;
        if (trigger == null || method == null) return;

        try {
            method.invoke(trigger, player, stack);
        } catch (Throwable t) {
            // 进度查不到也不能打断复活本身。
            Constants.LOG.warn("Could not trigger the used_totem advancement: {}", t.toString());
        }
    }

    /** 最多解析一次，逐个尝试已知的包位置。 */
    private static synchronized void resolve() {
        if (resolved) return;

        Object trigger = null;
        Method method = null;

        for (String className : CRITERIA_TRIGGERS_CANDIDATES) {
            try {
                Class<?> criteriaTriggers = Class.forName(className);
                Field field = criteriaTriggers.getField(TRIGGER_FIELD);
                Object instance = field.get(null);
                if (instance == null) continue;

                // trigger 自身的类型也换了包，所以在运行时实例上找方法，不写死类型。
                method = instance.getClass()
                        .getMethod(TRIGGER_METHOD, ServerPlayer.class, ItemStack.class);
                trigger = instance;
                Constants.LOG.debug("Resolved used_totem trigger through {}", className);
                break;
            } catch (Throwable ignored) {
                // 试下一个位置。
            }
        }

        if (trigger == null) {
            Constants.LOG.debug("CriteriaTriggers.USED_TOTEM was not found; "
                    + "the used_totem advancement will not be granted on this Minecraft version");
        }

        usedTotemTrigger = trigger;
        triggerMethod = method;
        resolved = true;
    }
}
