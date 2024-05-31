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

    private final List<TZClocksItemPanel> TZClocksItemPanels = new ArrayList<>();
    private final JComboBox<TZRegionEnum> regionDropdown;
    private final Map<TZClocksItem, TZClocksItemPanel> timezonePanelsMap = new HashMap<>();
    private final TZClocksPlugin plugin;
    private final TZClocksConfig config;
    private final JComboBox<String> timezoneDropdown;
    private JPanel clockPanel;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void activatePanel() {
        scheduler.scheduleAtFixedRate(this::refreshTimeDisplays, 0, 1, TimeUnit.SECONDS); //makes clock(s) in panel update every second
    }
    public TZClocksPluginPanel(TZClocksPlugin plugin, TZClocksConfig config) { //panel for the plugin. dropdowns and button
        List<String> zoneIds = new ArrayList<>(ZoneId.getAvailableZoneIds());
        Collections.sort(zoneIds);
        this.plugin = plugin;
        this.config = config;
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        topPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        regionDropdown = new JComboBox<>(); //dropdown for selecting the region
        for (TZRegionEnum region : TZRegionEnum.values()) {
            regionDropdown.addItem(region);
        }
        regionDropdown.addActionListener(e -> updateTimeZoneDropdown());
        topPanel.add(regionDropdown);

        timezoneDropdown = new JComboBox<>(); //dropdown for selecting the time zone
        topPanel.add(timezoneDropdown);

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
        topPanel.add(addButton);

        add(topPanel, BorderLayout.NORTH);

        clockPanel = new JPanel(new GridBagLayout());
        clockPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(clockPanel, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        scrollPane.getVerticalScrollBar().setBorder(new EmptyBorder(5, 5, 0, 0));
        add(scrollPane, BorderLayout.CENTER);

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

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = timezonePanelsMap.size() - 1;

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        containerPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
        containerPanel.add(TZClocksItemPanel, BorderLayout.CENTER); //

        clockPanel.add(containerPanel, gbc); // Add the container panel to the clock panel
        revalidate();
        repaint();
    }

    public void removeTimezonePanel(TZClocksItem item) { //removes time zone from panel
        TZClocksItemPanel panelToRemove = timezonePanelsMap.remove(item);
        if (panelToRemove != null) {
            for (Component component : clockPanel.getComponents()) {
                if (component instanceof JPanel && ((JPanel) component).getComponent(0) == panelToRemove) {
                    clockPanel.remove(component);
                    break;
                }
            }

            TZClocksItemPanels.remove(panelToRemove);
            revalidate();
            repaint();
        }
    }

    public void refreshTimeDisplays() { //refreshes panel when adding new time zone
        DateTimeFormatter formatter = plugin.getFormatter();
        for (TZClocksItem item : plugin.getTimezones()) { // Get timezones from the plugin
            ZoneId zoneId = ZoneId.of(item.getName());
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            String currentTime = now.format(formatter);
            item.setCurrentTime(currentTime);

            // Update the corresponding panel directly
            TZClocksItemPanel panel = timezonePanelsMap.get(item);
            if (panel != null) {
                panel.updateTime();
            }
        }
    }

    public void removeAllClocks() { //removes clocks to prevent re-adding previously selected clocks. there might be an alternative
        clockPanel.removeAll();
        TZClocksItemPanels.clear();
        timezonePanelsMap.clear();
        revalidate();
        repaint();
    }

    public Map<TZClocksItem, TZClocksItemPanel> getTimezonePanelsMap() {
        return timezonePanelsMap;
    }
}