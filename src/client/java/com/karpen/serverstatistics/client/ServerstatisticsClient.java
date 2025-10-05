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
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

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

        timeTracker.setMenu(true);
        timeTracker.setMenuEnterTime(Instant.now());

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
            if (screen instanceof StatsScreen) {
                addButtonToStatsScreen((StatsScreen) screen);
            } else if (screen instanceof OptionsScreen) {
                addButtonToOptionsScreen((OptionsScreen) screen);
            }
        });
    }

    private void addButtonToStatsScreen(StatsScreen screen) {
        try {
            int buttonWidth = 120;
            int buttonHeight = 20;
            int x = screen.width - buttonWidth - 10;
            int y = 5;

            ButtonWidget statsButton = ButtonWidget.builder(
                            Text.literal("Server Stats"),
                            button -> {
                                LOGGER.info("Server Stats button clicked in stats menu!");
                                MinecraftClient client = MinecraftClient.getInstance();
                                if (client != null) {
                                    client.setScreen(new PlayTimeStatsScreen());
                                }
                            })
                    .position(x, y)
                    .size(buttonWidth, buttonHeight)
                    .build();

            Screens.getButtons(screen).add(statsButton);
            LOGGER.info("Button added to StatsScreen at position {}, {}", x, y);

        } catch (Exception e) {
            LOGGER.error("Failed to add button to StatsScreen", e);
        }
    }

    private void addButtonToOptionsScreen(OptionsScreen screen) {
        try {
            int buttonWidth = 120;
            int buttonHeight = 20;
            int x = screen.width - buttonWidth - 10;
            int y = 10;

            ButtonWidget statsButton = ButtonWidget.builder(
                            Text.literal("Server Stats"),
                            button -> {
                                LOGGER.info("Server Stats button clicked in options menu!");
                                MinecraftClient client = MinecraftClient.getInstance();
                                if (client != null) {
                                    client.setScreen(new PlayTimeStatsScreen());
                                }
                            })
                    .position(x, y)
                    .size(buttonWidth, buttonHeight)
                    .build();

            Screens.getButtons(screen).add(statsButton);
            LOGGER.info("Button added to OptionsScreen at position {}, {}", x, y);

        } catch (Exception e) {
            LOGGER.error("Failed to add button to OptionsScreen", e);
        }
    }
}