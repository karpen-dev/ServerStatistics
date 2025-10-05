package com.karpen.serverstatistics.client.screens;

import com.karpen.serverstatistics.client.ServerstatisticsClient;
import com.karpen.serverstatistics.client.utils.DataManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ConfirmResetScreen extends Screen {

    private DataManager dataManager;
    private boolean isResetConfirmed = false;

    public ConfirmResetScreen() {
        super(Text.literal("Confirm Reset"));

        this.dataManager = ServerstatisticsClient.getInstance().getDataManager();
    }

    @Override
    protected void init() {
        super.init();

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("§cYES, RESET ALL"),
                        button -> {
                            if (!isResetConfirmed){
                                confirmReset();
                                button.setMessage(Text.literal("§cExit"));
                                return;
                            }
                            else {
                                if (button.getMessage().equals(Text.literal("§cExit"))) {
                                    this.close();
                                }
                            }

                        })
                .position(width / 2 - 105, height / 2 + 10)
                .size(100, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("§aCancel"),
                        button -> {
                            if (!isResetConfirmed) {
                                cancel();
                            } else {
                                button.active = false;
                            }
                        })
                .position(width / 2 + 5, height / 2 + 10)
                .size(100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderInGameBackground(context);

        assert client != null;
        context.drawCenteredTextWithShadow(client.textRenderer, "§6Confirm Data Reset", width / 2, 60, 0xFFFFFF);
        context.drawCenteredTextWithShadow(client.textRenderer, "§7This will delete ALL playtime data!", width / 2, 80, 0xAAAAAA);
        context.drawCenteredTextWithShadow(client.textRenderer, "§7This action cannot be undone!", width / 2, 95, 0xAAAAAA);

        super.render(context, mouseX, mouseY, delta);
    }

    private void confirmReset() {
        ServerstatisticsClient.getInstance().getDataManager().resetAllData();
        if (client != null) {
            client.setScreen(new PlayTimeStatsScreen());
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§cAll playtime data has been reset!"), false);
                dataManager.resetAllData();
            }
        }
    }

    private void cancel() {
        if (client != null) {
            client.setScreen(new PlayTimeStatsScreen());
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xDD000000);
    }
}