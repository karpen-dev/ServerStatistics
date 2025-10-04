package com.karpen.serverstatistics.client.screens;

import com.karpen.serverstatistics.client.ServerstatisticsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ConfirmResetScreen extends Screen {

    public ConfirmResetScreen() {
        super(Text.literal("Confirm Reset"));
    }

    @Override
    protected void init() {
        super.init();

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("§cYES, RESET ALL"),
                        button -> confirmReset())
                .position(width / 2 - 105, height / 2 + 10)
                .size(100, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("§aCancel"),
                        button -> cancel())
                .position(width / 2 + 5, height / 2 + 10)
                .size(100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xDD000000);

        context.drawCenteredTextWithShadow(textRenderer, "§6Confirm Data Reset", width / 2, 60, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, "§7This will delete ALL playtime data!", width / 2, 80, 0xAAAAAA);
        context.drawCenteredTextWithShadow(textRenderer, "§7This action cannot be undone!", width / 2, 95, 0xAAAAAA);

        super.render(context, mouseX, mouseY, delta);
    }

    private void confirmReset() {
        ServerstatisticsClient.getInstance().getDataManager().resetAllData();
        if (client != null) {
            client.setScreen(new PlayTimeStatsScreen(Text.literal("Server Statistics")));
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§cAll playtime data has been reset!"), false);
            }
        }
    }

    private void cancel() {
        if (client != null) {
            client.setScreen(new PlayTimeStatsScreen(Text.literal("Server Statistics")));
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xDD000000);
    }
}