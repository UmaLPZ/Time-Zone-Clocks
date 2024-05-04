package com.tzclocks.tzui;

import com.tzclocks.TZClocksPlugin;
import com.tzclocks.tzconfig.TZClocksConfig;
import com.tzclocks.tzdata.TZClocksItem;
import com.tzclocks.tzutilities.TZRegionEnum;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TZClocksPluginPanel extends PluginPanel {

    private final List<TZClocksItem> timezones = new ArrayList<>();
    private final List<TZClocksItemPanel> TZClocksItemPanels = new ArrayList<>();
    private final JComboBox<TZRegionEnum> regionDropdown;
    private final Map<TZClocksItem, TZClocksItemPanel> timezonePanelsMap = new HashMap<>();
    private final TZClocksPlugin plugin;
    private final TZClocksConfig config;
    private final JComboBox<String> timezoneDropdown;
    private JPanel clockPanel;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void activatePanel() {
        scheduler.scheduleAtFixedRate(this::refreshTimeDisplays, 0, 1, TimeUnit.SECONDS);
    } //makes clock(s) in panel update every second
    public TZClocksPluginPanel(TZClocksPlugin plugin, TZClocksConfig config) { //panel for the plugin. dropdowns and button
        List<String> zoneIds = new ArrayList<>(ZoneId.getAvailableZoneIds());
        Collections.sort(zoneIds);
        this.plugin = plugin;
        this.config = config;
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 0;
        regionDropdown = new JComboBox<>(); //dropdown for selecting the region
        for (TZRegionEnum region : TZRegionEnum.values()) {
            regionDropdown.addItem(region);
        }
        regionDropdown.addActionListener(e -> updateTimeZoneDropdown());
        add(regionDropdown, c);
        c.gridy++;
        timezoneDropdown = new JComboBox<>(); //dropdown for selecting the time zone
        add(timezoneDropdown, c);
        c.gridy++;
        JButton addButton = new JButton("Add Timezone");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedZoneId = (String) timezoneDropdown.getSelectedItem();
                if (selectedZoneId != null) {
                    plugin.addTimezoneToPanel(selectedZoneId);
                }
            }
        });
        add(addButton, c);
        c.gridy++;
        clockPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        clockPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        add(clockPanel, c);
        scheduler.scheduleAtFixedRate(this::refreshTimeDisplays, 0, 1, TimeUnit.SECONDS);
        updateTimeZoneDropdown();
    }

    private List<ZoneId> getTimeZoneIdsForRegion(TZRegionEnum region) { //self-explanatory. gets the time zone IDs from TZRegionEnum
        if (region == TZRegionEnum.ALL) {
            List<ZoneId> allZoneIds = new ArrayList<>();
            for (TZRegionEnum reg : TZRegionEnum.values()) {
                if (reg != TZRegionEnum.ALL) {
                    allZoneIds.addAll(reg.getZoneIds());
                }
            }
            return allZoneIds;
        } else {
            return region.getZoneIds();
        }
    }

    private void updateTimeZoneDropdown() { //self-explanatory. updates the time zone dropdown to match region
        TZRegionEnum selectedRegion = (TZRegionEnum) regionDropdown.getSelectedItem();
        List<ZoneId> zoneIds = getTimeZoneIdsForRegion(selectedRegion);

        timezoneDropdown.removeAllItems();

        for (ZoneId zoneId : zoneIds) {
            timezoneDropdown.addItem(zoneId.toString());
        }
    }

    public void addTimezonePanel(TZClocksItem item) { //adds the time zone panel
        TZClocksItemPanel TZClocksItemPanel = new TZClocksItemPanel(plugin, item);
        TZClocksItemPanels.add(TZClocksItemPanel);
        timezonePanelsMap.put(item, TZClocksItemPanel);
        clockPanel.add(TZClocksItemPanel);
        revalidate();
        repaint();
    }

    public void removeTimezonePanel(TZClocksItem item) { //removes time zone from panel
        TZClocksItemPanel timezonePanelToRemove = timezonePanelsMap.remove(item);
        if (timezonePanelToRemove != null) {
            clockPanel.remove(timezonePanelToRemove);
            TZClocksItemPanels.remove(timezonePanelToRemove);
            revalidate();
            repaint();
        }
    }

    public void refreshTimeDisplays( ) { //refreshes panel when adding new time zone
        DateTimeFormatter formatter = plugin.getFormatter();
        for (TZClocksItem item : timezones) {
            ZoneId zoneId = ZoneId.of(item.getName());
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            item.setCurrentTime(now.format(formatter));
        }
        for (TZClocksItemPanel panel : TZClocksItemPanels) {
            panel.updateTime();
        }
    }

    public void removeAllClocks() { //removes clocks to prevent re-adding previously selected clocks. there might be an alternative
        clockPanel.removeAll();
        TZClocksItemPanels.clear();
        timezonePanelsMap.clear();
        revalidate();
        repaint();
    }

}