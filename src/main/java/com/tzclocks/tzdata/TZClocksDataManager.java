package com.tzclocks.tzdata;

import com.tzclocks.TZClocksPlugin;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TZClocksDataManager {
    private static final String CONFIG_KEY_TIMEZONES = "timezonekey";
    private static final String CONFIG_GROUP = "timezonesgroup";
    private static final String LOAD_TIMEZONE_ERROR = "Exception occurred while loading timezones";
    private static final String EMPTY_ARRAY = "[]";
    private final TZClocksPlugin plugin;
    private final Client client;
    private final ConfigManager configManager;
    private final Gson gson;

    private List<String> timezoneIds = new ArrayList<>();
    private final Type timezoneIdsType = new TypeToken<ArrayList<String>>() {}.getType(); //not sure what this does either
    private final Type itemsType = new TypeToken<ArrayList<String>>() {}.getType(); //takes saved data from data manager for conversion
    @Inject
    public TZClocksDataManager(TZClocksPlugin plugin, Client client, ConfigManager configManager, Gson gson) { //saves time zones between client sessions
        this.plugin = plugin;
        this.client = client;
        this.configManager = configManager;
        this.gson = gson;
    }


// was used when adding time zone but did not always save to data manager; leaving here just in case
//    public void saveTimezoneToConfig(String timezoneId) {
//        if (!timezoneIds.contains(timezoneId)) {
//            timezoneIds.add(timezoneId);
//            saveData();
//        }
//    }

    public boolean loadData() { //loads data after starting client. works
        if (client.getGameState().getState() < GameState.LOGIN_SCREEN.getState()) {
            return false;
        }

        timezoneIds.clear();

        String timezonesJson = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_TIMEZONES);
        if (timezonesJson == null || timezonesJson.equals(EMPTY_ARRAY)) {
            plugin.setTimezones(new ArrayList<>());
        } else {
            try {
                timezoneIds = gson.fromJson(timezonesJson, itemsType);
                convertItems();
            } catch (Exception e) {
                log.error(LOAD_TIMEZONE_ERROR, e);
                plugin.setTimezones(new ArrayList<>());
            }
        }


        plugin.updateTimezoneData();
        return true;
    }

    public void saveData() { //saves data to config
        timezoneIds.clear();

        for (TZClocksItem item : plugin.getTimezones()) {
            timezoneIds.add(item.getName());
        }

        final String timezonesJson = gson.toJson(timezoneIds);
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_TIMEZONES, timezonesJson);

    }

    private void convertItems() { //converts time zones for loading
        List<TZClocksItem> watchItems = new ArrayList<>();

        for (String timezoneId : timezoneIds) {
            watchItems.add(convertIdToItem(timezoneId));
        }

        plugin.setTimezones(watchItems);
    }

    private TZClocksItem convertIdToItem(String timezoneId) { //also converts time zones for loading
        ZoneId zoneId = ZoneId.of(timezoneId);
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = plugin.getFormatter();
        String currentTime = now.format(formatter);
        return new TZClocksItem(timezoneId, currentTime);
    }
}