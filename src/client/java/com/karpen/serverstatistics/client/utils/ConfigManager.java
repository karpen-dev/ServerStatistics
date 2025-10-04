package com.karpen.serverstatistics.client.utils;

import com.karpen.serverstatistics.client.ServerstatisticsClient;
import lombok.Data;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    @Getter
    private final Path configDir;

    @Getter
    private final Path dataFile;

    @Getter
    private final ModConfig modConfig;

    public ConfigManager() {
        this.configDir = FabricLoader.getInstance().getConfigDir().resolve("serverstatistics");
        this.dataFile = configDir.resolve("playtime_data.properties");
        this.modConfig = new ModConfig();
    }

    public void ensureConfigDir() {
        try {
            if (!Files.exists(configDir))
                Files.createDirectories(configDir);
        } catch (Exception e) {
            ServerstatisticsClient.getLOGGER().error("Failed to create config dir ", e);
        }
    }

    @Data
    public static class ModConfig {
        private boolean autoSave = true;
        private int autoSaveInterval = 30; // seconds
    }
}
