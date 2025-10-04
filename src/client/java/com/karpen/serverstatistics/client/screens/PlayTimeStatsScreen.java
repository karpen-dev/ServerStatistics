package com.karpen.serverstatistics.client.screens;

import com.karpen.serverstatistics.client.ServerstatisticsClient;
import com.karpen.serverstatistics.client.utils.DataManager;
import com.karpen.serverstatistics.client.utils.TimeTracker;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayTimeStatsScreen extends Screen {
    private final TimeTracker timeTracker;
    private final DataManager dataManager;

    public PlayTimeStatsScreen(Text title) {
        super(title);
        this.timeTracker = ServerstatisticsClient.getInstance().getTimeTracker();
        this.dataManager = ServerstatisticsClient.getInstance().getDataManager();
    }

    @Override
    protected void init() {
        super.init();

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Back"),
                        button -> close())
                .position(width / 2 - 100, height - 30)
                .size(200, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Save Data"),
                        button -> {
                            dataManager.saveDate();
                            if (client != null && client.player != null) {
                                client.player.sendMessage(Text.literal("§aData saved!"), false);
                            }
                        })
                .position(width / 2 - 100, height - 60)
                .size(200, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Reset Data"),
                        button -> {
                            if (client != null) {
                                client.setScreen(new ConfirmResetScreen());
                            }
                        })
                .position(width / 2 - 100, height - 90)
                .size(200, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (textRenderer == null) {
            ServerstatisticsClient.getLOGGER().error("textRenderer is null in render!");
            return;
        }

        ServerstatisticsClient.getLOGGER().info("Rendering PlayTimeStatsScreen, screen dimensions: width={}, height={}", width, height);

        context.drawTextWithShadow(textRenderer, "TEST TEXT", 10, 10, 0xFF0000);
        ServerstatisticsClient.getLOGGER().info("Rendering TEST TEXT at x=10, y=10");

        String title = "Server Statistics";
        int titleX = width / 2 - textRenderer.getWidth(title) / 2;
        context.drawTextWithShadow(textRenderer, title, titleX, 20, 0xFFFFFF);
        ServerstatisticsClient.getLOGGER().info("Rendering title '{}' at x={}, y=20", title, titleX);

        int yPos = 50;

        String status = timeTracker.isMenu() ? "In Menu" :
                timeTracker.isOnServer() ? "On Server: " + timeTracker.getCurrentServer() : "Unknown";
        drawTextGui(context, "Status: " + status, width / 2 - 150, yPos, 0xFFFFFF);
        ServerstatisticsClient.getLOGGER().info("Rendering status '{}' at x={}, y={}", status, width / 2 - 150, yPos);
        yPos += 25;

        context.fill(width / 2 - 150, yPos, width / 2 + 150, yPos + 1, 0x66FFFFFF);
        yPos += 15;

        Duration menuTime = timeTracker.getCurrentTotalMenuTime();
        String menuTimeText = "Menu Time: " + formatDuration(menuTime);
        context.drawTextWithShadow(textRenderer, menuTimeText, width / 2 - 150, yPos, 0xFFFFFF);
        ServerstatisticsClient.getLOGGER().info("Rendering menu time '{}' at x={}, y={}", menuTimeText, width / 2 - 150, yPos);
        yPos += 20;

        Duration serverTime = timeTracker.getCurrentTotalServerTime();
        String serverTimeText = "Server Time: " + formatDuration(serverTime);
        context.drawTextWithShadow(textRenderer, serverTimeText, width / 2 - 150, yPos, 0xFFFFFF);
        ServerstatisticsClient.getLOGGER().info("Rendering server time '{}' at x={}, y={}", serverTimeText, width / 2 - 150, yPos);
        yPos += 30;

        context.drawTextWithShadow(textRenderer, "Time by Server:", width / 2 - 150, yPos, 0xFFFFFF);
        ServerstatisticsClient.getLOGGER().info("Rendering 'Time by Server:' at x={}, y={}", width / 2 - 150, yPos);
        yPos += 20;

        Map<String, Duration> serverTimes = timeTracker.getCurrentServerTimes();
        if (serverTimes.isEmpty()) {
            context.drawTextWithShadow(textRenderer, "No server data collected yet", width / 2 - 150, yPos, 0xAAAAAA);
            ServerstatisticsClient.getLOGGER().info("Rendering 'No server data collected yet' at x={}, y={}", width / 2 - 150, yPos);
        } else {
            List<Map.Entry<String, Duration>> sortedServers = new ArrayList<>(serverTimes.entrySet());
            sortedServers.sort((a, b) -> Long.compare(b.getValue().getSeconds(), a.getValue().getSeconds()));

            int displayedCount = 0;
            for (Map.Entry<String, Duration> entry : sortedServers) {
                if (displayedCount >= 8) {
                    int remaining = sortedServers.size() - displayedCount;
                    if (remaining > 0) {
                        String remainingText = "... and " + remaining + " more servers";
                        context.drawTextWithShadow(textRenderer, remainingText, width / 2 - 150, yPos, 0x666666);
                        ServerstatisticsClient.getLOGGER().info("Rendering '{}' at x={}, y={}", remainingText, width / 2 - 150, yPos);
                    }
                    break;
                }

                String serverName = cleanServerName(entry.getKey());
                String time = formatDuration(entry.getValue());
                String serverText = serverName + ": " + time;
                context.drawTextWithShadow(textRenderer, serverText, width / 2 - 150, yPos, 0xFFFFFF);
                ServerstatisticsClient.getLOGGER().info("Rendering server '{}' at x={}, y={}", serverText, width / 2 - 150, yPos);
                yPos += 15;
                displayedCount++;
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private String cleanServerName(String serverName) {
        return serverName.replace("\\", "");
    }

    private void drawTextGui(DrawContext context, String text, int x, int y, int color) {
        context.drawTextWithShadow(textRenderer, text, x, y, color);
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        hours = duration.toHours();
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    @Override
    public void close() {
        dataManager.saveDate();
        super.close();
    }
}