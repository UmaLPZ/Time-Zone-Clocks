package com.tzclocks.tzui;

import com.tzclocks.TZClocksPlugin;
import com.tzclocks.tzconfig.TZClocksConfig;
import com.tzclocks.tzdata.TZClocksItem;
import com.tzclocks.tzdata.TZClocksTab;
import com.tzclocks.tzutilities.NamedZoneId;
import com.tzclocks.tzutilities.TZAbbrevEnum;
import com.tzclocks.tzutilities.TZRegionEnum;
import com.tzclocks.tzutilities.TZSourceMode;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.tzclocks.tzutilities.TZConstants.*;


public class TZClocksPluginPanel extends PluginPanel {
    private final TZClocksPlugin plugin;


    private final JComboBox<Object> groupDropdown = new JComboBox<>();
    private final JComboBox<NamedZoneId> timezoneDropdown = new JComboBox<>();
    private final JPanel clockListPanel = new JPanel();
    private final Map<TZClocksItem, TZClocksItemPanel> timezonePanelsMap = new HashMap<>();
    private final Map<TZClocksTab, TZClocksTabPanel> tabPanelsMap = new HashMap<>();
    private final GridBagConstraints clockListConstraints = new GridBagConstraints();
    private final Box.Filler glue;

    private final JPanel topPanel;
    private final JScrollPane scrollPane;
    private final JPanel southPanel;


    private static final ImageIcon COLLAPSE_ICON;
    private static final ImageIcon COLLAPSE_HOVER_ICON;
    private static final ImageIcon EXPAND_DOWN_ICON;
    private static final ImageIcon EXPAND_DOWN_HOVER_ICON;
    private static final ImageIcon EXPAND_UP_ICON;
    private static final ImageIcon EXPAND_UP_HOVER_ICON;

