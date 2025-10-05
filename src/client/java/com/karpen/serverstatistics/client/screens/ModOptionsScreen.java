package com.karpen.serverstatistics.client.screens;

import com.karpen.serverstatistics.client.ServerstatisticsClient;
import com.karpen.serverstatistics.client.utils.DataManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ModOptionsScreen extends Screen {
    private DataManager dataManager;

    public ModOptionsScreen() {
        super(Text.literal("ServerStatistis settings"));
        this.dataManager = ServerstatisticsClient.getInstance().getDataManager();
    }

    @Override
    protected void init() {
        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = (width - buttonWidth) / 2;
        int buttonY = height - 220;

        assert client != null;
        addDrawableChild(ButtonWidget.builder(Text.literal(
                String.format("Tracking activity: %s", dataManager.getConfigManager().getModConfig().isTrackingActivity())
        ), button -> {
            if (dataManager.getConfigManager().getModConfig().isTrackingActivity()) {
                dataManager.getConfigManager().getModConfig().setTrackingActivity(false);
                button.setMessage(Text.literal(String.format("Tracking activity: %s", dataManager.getConfigManager().getModConfig().isTrackingActivity())));
            } else {
                dataManager.getConfigManager().getModConfig().setTrackingActivity(true);
                button.setMessage(Text.literal(String.format("Tracking activity: %s", dataManager.getConfigManager().getModConfig().isTrackingActivity())));
            }
            dataManager.saveDate();
        }).dimensions(centerX, buttonY, buttonWidth, buttonHeight).build());

        addDrawableChild(ButtonWidget.builder(Text.literal(
                String.format("Show in options button: %s", dataManager.getConfigManager().getModConfig().isShowBtnInSettings())
        ), button -> {
            if (dataManager.getConfigManager().getModConfig().isShowBtnInSettings()) {
                dataManager.getConfigManager().getModConfig().setShowBtnInSettings(false);
                button.setMessage(Text.literal(String.format("Show in options button: %s", dataManager.getConfigManager().getModConfig().isShowBtnInSettings())));
            } else {
                dataManager.getConfigManager().getModConfig().setShowBtnInSettings(true);
                button.setMessage(Text.literal(String.format("Show in options button: %s", dataManager.getConfigManager().getModConfig().isShowBtnInSettings())));
            }
            dataManager.saveDate();
        }).dimensions(centerX, buttonY - 25, buttonWidth, buttonHeight).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Reset Data"), button -> {
            client.setScreen(new ConfirmResetScreen());
        }).dimensions(centerX, buttonY - 50, buttonWidth, buttonHeight).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> {
            client.setScreen(new PlayTimeStatsScreen());
        }).dimensions(centerX, buttonY - 75, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        renderInGameBackground(context);

        assert client != null;

        int titleY = 20;
        int centerX = width / 2;

        String title = "Server statistics settings:";
        context.drawText(client.textRenderer, title,
                centerX - client.textRenderer.getWidth(title) / 2, titleY, 0xFFFFFFFF, false);

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void close() {
        super.close();
    }
}
