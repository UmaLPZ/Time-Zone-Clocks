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
    private final JComboBox<TZRegionEnum> regionDropdown;
    private final JComboBox<String> timezoneDropdown;
    private JPanel clockListPanel; // Panel to hold the list of clocks and tabs
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final TZClocksPlugin plugin;
    private final Map<TZClocksItem, TZClocksItemPanel> timezonePanelsMap = new HashMap<>();
    private final Map<TZClocksTab, TZClocksTabPanel> tabPanelsMap = new HashMap<>();

    public TZClocksPluginPanel(TZClocksPlugin plugin, TZClocksConfig config) {
        List<String> zoneIds = new ArrayList<>(ZoneId.getAvailableZoneIds());
        Collections.sort(zoneIds);
        this.plugin = plugin;
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JPanel topPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        topPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        regionDropdown = new JComboBox<>();
        for (TZRegionEnum region : TZRegionEnum.values()) {
            regionDropdown.addItem(region);
        }
        regionDropdown.addActionListener(e -> updateTimeZoneDropdown());
        topPanel.add(regionDropdown);

        timezoneDropdown = new JComboBox<>();
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

        JButton addTabButton = new JButton("Add Tab");
        addTabButton.addActionListener(e -> {
            String tabName = JOptionPane.showInputDialog(TZClocksPluginPanel.this,
                    "Enter a name for the tab:", "New Tab", JOptionPane.PLAIN_MESSAGE);
            if (tabName != null && !tabName.trim().isEmpty()) {
                plugin.addTab(tabName);
            }
        });
        topPanel.add(addTabButton);

        add(topPanel, gbc);

        gbc.gridy++;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        clockListPanel = new JPanel(new GridBagLayout());
        clockListPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        JScrollPane scrollPane = new JScrollPane(clockListPanel);
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        scrollPane.getVerticalScrollBar().setBorder(new EmptyBorder(5, 5, 0, 0));
        add(scrollPane, gbc);

        scheduler.scheduleAtFixedRate(this::refreshTimeDisplays, 0, 1, TimeUnit.SECONDS);
        updateTimeZoneDropdown();
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
            // Remove from its current parent (either main panel or a tab)
            Component parent = panelToRemove.getParent();
            if (parent != null) {
                ((Container) parent).remove(panelToRemove.getParent()); // Cast parent to Container
                parent.revalidate();
                parent.repaint();
            }

            // Also remove from the main clockListPanel if it's there
            for (Component c : clockListPanel.getComponents()) {
                if (c instanceof JPanel && ((JPanel) c).getComponent(0) == panelToRemove) {
                    clockListPanel.remove(c);
                    clockListPanel.revalidate();
                    clockListPanel.repaint();
                    break;
                }
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
        TZClocksTabPanel tabPanel = new TZClocksTabPanel(plugin, tab);
        tabPanelsMap.put(tab, tabPanel); // Store the tab panel

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
        TZClocksTabPanel tabPanel = tabPanelsMap.remove(tab); // Remove the tab panel from the map
        if (tabPanel != null) {
            clockListPanel.remove(tabPanel); // Remove from the UI
            clockListPanel.revalidate();
            clockListPanel.repaint();
        }
    }

    public Map<TZClocksItem, TZClocksItemPanel> getTimezonePanelsMap() {
        return timezonePanelsMap;
    }

    public Map<TZClocksTab, TZClocksTabPanel> getTabPanelsMap() { // Added getTabPanelsMap() method
        return tabPanelsMap;
    }
}