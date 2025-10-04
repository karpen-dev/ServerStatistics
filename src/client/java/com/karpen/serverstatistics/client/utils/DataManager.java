package com.karpen.serverstatistics.client.utils;

import com.karpen.serverstatistics.client.models.PlayTimeDate;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DataManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("ServerStatistics/dateManager");

    @Getter
    private final ConfigManager configManager;
    
    @Getter
    private PlayTimeDate date;
    
    @Getter
    private boolean dirty = false;
    
    @Getter
    private long lastSaveTime = 0;

    public DataManager(ConfigManager configManager) {
        this.configManager = configManager;
        this.date = new PlayTimeDate();
    }

    public void loadDate() {
        Path dateFile = getdateFilePath();

        if (!Files.exists(dateFile)) {
            LOGGER.info("No existing date file found, starting fresh");
            return;
        }

        try (InputStream input = new FileInputStream(dateFile.toFile())) {
            Properties props = new Properties();
            props.load(input);

            date.setTotalMenuTime(Duration.ofSeconds(
                    Long.parseLong(props.getProperty("totalMenuTime", "0"))));
            date.setTotalServerTime(Duration.ofSeconds(
                    Long.parseLong(props.getProperty("totalServerTime", "0"))));

            Map<String, Duration> serverTimes = new HashMap<>();
            for (String key : props.stringPropertyNames()) {
                if (key.startsWith("server.")) {
                    String serverName = key.substring(7);
                    long seconds = Long.parseLong(props.getProperty(key));
                    serverTimes.put(serverName, Duration.ofSeconds(seconds));
                }
            }
            date.setServerTimes(serverTimes);

            LOGGER.info("Loaded playtime date: {} servers, menu: {}, server: {}",
                    serverTimes.size(), date.getTotalMenuTime(), date.getTotalServerTime());

        } catch (Exception e) {
            LOGGER.error("Failed to load playtime date", e);
        }
    }

    public void saveDate() {
        if (!dirty) return;

        try {
            Path dateFile = getdateFilePath();
            Path configDir = dateFile.getParent();

            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                LOGGER.info("Created config directory: {}", configDir);
            }

            Properties props = new Properties();

            props.setProperty("totalMenuTime", String.valueOf(date.getTotalMenuTime().getSeconds()));
            props.setProperty("totalServerTime", String.valueOf(date.getTotalServerTime().getSeconds()));

            for (Map.Entry<String, Duration> entry : date.getServerTimes().entrySet()) {
                String safeServerName = escapeServerName(entry.getKey());
                props.setProperty("server." + safeServerName,
                        String.valueOf(entry.getValue().getSeconds()));
            }

            props.setProperty("lastSave", java.time.Instant.now().toString());
            props.setProperty("modVersion", "1.0.0");

            try (OutputStream output = new FileOutputStream(dateFile.toFile())) {
                props.store(output, "Server Statistics date - DO NOT EDIT MANUALLY");
            }

            dirty = false;
            lastSaveTime = System.currentTimeMillis();
            LOGGER.info("Playtime date saved successfully to: {}", dateFile);

        } catch (Exception e) {
            LOGGER.error("Failed to save playtime date", e);
        }
    }

    private Path getdateFilePath() {
        return FabricLoader.getInstance().getConfigDir().resolve("serverstatistics/playtime_date.properties");
    }

    public void autoSaveIfNeeded() {
        if (!configManager.getModConfig().isAutoSave()) return;

        long currentTime = System.currentTimeMillis();
        long saveInterval = configManager.getModConfig().getAutoSaveInterval() * 1000L;

        if (dirty && (currentTime - lastSaveTime > saveInterval)) {
            saveDate();
        }
    }

    private String escapeServerName(String serverName) {
        return serverName.replace("=", "\\=").replace(":", "\\:");
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void resetAllData() {
        this.date = new PlayTimeDate();
        this.dirty = true;
        saveDate();
        LOGGER.info("All playtime date reset");
    }
}