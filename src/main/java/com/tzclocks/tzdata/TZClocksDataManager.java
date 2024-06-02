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
import java.util.UUID;

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

    private List<TZClocksItem> timezoneItems = new ArrayList<>();
    private final Type timezoneItemsType = new TypeToken<ArrayList<TZClocksItem>>() {}.getType(); //takes saved data from data manager for conversion
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

        timezoneItems.clear();

        String timezonesJson = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_TIMEZONES);
        if (timezonesJson == null || timezonesJson.equals(EMPTY_ARRAY)) {
            plugin.setTimezones(new ArrayList<>());
        } else {
            try {
                // Check if the loaded data contains UUIDs (new format)
                if (timezonesJson.contains("\"uuid\":")) {
                    // Load as new format
                    timezoneItems = gson.fromJson(timezonesJson, timezoneItemsType);
                } else {
                    // Load as old format (List<String>)
                    Type oldItemsType = new TypeToken<ArrayList<String>>() {}.getType();
                    List<String> oldTimezoneIds = gson.fromJson(timezonesJson, oldItemsType);

                    // Convert old format to new format
                    for (String timezoneId : oldTimezoneIds) {
                        timezoneItems.add(convertIdToItem(UUID.randomUUID(), timezoneId, null));
                    }
                }

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
        List<TZClocksItem> tempTimezoneItems = new ArrayList<>(); // Temporary list

        for (TZClocksItem item : plugin.getTimezones()) {
            tempTimezoneItems.add(item); // Add to the temporary list
        }

        timezoneItems = tempTimezoneItems; // Automigically replace the original list

        final String timezonesJson = gson.toJson(timezoneItems);
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_TIMEZONES, timezonesJson);

    }

    private void convertItems() { //converts time zones for loading
        List<TZClocksItem> watchItems = new ArrayList<>();

        for (TZClocksItem timezoneItem : timezoneItems) {
            watchItems.add(convertIdToItem(timezoneItem.getUuid(), timezoneItem.getName(), timezoneItem.getCustomName()));
        }

        plugin.setTimezones(watchItems);
    }

    private TZClocksItem convertIdToItem(UUID uuid, String timezoneId, String customName) { //also converts time zones for loading
        ZoneId zoneId = ZoneId.of(timezoneId);
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = plugin.getFormatter();
        String currentTime = now.format(formatter);
        return new TZClocksItem(uuid, timezoneId, currentTime, customName);
    }
}