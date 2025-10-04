package com.karpen.serverstatistics.client.utils;

import com.karpen.serverstatistics.client.ServerstatisticsClient;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class TimeTracker {
    private final DataManager dataManager;

    @Getter
    private Instant menuEnterTime;

    @Getter
    private Instant serverJoinTime;

    @Getter
    private String currentServer = "";

    @Getter
    private boolean isMenu = true;

    @Getter
    private boolean onServer = false;

    public TimeTracker(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void onClientTick(MinecraftClient client) {
        updateServerStatus(client);
        dataManager.autoSaveIfNeeded();
    }

    private void updateServerStatus(MinecraftClient client) {
        boolean wasIsMenu = isMenu;
        boolean wasOnServer = onServer;

        if (client.world != null && client.getCurrentServerEntry() != null) {
            ServerInfo serverInfo = client.getCurrentServerEntry();
            String serverAdderss = serverInfo.address;

            if (!onServer || !serverAdderss.equals(currentServer)) {
                if (wasOnServer) {
                    saveCurrentSession();
                }
                currentServer = serverAdderss;
                serverJoinTime = Instant.now();
                onServer = true;
                isMenu = false;
                ServerstatisticsClient.getLOGGER().info("Joined server: {}", currentServer);
            }
        } else if (client.world != null) {
            currentServer = "Singleplayer";
            if (!onServer) {
                serverJoinTime = Instant.now();
                onServer = true;
                isMenu = false;
                ServerstatisticsClient.getLOGGER().info("Starting singleplayer");
            }
        } else {
            if (!isMenu) {
                menuEnterTime = Instant.now();
                isMenu = true;
            }
            if (wasOnServer) {
                saveCurrentSession();
                onServer = false;
                currentServer = "";
                ServerstatisticsClient.getLOGGER().info("Returned to menu");
            }
        }

        if (wasIsMenu && !isMenu && menuEnterTime != null) {
            Duration menuSession = Duration.between(menuEnterTime, Instant.now());
            dataManager.getDate().addMenuTime(menuSession);
            dataManager.markDirty();
        }
    }

    private void saveCurrentSession() {
        if (serverJoinTime != null && onServer && !currentServer.isEmpty()) {
            Duration sessionTime = Duration.between(serverJoinTime, Instant.now());
            dataManager.getDate().addServerTime(sessionTime);
            dataManager.getDate().addServerTime(currentServer, sessionTime);
            dataManager.markDirty();
            ServerstatisticsClient.getLOGGER().debug("Saved session: {} - {}", currentServer, formatDuration(sessionTime));
        }
    }

    public static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    public static String formatDurationDetailed(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else {
            return String.format("%dm %ds", minutes, seconds);
        }
    }

    public Duration getCurrentTotalMenuTime() {
        Duration total = dataManager.getDate().getTotalMenuTime();
        if (isMenu && menuEnterTime != null) {
            total = total.plus(Duration.between(menuEnterTime, Instant.now()));
        }
        return total;
    }

    public Duration getCurrentTotalServerTime() {
        Duration total = dataManager.getDate    ().getTotalServerTime();
        if (onServer && serverJoinTime != null) {
            total = total.plus(Duration.between(serverJoinTime, Instant.now()));
        }
        return total;
    }

    public Map<String, Duration> getCurrentServerTimes() {
        Map<String, Duration> result = new HashMap<>(dataManager.getDate().getServerTimes());
        if (onServer && serverJoinTime != null && !currentServer.isEmpty()) {
            Duration currentSession = Duration.between(serverJoinTime, Instant.now());
            result.put(currentServer,
                    result.getOrDefault(currentServer, Duration.ZERO).plus(currentSession));
        }
        return result;
    }
}
