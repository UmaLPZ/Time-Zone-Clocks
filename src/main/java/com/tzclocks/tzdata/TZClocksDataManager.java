package com.tzclocks.tzdata;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.tzclocks.TZClocksPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class TZClocksDataManager {
    private static final String CONFIG_KEY_TIMEZONES = "timezonekey";
    private static final String CONFIG_KEY_TABS = "tabsKey";
    private static final String CONFIG_GROUP = "timezonesgroup";
    private static final String LOAD_TIMEZONE_ERROR = "Exception occurred while loading user timezones";
    private static final String LOAD_TABS_ERROR = "Exception occurred while loading user tabs";
    private static final String EMPTY_ARRAY = "[]";

    private final TZClocksPlugin plugin;
    private final Client client;
    private final ConfigManager configManager;
    private final Gson gson;


    private final Type timezoneItemsType = new TypeToken<ArrayList<TZClocksItem>>() {}.getType();
    private final Type tabsDataType = new TypeToken<ArrayList<TZClocksTabData>>() {}.getType();
    private final Type oldTimezoneItemsType = new TypeToken<ArrayList<String>>() {}.getType();

    @Inject
    public TZClocksDataManager(TZClocksPlugin plugin, Client client, ConfigManager configManager, Gson gson) {
        this.plugin = plugin;
        this.client = client;
        this.configManager = configManager;
        this.gson = gson;
    }

    /**
     * Loads user-specific timezone and tab configurations.
     * Handles adding default displayName for older saved data.
     * @return true if loading was attempted, false if skipped.
     */
    public boolean loadData() {
        if (client.getGameState() == null || client.getGameState().getState() < GameState.LOGIN_SCREEN.getState()) {
            log.debug("Skipping data load, game state is {} or null", client.getGameState());
            return false;
        }
        log.info("Loading TZClocks user data...");

        List<TZClocksItem> userTimezones = loadUserTimezones();
        List<TZClocksTab> userTabs = loadUserTabs();


        plugin.setTimezones(userTimezones);
        plugin.setTabs(userTabs);

        log.info("Data loading process complete. Loaded {} user timezones and {} user tabs.", userTimezones.size(), userTabs.size());
        return true;
    }

    /**
     * Safely loads, parses, and processes user timezones from config.
     * Adds default displayName if missing from saved data or if converting from old format.
     */
    private List<TZClocksItem> loadUserTimezones() {
        String timezonesJson = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_TIMEZONES);
        log.debug("Raw user timezones JSON from config: {}", timezonesJson);

        if (timezonesJson == null || timezonesJson.equals(EMPTY_ARRAY) || timezonesJson.trim().isEmpty()) {
            log.debug("No user timezones found in config.");
            return new ArrayList<>();
        }

        List<TZClocksItem> loadedItems = null;
        boolean convertedFromOldFormat = false;
        try {
            String trimmedJson = timezonesJson.trim();
            if (trimmedJson.startsWith("[") && trimmedJson.contains("\"uuid\":")) {

                log.debug("Attempting to parse as new format (List<TZClocksItem>).");
                loadedItems = gson.fromJson(trimmedJson, timezoneItemsType);
                log.debug("Parsed {} items (before filtering/defaulting).", loadedItems == null ? 0 : loadedItems.size());
            } else if (trimmedJson.startsWith("[")) {

                log.info("Attempting conversion from old string list format.");
                List<String> oldTimezoneIds = gson.fromJson(trimmedJson, oldTimezoneItemsType);
                loadedItems = new ArrayList<>();
                if (oldTimezoneIds != null) {
                    for (String timezoneId : oldTimezoneIds) {
                        if (timezoneId != null && !timezoneId.trim().isEmpty()) {

                            loadedItems.add(new TZClocksItem(UUID.randomUUID(), timezoneId.trim(), null, null, null, null));
                        }
                    }
                }
                convertedFromOldFormat = true;
                log.info("Converted {} items from old format.", loadedItems.size());
            } else {
                log.warn("Unrecognized timezone JSON format: {}", timezonesJson);
                return new ArrayList<>();
            }
        } catch (JsonSyntaxException e) {
            log.error(LOAD_TIMEZONE_ERROR + " (JSON Syntax): " + e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error(LOAD_TIMEZONE_ERROR, e);
            return new ArrayList<>();
        }


        if (loadedItems != null) {
            for (TZClocksItem item : loadedItems) {

                if (item != null && (item.getDisplayName() == null || item.getDisplayName().trim().isEmpty())) {

                    if (item.getName() != null && !item.getName().trim().isEmpty()) {
                        item.setDisplayName(deriveDisplayNameFromName(item.getName()));
                        if (convertedFromOldFormat) {
                            log.debug("Set default displayName '{}' for converted item '{}'", item.getDisplayName(), item.getName());
                        } else {
                            log.debug("Set default displayName '{}' for loaded item '{}' (was missing)", item.getDisplayName(), item.getName());
                        }
                    } else {
                        log.warn("Cannot set default displayName for item with missing ZoneId name (UUID: {})", item.getUuid());

                        item.setDisplayName("Invalid Zone");
                    }
                }
            }


            return loadedItems.stream()
                    .filter(item -> item != null && item.getUuid() != null && item.getName() != null && item.getDisplayName() != null &&
                            !item.getUuid().equals(TZClocksPlugin.LOCAL_CLOCK_UUID) &&
                            !item.getUuid().equals(TZClocksPlugin.JAGEX_CLOCK_UUID))
                    .collect(Collectors.toList());
        } else {
            log.warn("Loaded timezone items list was null after parsing/conversion.");
            return new ArrayList<>();
        }
    }

    /**
     * Derives a user-friendly display name from a ZoneId string.
     * Example: "America/New_York" -> "New York", "Etc/UTC" -> "UTC"
     * @param zoneIdName The full ZoneId string.
     * @return A derived display name.
     */
    private String deriveDisplayNameFromName(String zoneIdName) {
        if (zoneIdName == null || zoneIdName.isEmpty()) {
            return "Unknown";
        }

        if ("Etc/UTC".equalsIgnoreCase(zoneIdName)) {
            return "UTC";
        }
        if ("Europe/London".equalsIgnoreCase(zoneIdName)) {
            return "London";
        }

        int lastSlash = zoneIdName.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash < zoneIdName.length() - 1) {

            String derived = zoneIdName.substring(lastSlash + 1);

            return derived.replace('_', ' ');
        }

        return zoneIdName;
    }


    /** Safely loads and parses user tabs from config. */
    private List<TZClocksTab> loadUserTabs() {
        String tabsJson = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_TABS);
        log.debug("Raw user tabs JSON from config: {}", tabsJson);

        if (tabsJson == null || tabsJson.equals(EMPTY_ARRAY) || tabsJson.trim().isEmpty()) {
            log.debug("No user tabs found in config.");
            return new ArrayList<>();
        }

        List<TZClocksTabData> loadedTabsDataList = null;
        try {
            loadedTabsDataList = gson.fromJson(tabsJson, tabsDataType);
            log.debug("Parsed {} TabData objects (before filtering).", loadedTabsDataList == null ? 0 : loadedTabsDataList.size());

        } catch (JsonSyntaxException e) {
            log.error(LOAD_TABS_ERROR + " (JSON Syntax): " + e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error(LOAD_TABS_ERROR, e);
            return new ArrayList<>();
        }


        if (loadedTabsDataList != null) {
            List<TZClocksTab> userTabs = new ArrayList<>();
            for (TZClocksTabData tabData : loadedTabsDataList) {
                if (tabData != null && tabData.getName() != null &&
                        !tabData.getName().equalsIgnoreCase(TZClocksPlugin.FIXED_TAB_NAME))
                {
                    List<UUID> userClockIds = (tabData.getClocks() == null) ? new ArrayList<>() :
                            tabData.getClocks().stream()
                                    .filter(id -> id != null &&
                                            !id.equals(TZClocksPlugin.LOCAL_CLOCK_UUID) &&
                                            !id.equals(TZClocksPlugin.JAGEX_CLOCK_UUID))
                                    .collect(Collectors.toList());


                    userTabs.add(new TZClocksTab(tabData.getName(), tabData.isCollapsed(), new ArrayList<>(userClockIds)));
                } else if (tabData != null && tabData.getName() != null && tabData.getName().equalsIgnoreCase(TZClocksPlugin.FIXED_TAB_NAME)) {
                    log.debug("Ignoring fixed tab '{}' found in saved data.", TZClocksPlugin.FIXED_TAB_NAME);
                }
            }
            return userTabs;
        } else {
            log.warn("Loaded tabs data list was null after parsing.");
            return new ArrayList<>();
        }
    }


    /**
     * Saves only the user-defined timezones and tabs to the configuration.
     * The `displayName` field is now included automatically by Gson.
     * Fixed clocks (UTC, London) and the fixed tab are excluded.
     */
    public void saveData() {
        if (plugin == null || plugin.getTimezones() == null || plugin.getTabs() == null) {
            log.error("Cannot save data - plugin references are null."); return;
        }
        log.debug("Saving TZClocks user data...");


        List<TZClocksItem> itemsToSave = plugin.getTimezones().stream()
                .filter(item -> item != null && item.getUuid() != null &&
                        !item.getUuid().equals(TZClocksPlugin.LOCAL_CLOCK_UUID) &&
                        !item.getUuid().equals(TZClocksPlugin.JAGEX_CLOCK_UUID))
                .collect(Collectors.toList());


        for (TZClocksItem item : itemsToSave) {
            if (item.getDisplayName() == null || item.getDisplayName().trim().isEmpty()) {
                log.warn("User timezone item '{}' missing display name before save. Setting default.", item.getName());
                item.setDisplayName(deriveDisplayNameFromName(item.getName()));
            }
        }

        final String timezonesJson = gson.toJson(itemsToSave);
        if (timezonesJson == null) { log.error("Failed to serialize user timezones to JSON."); return; }
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_TIMEZONES, timezonesJson);
        log.debug("Saved {} user timezones.", itemsToSave.size());


        List<TZClocksTabData> tabsDataToSave = new ArrayList<>();
        List<TZClocksTab> userTabs = plugin.getTabs();

        for (TZClocksTab tab : userTabs) {
            if (tab == null || tab.getName() == null || tab.getName().equalsIgnoreCase(TZClocksPlugin.FIXED_TAB_NAME)) continue;
            List<UUID> userClockIds = (tab.getClocks() == null) ? new ArrayList<>() :
                    tab.getClocks().stream()
                            .filter(id -> id != null && !id.equals(TZClocksPlugin.LOCAL_CLOCK_UUID) && !id.equals(TZClocksPlugin.JAGEX_CLOCK_UUID))
                            .collect(Collectors.toList());
            tabsDataToSave.add(new TZClocksTabData(tab.getName(), tab.isCollapsed(), userClockIds));
        }

        final String tabsJson = gson.toJson(tabsDataToSave);
        if (tabsJson == null) { log.error("Failed to serialize user tabs to JSON."); return; }
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_TABS, tabsJson);
        log.debug("Saved {} user tabs.", tabsDataToSave.size());
        log.debug("Data saving process complete.");
    }
}