package com.tzclocks.tzdata;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tzclocks.TZClocksPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors; // Make sure this import is present

@Slf4j
public class TZClocksDataManager {
    private static final String CONFIG_KEY_TIMEZONES = "timezonekey";
    private static final String CONFIG_KEY_TABS = "tabsKey";
    private static final String CONFIG_GROUP = "timezonesgroup"; // Consider renaming if different from plugin's group
    private static final String LOAD_TIMEZONE_ERROR = "Exception occurred while loading timezones";
    private static final String EMPTY_ARRAY = "[]";

    private final TZClocksPlugin plugin;
    private final Client client;
    private final ConfigManager configManager;
    private final Gson gson;

    // Temporary storage during load/save - not the primary data holders
    private List<TZClocksItem> loadedTimezoneItems = new ArrayList<>();
    private List<TZClocksTabData> loadedTabsData = new ArrayList<>();

    private final Type timezoneItemsType = new TypeToken<ArrayList<TZClocksItem>>() {}.getType();
    private final Type tabsDataType = new TypeToken<ArrayList<TZClocksTabData>>() {}.getType();

    @Inject
    public TZClocksDataManager(TZClocksPlugin plugin, Client client, ConfigManager configManager, Gson gson) {
        this.plugin = plugin;
        this.client = client;
        this.configManager = configManager;
        this.gson = gson;
    }

    /**
     * Loads user-specific timezone and tab configurations.
     * Fixed clocks (UTC, London) are added separately by the plugin itself.
     * @return true if loading was attempted (regardless of success), false if skipped due to game state.
     */
    public boolean loadData() {
        if (client.getGameState().getState() < GameState.LOGIN_SCREEN.getState()) {
            log.debug("Skipping data load, game state is {}", client.getGameState());
            return false;
        }
        log.debug("Loading TZClocks data...");

        loadedTimezoneItems.clear();
        loadedTabsData.clear();

        List<TZClocksItem> userTimezones = new ArrayList<>();
        List<TZClocksTab> userTabs = new ArrayList<>();

        // --- Load Timezones (User only) ---
        String timezonesJson = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_TIMEZONES);
        if (timezonesJson == null || timezonesJson.equals(EMPTY_ARRAY) || timezonesJson.trim().isEmpty()) {
            log.debug("No user timezones found in config.");
            // No user timezones saved, list remains empty
        } else {
            try {
                List<TZClocksItem> tempLoadedItems;
                // Handle potential old format (list of strings) vs new format (list of TZClocksItem objects)
                if (timezonesJson.trim().startsWith("[") && timezonesJson.contains("\"uuid\":")) {
                    // Assume new format
                    tempLoadedItems = gson.fromJson(timezonesJson, timezoneItemsType);
                } else if (timezonesJson.trim().startsWith("[")) {
                    // Attempt conversion from old format (list of strings)
                    log.info("Attempting conversion from old timezone data format.");
                    Type oldItemsType = new TypeToken<ArrayList<String>>() {}.getType();
                    List<String> oldTimezoneIds = gson.fromJson(timezonesJson, oldItemsType);
                    tempLoadedItems = new ArrayList<>();
                    for (String timezoneId : oldTimezoneIds) {
                        // Generate UUID, leave time/custom name null initially
                        tempLoadedItems.add(new TZClocksItem(UUID.randomUUID(), timezoneId, null, null, null));
                    }
                    log.info("Successfully converted {} timezones from old format.", tempLoadedItems.size());

                } else {
                    log.warn("Unrecognized timezone JSON format, starting fresh.");
                    tempLoadedItems = new ArrayList<>();
                }

                // Ensure loaded items are valid and don't include fixed IDs - defensive check
                if (tempLoadedItems != null) {
                    userTimezones = tempLoadedItems.stream()
                            .filter(item -> item != null && item.getUuid() != null && item.getName() != null &&
                                    !item.getUuid().equals(TZClocksPlugin.LOCAL_CLOCK_UUID) &&
                                    !item.getUuid().equals(TZClocksPlugin.JAGEX_CLOCK_UUID))
                            .collect(Collectors.toList());
                    log.debug("Loaded {} valid user timezones.", userTimezones.size());
                }

            } catch (Exception e) {
                log.error(LOAD_TIMEZONE_ERROR, e);
                // Keep userTimezones empty on error
            }
        }
        // Set the loaded user timezones in the plugin (fixed ones are added in plugin.startUp)
        plugin.setTimezones(userTimezones);


