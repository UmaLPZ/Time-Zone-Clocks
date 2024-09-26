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

@Slf4j
public class TZClocksDataManager {
    private static final String CONFIG_KEY_TIMEZONES = "timezonekey";
    private static final String CONFIG_KEY_TABS = "tabsKey";
    private static final String CONFIG_GROUP = "timezonesgroup";
    private static final String LOAD_TIMEZONE_ERROR = "Exception occurred while loading timezones";
    private static final String EMPTY_ARRAY = "[]";

    private final TZClocksPlugin plugin;
    private final Client client;
    private final ConfigManager configManager;
    private final Gson gson;

    private List<TZClocksItem> timezoneItems = new ArrayList<>();
    private List<TZClocksTabData> tabsData = new ArrayList<>();
    private final Type timezoneItemsType = new TypeToken<ArrayList<TZClocksItem>>() {}.getType();
    private final Type tabsDataType = new TypeToken<ArrayList<TZClocksTabData>>() {}.getType();

    @Inject
    public TZClocksDataManager(TZClocksPlugin plugin, Client client, ConfigManager configManager, Gson gson) {
        this.plugin = plugin;
        this.client = client;
        this.configManager = configManager;
        this.gson = gson;
    }

    public boolean loadData() {
        if (client.getGameState().getState() < GameState.LOGIN_SCREEN.getState()) {
            return false;
        }

        timezoneItems.clear();
        tabsData.clear();

        String timezonesJson = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_TIMEZONES);
        if (timezonesJson == null || timezonesJson.equals(EMPTY_ARRAY)) {
            plugin.setTimezones(new ArrayList<>());
        } else {
            try {
                if (timezonesJson.contains("\"uuid\":")) {
                    timezoneItems = gson.fromJson(timezonesJson, timezoneItemsType);
                } else {
                    Type oldItemsType = new TypeToken<ArrayList<String>>() {}.getType();
                    List<String> oldTimezoneIds = gson.fromJson(timezonesJson, oldItemsType);
                    for (String timezoneId : oldTimezoneIds) {
                        timezoneItems.add(new TZClocksItem(UUID.randomUUID(), timezoneId, null, null, null));
                    }
                }
                convertItems();
            } catch (Exception e) {
                log.error(LOAD_TIMEZONE_ERROR, e);
                plugin.setTimezones(new ArrayList<>());
            }
        }

        String tabsJson = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_TABS);
        if (tabsJson != null && !tabsJson.equals(EMPTY_ARRAY)) {
            try {
                tabsData = gson.fromJson(tabsJson, tabsDataType);
                convertTabs();
            } catch (Exception e) {
                log.error("Error loading tabs:", e);
            }
        }

        return true;
    }

    public void saveData() {
        List<TZClocksItem> tempTimezoneItems = new ArrayList<>(plugin.getTimezones());
        timezoneItems = tempTimezoneItems;
        final String timezonesJson = gson.toJson(timezoneItems);
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_TIMEZONES, timezonesJson);

        tabsData.clear();
        for (TZClocksTab tab : plugin.getTabs()) {
            List<UUID> tabClockIds = new ArrayList<>();
            for (TZClocksItem item : plugin.getTimezones()) {
                if (tab.getClocks().contains(item.getUuid())) {
                    tabClockIds.add(item.getUuid());
                }
            }
            tabsData.add(new TZClocksTabData(tab.getName(), tab.isCollapsed(), tabClockIds));
        }
        final String tabsJson = gson.toJson(tabsData);
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_TABS, tabsJson);
    }

    private void convertItems() {
        List<TZClocksItem> watchItems = new ArrayList<>();
        for (TZClocksItem timezoneItem : timezoneItems) {
            watchItems.add(new TZClocksItem(timezoneItem.getUuid(), timezoneItem.getName(), timezoneItem.getCurrentTime(), timezoneItem.getCustomName(), timezoneItem.getShowCalendar()));
        }
        plugin.setTimezones(watchItems);
    }

    private void convertTabs() {
        List<TZClocksTab> watchTabs = new ArrayList<>();
        for (TZClocksTabData tabData : tabsData) {
            List<UUID> clockIds = tabData.getClocks();
            watchTabs.add(new TZClocksTab(tabData.getName(), tabData.isCollapsed(), clockIds));
        }
        plugin.setTabs(watchTabs);
    }
}