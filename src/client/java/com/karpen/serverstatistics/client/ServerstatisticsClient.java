package com.karpen.serverstatistics.client;

import com.karpen.serverstatistics.client.screens.PlayTimeStatsScreen;
import com.karpen.serverstatistics.client.utils.ConfigManager;
import com.karpen.serverstatistics.client.utils.DataManager;
import com.karpen.serverstatistics.client.utils.TimeTracker;
import lombok.Getter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerstatisticsClient implements ClientModInitializer {

    @Getter
    private static final String MOD_ID = "serverstatistics";

    @Getter
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Getter
    private static ServerstatisticsClient instance;

    @Getter
    private TimeTracker timeTracker;

    @Getter
    private DataManager dataManager;

    @Getter
    private ConfigManager configManager;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Initializing Serverstatistics mod");

        this.configManager = new ConfigManager();
        this.dataManager = new DataManager(configManager);
        this.timeTracker = new TimeTracker(dataManager);

        dataManager.loadDate();

        registerEvents();

        LOGGER.info("Serverstatistics loaded");
    }

    private void registerEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            timeTracker.onClientTick(client);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            dataManager.saveDate();
        }));

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                addButtonToTitleScreen((TitleScreen) screen);
            } else if (screen instanceof GameMenuScreen) {
                addButtonToGameMenu((GameMenuScreen) screen);
            }
        });
    }

    private void addButtonToTitleScreen(TitleScreen screen) {
        try {
            int buttonWidth = 120;
            int buttonHeight = 20;
            int x = screen.width - buttonWidth - 10;
            int y = 10;

            ButtonWidget statsButton = ButtonWidget.builder(
                            Text.literal("Server Stats"),
                            button -> {
                                LOGGER.info("Stats button clicked in main menu!");
                                MinecraftClient client = MinecraftClient.getInstance();
                                if (client != null) {
                                    client.setScreen(new PlayTimeStatsScreen(Text.literal("Server Statistics")));
                                }
                            })
                    .position(x, y)
                    .size(buttonWidth, buttonHeight)
                    .build();

            Screens.getButtons(screen).add(statsButton);
            LOGGER.info("Button added to TitleScreen at position {}, {}", x, y);

        } catch (Exception e) {
            LOGGER.error("Failed to add button to TitleScreen", e);
        }
    }

    private void addButtonToGameMenu(GameMenuScreen screen) {
        try {
            int buttonWidth = 120;
            int buttonHeight = 20;
            int x = 10;
            int y = 50;

            ButtonWidget statsButton = ButtonWidget.builder(
                            Text.literal("Server Stats"),
                            button -> {
                                LOGGER.info("Stats button clicked in game menu!");
                                MinecraftClient client = MinecraftClient.getInstance();
                                if (client != null) {
                                    client.setScreen(new PlayTimeStatsScreen(Text.literal("Server Statistics")));
                                }
                            })
                    .position(x, y)
                    .size(buttonWidth, buttonHeight)
                    .build();

            Screens.getButtons(screen).add(statsButton);
            LOGGER.info("Button added to GameMenuScreen at position {}, {}", x, y);

        } catch (Exception e) {
            LOGGER.error("Failed to add button to GameMenuScreen", e);
        }
    }
}