        // --- Load Tabs (User only) ---
        String tabsJson = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_TABS);
        if (tabsJson != null && !tabsJson.equals(EMPTY_ARRAY) && !tabsJson.trim().isEmpty()) {
            try {
                loadedTabsData = gson.fromJson(tabsJson, tabsDataType);
                if (loadedTabsData != null) {
                    // Convert loaded tab data to TZClocksTab objects
                    for (TZClocksTabData tabData : loadedTabsData) {
                        if (tabData != null && tabData.getName() != null &&
                                !tabData.getName().equalsIgnoreCase("Game Times")) // Ensure fixed tab isn't loaded
                        {
                            // Filter clock IDs within the tab just in case
                            List<UUID> userClockIds = (tabData.getClocks() == null) ? new ArrayList<>() :
                                    tabData.getClocks().stream()
                                            .filter(id -> id != null &&
                                                    !id.equals(TZClocksPlugin.LOCAL_CLOCK_UUID) &&
                                                    !id.equals(TZClocksPlugin.JAGEX_CLOCK_UUID))
                                            .collect(Collectors.toList());

                            userTabs.add(new TZClocksTab(tabData.getName(), tabData.isCollapsed(), userClockIds));
                        }
                    }
                    log.debug("Loaded {} user tabs.", userTabs.size());
                }
            } catch (Exception e) {
                log.error("Error loading user tabs:", e);
                // Keep userTabs empty on error
            }
        } else {
            log.debug("No user tabs found in config.");
        }
        // Set the loaded user tabs in the plugin
        plugin.setTabs(userTabs);

        log.debug("Data loading process complete.");
        return true; // Loading was attempted
    }

    /**
     * Saves only the user-defined timezones and tabs to the configuration.
     * Fixed clocks (UTC, London) are excluded.
     */
    public void saveData() {
        log.debug("Saving TZClocks data...");
        // --- Save Timezones (User only) ---
        // Filter out fixed clocks before saving
        List<TZClocksItem> itemsToSave = plugin.getTimezones().stream()
                .filter(item -> !item.getUuid().equals(TZClocksPlugin.LOCAL_CLOCK_UUID) &&
                        !item.getUuid().equals(TZClocksPlugin.JAGEX_CLOCK_UUID))
                .collect(Collectors.toList()); // Use stream().collect()

        final String timezonesJson = gson.toJson(itemsToSave);
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_TIMEZONES, timezonesJson);
        log.debug("Saved {} user timezones.", itemsToSave.size());

        // --- Save Tabs (User only) ---
        loadedTabsData.clear(); // Use local list for saving DTOs
        List<TZClocksTab> userTabs = plugin.getTabs(); // Get the current list of user tabs

        for (TZClocksTab tab : userTabs) {
            // Ensure we only save user tabs (redundant check if plugin.getTabs() is correct)
            if (tab.getName().equalsIgnoreCase("Game Times")) continue;

            // Filter out fixed clock UUIDs from the list within the user tab, just in case
            List<UUID> userClockIds = (tab.getClocks() == null) ? new ArrayList<>() :
                    tab.getClocks().stream()
                            .filter(id -> id != null &&
                                    !id.equals(TZClocksPlugin.LOCAL_CLOCK_UUID) &&
                                    !id.equals(TZClocksPlugin.JAGEX_CLOCK_UUID))
                            .collect(Collectors.toList());

            loadedTabsData.add(new TZClocksTabData(tab.getName(), tab.isCollapsed(), userClockIds));
        }

        final String tabsJson = gson.toJson(loadedTabsData);
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_TABS, tabsJson);
        log.debug("Saved {} user tabs.", loadedTabsData.size());
        log.debug("Data saving process complete.");
    }

    // These conversion methods are no longer needed as loading directly populates
    // the plugin's lists after filtering. They could be removed or kept if
    // there's another use case for converting between formats internally.
    /*
    private void convertItems() {
        List<TZClocksItem> watchItems = new ArrayList<>();
        for (TZClocksItem timezoneItem : loadedTimezoneItems) { // Use loaded list
            watchItems.add(new TZClocksItem(timezoneItem.getUuid(), timezoneItem.getName(), timezoneItem.getCurrentTime(), timezoneItem.getCustomName(), timezoneItem.getShowCalendar()));
        }
        plugin.setTimezones(watchItems); // This is now done directly in loadData
    }

    private void convertTabs() {
        List<TZClocksTab> watchTabs = new ArrayList<>();
        for (TZClocksTabData tabData : loadedTabsData) { // Use loaded list
            List<UUID> clockIds = (tabData.getClocks() == null) ? new ArrayList<>() : tabData.getClocks();
            watchTabs.add(new TZClocksTab(tabData.getName(), tabData.isCollapsed(), clockIds));
        }
        plugin.setTabs(watchTabs); // This is now done directly in loadData
    }
    */
}