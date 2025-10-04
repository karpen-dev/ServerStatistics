package com.karpen.serverstatistics.client.models;

import lombok.Data;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Data
public class PlayTimeDate {
    private Duration totalMenuTime = Duration.ZERO;
    private Duration totalServerTime = Duration.ZERO;
    private Map<String, Duration> serverTimes = new HashMap<>();

    public void addMenuTime(Duration duration) {
        this.totalMenuTime = this.totalMenuTime.plus(duration);
    }

    public void addServerTime(Duration duration) {
        this.totalServerTime = this.totalServerTime.plus(duration);
    }

    public void addServerTime(String serverName, Duration duration) {
        this.serverTimes.put(serverName,
                this.serverTimes.getOrDefault(serverName, Duration.ZERO).plus(duration));
    }
}
