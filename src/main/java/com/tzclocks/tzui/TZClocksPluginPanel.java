package com.tzclocks.tzui;

import com.tzclocks.TZClocksPlugin;
import com.tzclocks.tzconfig.TZClocksConfig;
import com.tzclocks.tzdata.TZClocksItem;
import com.tzclocks.tzdata.TZClocksTab;
import com.tzclocks.tzutilities.TZRegionEnum;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TZClocksPluginPanel extends PluginPanel {

    private final TZClocksPlugin plugin;
    private final TZClocksConfig config;

    private final JComboBox<TZRegionEnum> regionDropdown = new JComboBox<>(); // Initialize here
    private final JComboBox<String> timezoneDropdown = new JComboBox<>(); // Initialize here
    private final JPanel clockListPanel;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<TZClocksItem, TZClocksItemPanel> timezonePanelsMap = new HashMap<>();
    private final Map<TZClocksTab, TZClocksTabPanel> tabPanelsMap = new HashMap<>();


    public TZClocksPluginPanel(TZClocksPlugin plugin, TZClocksConfig config) {
        this.plugin = plugin;
        this.config = config;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Create the top panel for controls (region and timezone dropdowns, buttons)
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Create the panel to hold clocks and tabs
        clockListPanel = new JPanel(new GridBagLayout());
        clockListPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Wrap clockListPanel in a JScrollPane for scrolling
        JScrollPane scrollPane = new JScrollPane(clockListPanel);
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        scrollPane.getVerticalScrollBar().setBorder(new EmptyBorder(5, 5, 0, 0));
        add(scrollPane, BorderLayout.CENTER);

        scheduler.scheduleAtFixedRate(this::refreshTimeDisplays, 0, 1, TimeUnit.SECONDS);
        updateTimeZoneDropdown();
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0)); // Add some bottom margin

        // Region Dropdown
        for (TZRegionEnum region : TZRegionEnum.values()) {
            regionDropdown.addItem(region);
        }
        regionDropdown.addActionListener(e -> updateTimeZoneDropdown());
        topPanel.add(regionDropdown);

        // Timezone Dropdown
        topPanel.add(timezoneDropdown);

        // Add Timezone Button
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

        // Add Tab Button
        JButton addTabButton = new JButton("Add Tab");
        addTabButton.addActionListener(e -> {
            String tabName = JOptionPane.showInputDialog(TZClocksPluginPanel.this,
                    "Enter a name for the tab:", "New Tab", JOptionPane.PLAIN_MESSAGE);
            if (tabName != null && !tabName.trim().isEmpty()) {
                plugin.addTab(tabName);
            }
        });
        topPanel.add(addTabButton);

        return topPanel;
    }


    private List<ZoneId> getTimeZoneIdsForRegion(TZRegionEnum region) {
        if (region == TZRegionEnum.ALL) {
            List<ZoneId> allZoneIds = new ArrayList<>();
            for (TZRegionEnum reg : TZRegionEnum.values()) {
                if (reg != TZRegionEnum.ALL && reg != TZRegionEnum.SPECIAL_TIMES) { // Exclude SPECIAL_TIMES from ALL
                    allZoneIds.addAll(reg.getZoneIds());
                }
            }
            return allZoneIds;
        } else if (region == TZRegionEnum.SPECIAL_TIMES) {
            return region.getZoneIds();
        } else {
            return region.getZoneIds();
        }
    }

    private void updateTimeZoneDropdown() {
        TZRegionEnum selectedRegion = (TZRegionEnum) regionDropdown.getSelectedItem();
        List<ZoneId> zoneIds = getTimeZoneIdsForRegion(selectedRegion);

        timezoneDropdown.removeAllItems();

        if (selectedRegion == TZRegionEnum.SPECIAL_TIMES) {
            timezoneDropdown.addItem("Local Time#" + ZoneId.systemDefault());
            timezoneDropdown.addItem("Jagex Time#Europe/London");
        } else {
            for (ZoneId zoneId : zoneIds) {
                timezoneDropdown.addItem(zoneId.toString());
            }
        }
    }

    public void addTimezonePanel(TZClocksItem item) {
        TZClocksItemPanel timeZonePanel = new TZClocksItemPanel(plugin, item);
        timezonePanelsMap.put(item, timeZonePanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = clockListPanel.getComponentCount();

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        containerPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
        containerPanel.add(timeZonePanel, BorderLayout.CENTER);

        clockListPanel.add(containerPanel, gbc);
        clockListPanel.revalidate();
        clockListPanel.repaint();
    }

    public void removeTimezonePanel(TZClocksItem item) {
        TZClocksItemPanel panelToRemove = timezonePanelsMap.get(item);
        if (panelToRemove != null) {
            Component parent = panelToRemove.getParent();
            if (parent != null) {
                ((Container) parent).remove(panelToRemove.getParent());
                parent.revalidate();
                parent.repaint();
            }

            timezonePanelsMap.remove(item);
        }
    }

    public void refreshTimeDisplays() {
        for (TZClocksItem item : plugin.getTimezones()) {
            TZClocksItemPanel panel = timezonePanelsMap.get(item);
            if (panel != null) {
                panel.updateTime();
            }
        }
    }

    public void removeAllClocks() {
        clockListPanel.removeAll();
        timezonePanelsMap.clear();
        revalidate();
        repaint();
    }

    public void addTabPanel(TZClocksTab tab) {
        TZClocksTabPanel tabPanel = new TZClocksTabPanel(plugin, this, tab);
        tabPanelsMap.put(tab, tabPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = clockListPanel.getComponentCount();

        clockListPanel.add(tabPanel, gbc);
        clockListPanel.revalidate();
        clockListPanel.repaint();
    }

    public void removeTabPanel(TZClocksTab tab) {
        TZClocksTabPanel tabPanel = tabPanelsMap.remove(tab);
        if (tabPanel != null) {
            clockListPanel.remove(tabPanel);
            clockListPanel.revalidate();
            clockListPanel.repaint();
        }
    }

    public Map<TZClocksItem, TZClocksItemPanel> getTimezonePanelsMap() {
        return timezonePanelsMap;
    }

    public Map<TZClocksTab, TZClocksTabPanel> getTabPanelsMap() {
        return tabPanelsMap;
    }

    // Method to refresh all tabs (used after adding clocks to tabs)
    public void refreshAllTabs() {
        for (TZClocksTabPanel tabPanel : tabPanelsMap.values()) {
            if (!tabPanel.getTab().isCollapsed()) { // Only refresh expanded tabs
                tabPanel.toggleTabCollapse();
                tabPanel.toggleTabCollapse();
            }
        }
        clockListPanel.revalidate(); // Revalidate the main clock list panel
        clockListPanel.repaint();  // Repaint the main clock list panel
    }

    public List<TZClocksItem> getAvailableClocks() {
        List<TZClocksItem> availableClocks = new ArrayList<>(plugin.getTimezones());
        for (TZClocksTab t : plugin.getTabs()) {
            availableClocks.removeIf(clock -> t.getClocks().contains(clock.getUuid()));
        }
        return availableClocks;
    }
}