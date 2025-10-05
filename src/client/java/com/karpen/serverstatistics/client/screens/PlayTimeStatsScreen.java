package com.karpen.serverstatistics.client.screens;

import com.karpen.serverstatistics.client.ServerstatisticsClient;
import com.karpen.serverstatistics.client.utils.DataManager;
import com.karpen.serverstatistics.client.utils.TimeTracker;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.time.Duration;
import java.util.Map;

public class PlayTimeStatsScreen extends Screen {
    private final TimeTracker timeTracker;
    private final DataManager dataManager;

    public PlayTimeStatsScreen() {
        super(Text.literal("Your server statistics"));
        this.timeTracker = ServerstatisticsClient.getInstance().getTimeTracker();
        this.dataManager = ServerstatisticsClient.getInstance().getDataManager();
        ServerstatisticsClient.getLOGGER().info("StatisticsScreen opened - Debug: Current menu time: {}, server time: {}",
                TimeTracker.formatDurationDetailed(timeTracker.getCurrentTotalMenuTime()),
                TimeTracker.formatDurationDetailed(timeTracker.getCurrentTotalServerTime()));
    }

    @Override
    protected void init() {
        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = (width - buttonWidth) / 2;
        int buttonY = height - 60;

        assert client != null;
        if (client.getWindow().getWidth() < (buttonWidth * 5)) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Save Data"), button -> {
                dataManager.saveDate();
                button.active = false;
                button.setMessage(Text.literal("Successfully saved"));
            }).dimensions(centerX, buttonY - 30, buttonWidth, buttonHeight).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Reset Data"), button -> {
                client.setScreen(new ConfirmResetScreen());
            }).dimensions(centerX, buttonY + 20, buttonWidth, buttonHeight).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> {
                this.close();
            }).dimensions(centerX, buttonY - 5, buttonWidth, buttonHeight).build());
        } else {
            addDrawableChild(ButtonWidget.builder(Text.literal("Save Data"), button -> {
                dataManager.saveDate();
                button.active = false;
                button.setMessage(Text.literal("Successfully saved"));
            }).dimensions(centerX - 210, buttonY - 5, buttonWidth, buttonHeight).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Reset Data"), button -> {
                client.setScreen(new ConfirmResetScreen());
            }).dimensions(centerX + 210, buttonY - 5, buttonWidth, buttonHeight).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> {
                this.close();
            }).dimensions(centerX, buttonY - 5, buttonWidth, buttonHeight).build());
        }

        ServerstatisticsClient.getLOGGER().debug("Screen init - Debug: Width={}, Height={}", width, height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderInGameBackground(context);

        assert client != null;

        int titleY = 20;
        int centerX = width / 2;

        String title = "Your server statistics";
        context.drawText(client.textRenderer, title,
                centerX - client.textRenderer.getWidth(title) / 2, titleY, 0xFFFFFFFF, false);

        Map<String, Duration> serverTimes = timeTracker.getCurrentServerTimes();
        int infoY = titleY + 20;

        String menuTime = String.format("Menu time: %s", TimeTracker.formatDurationDetailed(timeTracker.getCurrentTotalMenuTime()));
        context.drawText(client.textRenderer, menuTime,
                centerX - client.textRenderer.getWidth(menuTime) / 2, infoY, 0xFFFFFFFF, false);
        infoY += 20;

        context.drawText(client.textRenderer, "Server times:",
                centerX - client.textRenderer.getWidth("Server times:") / 2, infoY, 0xFFFFFFFF, false);
        infoY += 15;

        if (serverTimes.isEmpty()) {
            String emptyMessage = "Server list is empty :(";
            context.drawText(client.textRenderer, emptyMessage,
                    centerX - client.textRenderer.getWidth(emptyMessage) / 2, infoY, 0xFFFFFFFF, false);
        } else {
            for (Map.Entry<String, Duration> entry : serverTimes.entrySet()) {
                String serverName = entry.getKey().replaceAll("'", "");
                String timeStr = TimeTracker.formatDurationDetailed(entry.getValue());
                String displayText = serverName + ": " + timeStr;
                context.drawText(client.textRenderer, displayText,
                        centerX - client.textRenderer.getWidth(displayText) / 2, infoY, 0xFFFFFFFF, false);
                infoY += 15;
            }
        }

        if (client.world != null) {
            ServerstatisticsClient.getLOGGER().trace("Screen render - Debug: Mouse at ({}, {}), Delta: {}", mouseX, mouseY, delta);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        ServerstatisticsClient.getLOGGER().info("StatisticsScreen closed - Debug: Session end");
        super.close();
    }
}