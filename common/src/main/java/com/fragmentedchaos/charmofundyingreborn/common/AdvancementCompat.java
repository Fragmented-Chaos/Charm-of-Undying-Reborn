package com.fragmentedchaos.charmofundyingreborn.common;

import com.fragmentedchaos.charmofundyingreborn.Constants;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Version-tolerant bridge to {@code CriteriaTriggers.USED_TOTEM}, so that a single jar can run on
 * both Minecraft 26.1 and 26.2.
 *
 * <p>Background: 26.2 reorganized {@code net.minecraft.advancements}. Criteria and triggers moved
 * from {@code net.minecraft.advancements} to {@code net.minecraft.advancements.triggers}, while
 * predicates moved to {@code net.minecraft.advancements.predicates}. The old location was
 * {@code net.minecraft.advancements.CriteriaTriggers}.
 *
 * <p>A static {@code import} can only name one of the two paths, so compiling against either one
 * produces a jar that fails on the other version with {@link NoClassDefFoundError} the first time
 * the death handler runs. Resolving the trigger reflectively at runtime keeps one artifact valid on
 * both versions.
 *
 * <p>This is safe in production: since Minecraft 26.1, Fabric no longer ships intermediary or Yarn
 * mappings (both stop at 1.21.11), so {@code net.minecraft.*} are the real runtime class names on
 * Fabric as well as on NeoForge. String constants therefore resolve without a mapping resolver.
 *
 * <p>Cost: resolution runs once per JVM — on the first charm-slot resurrection — and the result is
 * cached. Afterwards each activation is a single volatile read plus one {@code Method.invoke}, and
 * a failed lookup degrades to a debug log rather than an exception in the death path.
 */
public final class AdvancementCompat {

    /**
     * Known locations of {@code CriteriaTriggers}, newest first.
     */
    private static final String[] CRITERIA_TRIGGERS_CANDIDATES = {
            "net.minecraft.advancements.triggers.CriteriaTriggers", // 26.2+
            "net.minecraft.advancements.CriteriaTriggers"           // 26.1
    };

    private static final String TRIGGER_FIELD = "USED_TOTEM";
    private static final String TRIGGER_METHOD = "trigger";

    /**
     * Guards the one-time {@link #resolve()}. Volatile so that the common path in
     * {@link #triggerUsedTotem} is a plain volatile read rather than a monitor enter/exit on every
     * activation, and so that the resolved fields below are safely published.
     */
    private static volatile boolean resolved;

    private static Object usedTotemTrigger;
    private static Method triggerMethod;

    private AdvancementCompat() {
        throw new UnsupportedOperationException("AdvancementCompat cannot be instantiated");
    }

    /**
     * Fires the vanilla {@code minecraft:used_totem} advancement criterion.
     *
     * <p>Mirrors what {@code LivingEntity#checkTotemDeathProtection} does for a hand-held totem, so
     * that a charm-slot resurrection grants the advancement identically.
     *
     * @param player the player being saved
     * @param stack  the consumed totem stack
     */
    public static void triggerUsedTotem(ServerPlayer player, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()) return;

        // Fast path: a single volatile read. Resolution has already run by now in all but the
        // very first activation of the JVM's lifetime.
        if (!resolved) resolve();

        Object trigger = usedTotemTrigger;
        Method method = triggerMethod;
        if (trigger == null || method == null) return;

        try {
            method.invoke(trigger, player, stack);
        } catch (Throwable t) {
            // Never let an advancement lookup break the resurrection itself.
            Constants.LOG.warn("Could not trigger the used_totem advancement: {}", t.toString());
        }
    }

    /**
     * Resolves {@code CriteriaTriggers.USED_TOTEM} at most once, trying each known package
     * location. Only ever entered while {@link #resolved} is false.
     */
    private static synchronized void resolve() {
        if (resolved) return; // another thread resolved it while we waited for the monitor

        Object trigger = null;
        Method method = null;

        for (String className : CRITERIA_TRIGGERS_CANDIDATES) {
            try {
                Class<?> criteriaTriggers = Class.forName(className);
                Field field = criteriaTriggers.getField(TRIGGER_FIELD);
                Object instance = field.get(null);
                if (instance == null) continue;

                // The trigger's own type moved packages too, so look the method up on the runtime
                // instance rather than naming its type.
                method = instance.getClass()
                        .getMethod(TRIGGER_METHOD, ServerPlayer.class, ItemStack.class);
                trigger = instance;
                Constants.LOG.debug("Resolved used_totem trigger through {}", className);
                break;
            } catch (Throwable ignored) {
                // Try the next known location.
            }
        }

        if (trigger == null) {
            Constants.LOG.debug("CriteriaTriggers.USED_TOTEM was not found; "
                    + "the used_totem advancement will not be granted on this Minecraft version");
        }

        usedTotemTrigger = trigger;
        triggerMethod = method;
        resolved = true; // volatile write: publishes the two fields above to other threads
    }
}
