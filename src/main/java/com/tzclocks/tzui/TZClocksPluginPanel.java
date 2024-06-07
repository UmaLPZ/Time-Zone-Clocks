package com.tzclocks.tzui;

import com.tzclocks.TZClocksPlugin;
import com.tzclocks.tzconfig.TZClocksConfig;
import com.tzclocks.tzdata.TZClocksItem;
import com.tzclocks.tzdata.TZClocksTab;
import com.tzclocks.tzutilities.TZRegionEnum;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
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

public class TZClocksPluginPanel extends PluginPanel {
    private final TZClocksPlugin plugin;
    private final TZClocksConfig config;

    private final JComboBox<TZRegionEnum> regionDropdown = new JComboBox<>();
    private final JComboBox<String> timezoneDropdown = new JComboBox<>();
    private final JPanel clockListPanel;
    private final Map<TZClocksItem, TZClocksItemPanel> timezonePanelsMap = new HashMap<>();
    private final Map<TZClocksTab, TZClocksTabPanel> tabPanelsMap = new HashMap<>();
    private final GridBagConstraints constraints = new GridBagConstraints();

    @Inject
    public TZClocksPluginPanel(TZClocksPlugin plugin, TZClocksConfig config) {
        this.plugin = plugin;
        this.config = config;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        clockListPanel = new JPanel(new GridBagLayout());
        clockListPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JPanel pWrapper = new JPanel(new BorderLayout());
        pWrapper.add(clockListPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(pWrapper);
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane.setBorder(new EmptyBorder(5, 0, 0, 0));
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        scrollPane.getVerticalScrollBar().setBorder(new EmptyBorder(5, 5, 0, 0));

        add(scrollPane, BorderLayout.CENTER);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 1;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        updateTimeZoneDropdown();
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        for (TZRegionEnum region : TZRegionEnum.values()) {
            regionDropdown.addItem(region);
        }
        regionDropdown.addActionListener(e -> updateTimeZoneDropdown());
        topPanel.add(regionDropdown);

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

        return topPanel;
    }

    private List<ZoneId> getTimeZoneIdsForRegion(TZRegionEnum region) {
        if (region == TZRegionEnum.ALL) {
            List<ZoneId> allZoneIds = new ArrayList<>();
            for (TZRegionEnum reg : TZRegionEnum.values()) {
                if (reg != TZRegionEnum.ALL && reg != TZRegionEnum.SPECIAL_TIMES) {
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
        TZClocksItemPanel clockPanel = new TZClocksItemPanel(plugin, item);
        timezonePanelsMap.put(item, clockPanel);

        if (constraints.gridy > 0) {
            clockListPanel.add(createMarginWrapper(clockPanel), constraints);
        } else {
            clockListPanel.add(clockPanel, constraints);
        }

        constraints.gridy++;
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

    public void removeAllClocks() {
        clockListPanel.removeAll();
        timezonePanelsMap.clear();
        revalidate();
        repaint();
    }

    public void addTabPanel(TZClocksTab tab) {
        TZClocksTabPanel tabPanel = new TZClocksTabPanel(plugin, this, tab);
        tabPanelsMap.put(tab, tabPanel);

        if (constraints.gridy > 0) {
            clockListPanel.add(createMarginWrapper(tabPanel), constraints);
        } else {
            clockListPanel.add(tabPanel, constraints);
        }

        constraints.gridy++;
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

    public void updatePanel() {
        clockListPanel.removeAll();

        constraints.gridy = 0;

        for (TZClocksTab tab : plugin.getTabs()) {
            addTabPanel(tab);
        }

        for (TZClocksItem clock : plugin.getTimezones()) {
            boolean isInTab = plugin.getTabs().stream()
                    .anyMatch(tab -> tab.getClocks().contains(clock.getUuid()));
            if (!isInTab) {
                addTimezonePanel(clock);
            }
        }
        clockListPanel.revalidate();
        clockListPanel.repaint();
    }

    public TZClocksPlugin getPlugin() {
        return plugin;
    }

    public List<TZClocksItem> getAvailableClocks() { // Method is in TZClocksPluginPanel.java
        List<TZClocksItem> availableClocks = new ArrayList<>(plugin.getTimezones());
        for (TZClocksTab t : plugin.getTabs()) {
            availableClocks.removeIf(clock -> t.getClocks().contains(clock.getUuid()));
        }
        return availableClocks;
    }

    private JPanel createMarginWrapper(JPanel panel) {
        JPanel marginWrapper = new JPanel(new BorderLayout());
        marginWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
        marginWrapper.add(panel, BorderLayout.NORTH);
        return marginWrapper;
    }
}