    static {
        final int ICON_SIZE = 12; final float ALPHA_HOVER = 0.53f;
        final BufferedImage collapseImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksPlugin.class, COLLAPSE_ICON_PATH), ICON_SIZE, ICON_SIZE);
        COLLAPSE_ICON = new ImageIcon(collapseImage); COLLAPSE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(collapseImage, ALPHA_HOVER));
        final BufferedImage expandDownImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksPlugin.class, EXPAND_ICON_PATH), ICON_SIZE, ICON_SIZE);
        EXPAND_DOWN_ICON = new ImageIcon(expandDownImage); EXPAND_DOWN_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(expandDownImage, ALPHA_HOVER));
        final BufferedImage expandUpImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksPlugin.class, ARROW_UP_ICON_PATH), ICON_SIZE, ICON_SIZE);
        EXPAND_UP_ICON = new ImageIcon(expandUpImage); EXPAND_UP_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(expandUpImage, ALPHA_HOVER));
    }


    @Inject
    public TZClocksPluginPanel(TZClocksPlugin plugin, TZClocksConfig config) {
        super(false);
        this.plugin = plugin;


        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx = 0; gbc.weightx = 1.0;


        topPanel = createTopPanel();
        gbc.gridy = 0; gbc.weighty = 0.0; add(topPanel, gbc);


        clockListPanel.setLayout(new GridBagLayout()); clockListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane = new JScrollPane(clockListPanel); scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane.setBorder(new EmptyBorder(10, 0, 10, 0)); scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        scrollPane.getVerticalScrollBar().setBorder(new EmptyBorder(0, 5, 0, 0));
        gbc.gridy = 1; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH; add(scrollPane, gbc);


        southPanel = new JPanel(new BorderLayout()); southPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        southPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        gbc.gridy = 2; gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL; add(southPanel, gbc);


        clockListConstraints.fill = GridBagConstraints.HORIZONTAL; clockListConstraints.gridwidth = 1; clockListConstraints.weightx = 1.0;
        clockListConstraints.weighty = 0.0; clockListConstraints.gridx = 0; clockListConstraints.gridy = 0; clockListConstraints.anchor = GridBagConstraints.NORTH;
        glue = (Box.Filler) Box.createVerticalGlue();


    }

    /**
     * Creates the top panel with dropdowns and add buttons.
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 0, 5));
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        panel.setBackground(ColorScheme.DARK_GRAY_COLOR);



        groupDropdown.addActionListener(e -> updateSecondDropdown());
        panel.add(groupDropdown);


        panel.add(timezoneDropdown);


        JButton addButton = new JButton("Add Timezone");
        addButton.addActionListener(e -> {
            NamedZoneId selectedNamedZone = (NamedZoneId) timezoneDropdown.getSelectedItem();
            if (selectedNamedZone != null) {
                String selectedZoneIdString = selectedNamedZone.getZoneId().toString();
                String selectedDisplayName = selectedNamedZone.getDisplayName();


                if (selectedZoneIdString.equals(TZClocksPlugin.LOCAL_ZONE_ID) || selectedZoneIdString.equals(TZClocksPlugin.JAGEX_ZONE_ID)) {
                    JOptionPane.showMessageDialog(this, selectedZoneIdString + " is already shown in the Game Times panel.", "Clock Exists", JOptionPane.INFORMATION_MESSAGE); return;
                }
                plugin.addTimezoneToPanel(selectedZoneIdString, selectedDisplayName);
            }
        });
        panel.add(addButton);


        JButton addTabButton = new JButton("Add Tab");
        addTabButton.addActionListener(e -> {
            String tabName = JOptionPane.showInputDialog(this, "Enter a name for the tab:", "New Tab", JOptionPane.PLAIN_MESSAGE);
            if (tabName != null && !tabName.trim().isEmpty()) {
                tabName = tabName.trim();
                if (tabName.equalsIgnoreCase(TZClocksPlugin.FIXED_TAB_NAME)) {
                    JOptionPane.showMessageDialog(this, "Cannot use the reserved tab name '"+TZClocksPlugin.FIXED_TAB_NAME+"'.", "Reserved Name", JOptionPane.WARNING_MESSAGE); return;
                } plugin.addTab(tabName);
            }
        });
        panel.add(addTabButton);
        return panel;
    }

    /**
     * Main method called on startup and config change to refresh both dropdowns.
     */
    public void updateDropdowns() {
        TZSourceMode currentMode = plugin.getConfig().getTimezoneSourceMode();
        populateFirstDropdown(currentMode);



        updateSecondDropdown();
    }

    /**
     * Populates the first dropdown (groupDropdown) based on the selected source mode.
     */
    private void populateFirstDropdown(TZSourceMode mode) {

        ActionListener[] listeners = groupDropdown.getActionListeners();
        for (ActionListener l : listeners) { groupDropdown.removeActionListener(l); }

        groupDropdown.removeAllItems();

        if (mode == TZSourceMode.REGIONAL) {
            for (TZRegionEnum region : TZRegionEnum.values()) {
                groupDropdown.addItem(region);
            }
        } else {
            for (TZAbbrevEnum abbrevGroup : TZAbbrevEnum.values()) {
                groupDropdown.addItem(abbrevGroup);
            }
        }


        for (ActionListener l : listeners) { groupDropdown.addActionListener(l); }


        if (groupDropdown.getItemCount() > 0) {
            groupDropdown.setSelectedIndex(0);
        }
    }


    /**
     * Updates the second dropdown (timezoneDropdown) based on the selection
     * in the first dropdown (groupDropdown) and the current source mode.
     * Renamed from updateTimeZoneDropdown.
     */
    public void updateSecondDropdown() {
        Object selectedGroup = groupDropdown.getSelectedItem();
        List<NamedZoneId> namedZoneIds = new ArrayList<>();


        if (selectedGroup instanceof TZRegionEnum) {
            namedZoneIds = getNamedZoneIdsForRegionSource((TZRegionEnum) selectedGroup);
        } else if (selectedGroup instanceof TZAbbrevEnum) {
            namedZoneIds = getNamedZoneIdsForAbbrevGroup((TZAbbrevEnum) selectedGroup);
        }



        Object previouslySelected = timezoneDropdown.getSelectedItem();

        timezoneDropdown.removeAllItems();
        for (NamedZoneId namedZone : namedZoneIds) {
            timezoneDropdown.addItem(namedZone);
        }


        if (previouslySelected instanceof NamedZoneId) {
            timezoneDropdown.setSelectedItem(previouslySelected);
        } else if (!namedZoneIds.isEmpty()){

        }
    }

    /**
     * Gets NamedZoneIds for a selected REGION, filtering out fixed zones.
     * Used when source mode is REGIONAL.
     * @param region The selected TZRegionEnum.
     * @return A sorted List<NamedZoneId>.
     */
    private List<NamedZoneId> getNamedZoneIdsForRegionSource(TZRegionEnum region) {
        List<NamedZoneId> namedZoneIdsResult = new ArrayList<>();
        if (region == TZRegionEnum.ALL) {
            HashSet<NamedZoneId> uniqueZones = new HashSet<>();
            for (TZRegionEnum reg : TZRegionEnum.values()) {
                if (reg != TZRegionEnum.ALL && reg.getNamedZoneIds() != null) {
                    uniqueZones.addAll(reg.getNamedZoneIds());
                }
            } namedZoneIdsResult.addAll(uniqueZones);
        } else if (region != null && region.getNamedZoneIds() != null) {
            namedZoneIdsResult.addAll(region.getNamedZoneIds());
        }


        namedZoneIdsResult.removeIf(namedZone ->
                namedZone.getZoneId().getId().equals(TZClocksPlugin.LOCAL_ZONE_ID) ||
                        namedZone.getZoneId().getId().equals(TZClocksPlugin.JAGEX_ZONE_ID)
        );

        Collections.sort(namedZoneIdsResult);
        return namedZoneIdsResult;
    }

    /**
     * Gets NamedZoneIds for a selected ABBREVIATION group.
     * Used when source mode is ABBREVIATION.
     * @param abbrevGroup The selected TZAbbrevEnum.
     * @return A sorted List<NamedZoneId>.
     */
    private List<NamedZoneId> getNamedZoneIdsForAbbrevGroup(TZAbbrevEnum abbrevGroup) {
        List<NamedZoneId> namedZoneIdsResult = new ArrayList<>();
        if (abbrevGroup == TZAbbrevEnum.ALL_ABBREV) {
            HashSet<NamedZoneId> uniqueZones = new HashSet<>();
            for (TZAbbrevEnum group : TZAbbrevEnum.values()) {

                if (group != TZAbbrevEnum.ALL_ABBREV && group.getNamedAbbreviationZoneIds() != null) {
                    uniqueZones.addAll(group.getNamedAbbreviationZoneIds());
                }
            }
            namedZoneIdsResult.addAll(uniqueZones);
        } else if (abbrevGroup != null && abbrevGroup.getNamedAbbreviationZoneIds() != null) {

            namedZoneIdsResult.addAll(abbrevGroup.getNamedAbbreviationZoneIds());
        }



        Collections.sort(namedZoneIdsResult);
        return namedZoneIdsResult;
    }



    public void initializeSouthPanel() {
        if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(this::initializeSouthPanel); return; }
        southPanel.removeAll();
        if (!plugin.getConfig().showFixedGameTimesTab()) { plugin.setFixedSouthTabClocksMap(new HashMap<>()); southPanel.setVisible(false); }
        else {
            southPanel.setVisible(true); boolean isCollapsed = plugin.isFixedTabCollapsed();
            JPanel headerPanel = new JPanel(new BorderLayout()); headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker()); headerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            JPanel leftActions = new JPanel(new BorderLayout(5, 0)); leftActions.setOpaque(false);
            JLabel collapseButton = new JLabel(); collapseButton.setOpaque(false);
            final ImageIcon currentIcon; final ImageIcon hoverIcon;
            if (isCollapsed) { currentIcon = EXPAND_UP_ICON; hoverIcon = EXPAND_UP_HOVER_ICON; collapseButton.setToolTipText("Expand Tab"); } else { currentIcon = COLLAPSE_ICON; hoverIcon = COLLAPSE_HOVER_ICON; collapseButton.setToolTipText("Collapse Tab"); }
            collapseButton.setIcon(currentIcon);
            collapseButton.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { plugin.switchTabExpandCollapse(TZClocksPlugin.FIXED_TAB_NAME); }
                @Override public void mouseEntered(MouseEvent e) { collapseButton.setIcon(hoverIcon); }
                @Override public void mouseExited(MouseEvent e) { collapseButton.setIcon(currentIcon); }
            });
            leftActions.add(collapseButton, BorderLayout.WEST);
            JLabel tabNameLabel = new JLabel(TZClocksPlugin.FIXED_TAB_NAME); tabNameLabel.setForeground(Color.WHITE); tabNameLabel.setToolTipText(TZClocksPlugin.FIXED_TAB_NAME);
            leftActions.add(tabNameLabel, BorderLayout.CENTER); headerPanel.add(leftActions, BorderLayout.WEST);

            Map<TZClocksItem, TZClocksTabItemPanel> localFixedMap = new HashMap<>();
            if (!isCollapsed) {
                JPanel itemsPanel = new JPanel(new GridBagLayout()); itemsPanel.setBorder(new EmptyBorder(5, 5, 5, 5)); itemsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR); itemsPanel.setOpaque(true);
                GridBagConstraints itemGBC = new GridBagConstraints(); itemGBC.fill = GridBagConstraints.HORIZONTAL; itemGBC.gridwidth = 1; itemGBC.weightx = 1.0; itemGBC.weighty = 0.0; itemGBC.gridx = 0; itemGBC.gridy = 0; itemGBC.anchor = GridBagConstraints.NORTH;
                List<UUID> fixedClockIds = List.of(TZClocksPlugin.LOCAL_CLOCK_UUID, TZClocksPlugin.JAGEX_CLOCK_UUID); int itemIndex = 0;
                for (UUID clockId : fixedClockIds) {
                    Optional<TZClocksItem> clockOpt = plugin.getTimezones().stream().filter(c -> c.getUuid().equals(clockId)).findFirst();
                    if (clockOpt.isPresent()) {
                        TZClocksItem clock = clockOpt.get(); TZClocksTabItemPanel itemPanel = new TZClocksTabItemPanel(plugin, clock); localFixedMap.put(clock, itemPanel);
                        JPanel wrapper = new JPanel(new BorderLayout()); wrapper.setOpaque(false);
                        if (itemIndex > 0) { wrapper.setBorder(new EmptyBorder(5, 0, 0, 0)); } else { wrapper.setBorder(new EmptyBorder(0,0,0,0)); }
                        wrapper.add(itemPanel, BorderLayout.NORTH); itemsPanel.add(wrapper, itemGBC); itemGBC.gridy++; itemIndex++;
                    }
                }
                GridBagConstraints glueGBC = new GridBagConstraints(); glueGBC.gridx = 0; glueGBC.gridy = itemGBC.gridy; glueGBC.weighty = 1.0; glueGBC.fill = GridBagConstraints.VERTICAL; itemsPanel.add(Box.createVerticalGlue(), glueGBC);
                southPanel.add(itemsPanel, BorderLayout.CENTER);
            }
            southPanel.add(headerPanel, BorderLayout.SOUTH); plugin.setFixedSouthTabClocksMap(localFixedMap);
        }
        southPanel.revalidate(); southPanel.repaint(); if (getParent() != null) { getParent().validate(); }
    }


    public void addTimezonePanel(TZClocksItem item) { updatePanel(); }
    public void removeTimezonePanel(TZClocksItem item) { timezonePanelsMap.remove(item); updatePanel(); }
    public void addTabPanel(TZClocksTab tab) { updatePanel(); }
    private void updateClockListPanel() { clockListPanel.revalidate(); clockListPanel.repaint(); }
    public Map<TZClocksItem, TZClocksItemPanel> getTimezonePanelsMap() { return timezonePanelsMap; }
    public Map<TZClocksTab, TZClocksTabPanel> getTabPanelsMap() { return tabPanelsMap; }
    public void updatePanel() {
        if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(this::updatePanel); return; }
        timezonePanelsMap.clear(); tabPanelsMap.clear(); clockListPanel.removeAll();
        clockListConstraints.gridy = 0;
        for (TZClocksTab tab : plugin.getTabs()) { TZClocksTabPanel tabPanel = new TZClocksTabPanel(plugin, this, tab); tabPanelsMap.put(tab, tabPanel); JPanel wrapper = createMarginWrapper(tabPanel); clockListPanel.add(wrapper, clockListConstraints); clockListConstraints.gridy++; }
        for (TZClocksItem clock : plugin.getTimezones()) { if (clock.getUuid().equals(TZClocksPlugin.LOCAL_CLOCK_UUID) || clock.getUuid().equals(TZClocksPlugin.JAGEX_CLOCK_UUID)) { continue; } boolean isInUserTab = plugin.getTabs().stream().anyMatch(t -> t.getClocks() != null && t.getClocks().contains(clock.getUuid())); if (!isInUserTab) { TZClocksItemPanel clockPanel = new TZClocksItemPanel(plugin, clock); timezonePanelsMap.put(clock, clockPanel); JPanel wrapper = createMarginWrapper(clockPanel); clockListPanel.add(wrapper, clockListConstraints); clockListConstraints.gridy++; } }
        GridBagConstraints glueConstraints = new GridBagConstraints(); glueConstraints.gridx = 0; glueConstraints.gridy = clockListConstraints.gridy; glueConstraints.weighty = 1.0; glueConstraints.fill = GridBagConstraints.VERTICAL; clockListPanel.add(glue, glueConstraints);
        updateClockListPanel();
    }


    public TZClocksPlugin getPlugin() { return plugin; }
    public List<TZClocksItem> getAvailableClocks() {
        List<TZClocksItem> availableClocks = plugin.getTimezones().stream().filter(clock -> !clock.getUuid().equals(TZClocksPlugin.LOCAL_CLOCK_UUID) && !clock.getUuid().equals(TZClocksPlugin.JAGEX_CLOCK_UUID)).collect(Collectors.toList());
        for (TZClocksTab userTab : plugin.getTabs()) { if (userTab.getClocks() != null) { userTab.getClocks().forEach(id -> availableClocks.removeIf(c -> c.getUuid().equals(id))); } } return availableClocks;
    }
    public JPanel createMarginWrapper(JPanel contentPanel) {
        JPanel marginWrapper = new JPanel(new BorderLayout()); marginWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR); boolean isFirstItem = (clockListConstraints.gridy == 0);
        marginWrapper.setBorder(isFirstItem ? new EmptyBorder(0,0,0,0) : new EmptyBorder(5,0,0,0)); marginWrapper.add(contentPanel, BorderLayout.NORTH); return marginWrapper;
    }
}