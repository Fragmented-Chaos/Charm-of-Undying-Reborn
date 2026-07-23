package com.fragmentedchaos.charmofundyingreborn;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration manager for custom totem item IDs.
 * Reads from "config/charmofundyingreborn/custom_totems.json".
 * Serves as a fallback for items that are not properly tagged.
 */
public final class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_DIR = "config/charmofundyingreborn";
    private static final String CONFIG_FILE = "custom_totems.json";

    private static final Set<Identifier> customTotems = new HashSet<>();

    private ModConfig() {
        throw new UnsupportedOperationException("ModConfig class cannot be instantiated");
    }

    /**
     * Loads custom totem IDs from the config file.
     * Creates a default config file if none exists.
     */
    public static void load() {
        customTotems.clear();

        Path configPath = getConfigPath();
        if (!Files.exists(configPath)) {
            createDefaultConfig(configPath);
            Constants.LOG.info("Created default config at {}", configPath);
            return;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            Set<String> ids = GSON.fromJson(reader,
                    new TypeToken<HashSet<String>>() {}.getType());

            if (ids != null) {
                for (String id : ids) {
                    try {
                        customTotems.add(Identifier.parse(id));
                    } catch (Exception e) {
                        Constants.LOG.warn("Invalid item ID in config: '{}' - skipping", id);
                    }
                }
            }
            Constants.LOG.info("Loaded {} custom totem IDs from config", customTotems.size());
        } catch (IOException e) {
            Constants.LOG.error("Failed to load config from {}", configPath, e);
            createDefaultConfig(configPath);
        }
    }

    /**
     * Reloads configuration from disk.
     */
    public static void reload() {
        Constants.LOG.info("Reloading configuration...");
        load();
    }

    /**
     * Returns an unmodifiable set of custom totem IDs from the config.
     */
    public static Set<Identifier> getCustomTotems() {
        return Collections.unmodifiableSet(customTotems);
    }

    /**
     * Checks whether a given item ID is listed in the custom totems config.
     */
    public static boolean isCustomTotem(Identifier id) {
        return customTotems.contains(id);
    }

    private static Path getConfigPath() {
        return Paths.get(CONFIG_DIR, CONFIG_FILE);
    }

    private static void createDefaultConfig(Path configPath) {
        try {
            Files.createDirectories(configPath.getParent());

            // Default: include vanilla totem as example
            Set<String> defaults = new HashSet<>();
            defaults.add("minecraft:totem_of_undying");

            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(defaults, writer);
            }
            customTotems.add(Identifier.parse("minecraft:totem_of_undying"));

            Constants.LOG.info("Created default custom_totems.json with vanilla totem as example");
        } catch (IOException e) {
            Constants.LOG.error("Failed to create default config at {}", configPath, e);
        }
    }
}
