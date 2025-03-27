package com.tzclocks.tzui;

import com.tzclocks.TZClocksPlugin;
import com.tzclocks.tzconfig.TZClocksConfig;
import com.tzclocks.tzdata.TZClocksItem;
import com.tzclocks.tzdata.TZClocksTab;
import com.tzclocks.tzutilities.TZRegionEnum;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
// Import necessary icon constants
import static com.tzclocks.tzutilities.TZConstants.*;


public class TZClocksPluginPanel extends PluginPanel {
    private final TZClocksPlugin plugin;

    // UI Components for the top control section
    private final JComboBox<TZRegionEnum> regionDropdown = new JComboBox<>();
    private final JComboBox<String> timezoneDropdown = new JComboBox<>();
    // UI Components for the main list area (user clocks/tabs)
    private final JPanel clockListPanel = new JPanel();
    private final Map<TZClocksItem, TZClocksItemPanel> timezonePanelsMap = new HashMap<>(); // Standalone user clocks
    private final Map<TZClocksTab, TZClocksTabPanel> tabPanelsMap = new HashMap<>(); // User tabs
    private final GridBagConstraints clockListConstraints = new GridBagConstraints(); // Constraints for clockListPanel items
    private final Box.Filler glue; // Pushes user items up

    // Main layout panels
    private final JPanel topPanel;
    private final JScrollPane scrollPane;
    private final JPanel southPanel; // Container for fixed tab elements

    // Icons needed for headers
    private static final ImageIcon COLLAPSE_ICON;       // Right Arrow (shown when EXPANDED) -> Collapses
    private static final ImageIcon COLLAPSE_HOVER_ICON;
    private static final ImageIcon EXPAND_DOWN_ICON;    // Down Arrow (shown when USER tab COLLAPSED) -> Expands Down
    private static final ImageIcon EXPAND_DOWN_HOVER_ICON;
    private static final ImageIcon EXPAND_UP_ICON;      // Up Arrow (shown when FIXED tab COLLAPSED) -> Expands Up
    private static final ImageIcon EXPAND_UP_HOVER_ICON;

    static {
        // Static initializer for icons
        final int ICON_SIZE = 12;
        final float ALPHA_HOVER = 0.53f;

        // Right Arrow -> Collapse
        final BufferedImage collapseImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksPlugin.class, COLLAPSE_ICON_PATH), ICON_SIZE, ICON_SIZE);
        COLLAPSE_ICON = new ImageIcon(collapseImage);
        COLLAPSE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(collapseImage, ALPHA_HOVER));

        // Down Arrow -> Expand Down (User Tabs)
        final BufferedImage expandDownImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksPlugin.class, EXPAND_ICON_PATH), ICON_SIZE, ICON_SIZE);
        EXPAND_DOWN_ICON = new ImageIcon(expandDownImage);
        EXPAND_DOWN_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(expandDownImage, ALPHA_HOVER));

        // Up Arrow -> Expand Up (Fixed Tab)
        final BufferedImage expandUpImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksPlugin.class, ARROW_UP_ICON_PATH), ICON_SIZE, ICON_SIZE);
        EXPAND_UP_ICON = new ImageIcon(expandUpImage);
        EXPAND_UP_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(expandUpImage, ALPHA_HOVER));
    }


    @Inject
    public TZClocksPluginPanel(TZClocksPlugin plugin, TZClocksConfig config) { // config is injected but not stored if unused
        super(false); // Prevent default scrolling
        this.plugin = plugin;

        // Main panel uses GridBagLayout for NORTH, CENTER (stretchy), SOUTH structure
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        GridBagConstraints gbc = new GridBagConstraints(); // Constraints for main GBL
        gbc.fill = GridBagConstraints.HORIZONTAL; // Default fill horizontally
        gbc.gridx = 0; gbc.weightx = 1.0; // Components take full width

        // --- NORTH Panel (Controls) ---
        topPanel = createTopPanel();
        gbc.gridy = 0; gbc.weighty = 0.0; // Don't stretch vertically
        add(topPanel, gbc);

        // --- CENTER Panel (Scrollable User List) ---
        clockListPanel.setLayout(new GridBagLayout());
        clockListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane = new JScrollPane(clockListPanel);
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane.setBorder(new EmptyBorder(10, 0, 10, 0)); // Vertical spacing
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        scrollPane.getVerticalScrollBar().setBorder(new EmptyBorder(0, 5, 0, 0));
        gbc.gridy = 1; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH; // Stretch V+H
        add(scrollPane, gbc);

        // --- SOUTH Panel (Fixed Tab Container) ---
        southPanel = new JPanel(new BorderLayout()); // Holds header (SOUTH) and items (CENTER)
        southPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        southPanel.setBorder(new EmptyBorder(5, 0, 0, 0)); // Top margin
        gbc.gridy = 2; gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL; // Don't stretch V
        add(southPanel, gbc);

        // --- Constraints for items inside clockListPanel ---
        clockListConstraints.fill = GridBagConstraints.HORIZONTAL;
        clockListConstraints.gridwidth = 1; clockListConstraints.weightx = 1.0;
        clockListConstraints.weighty = 0.0; clockListConstraints.gridx = 0;
        clockListConstraints.gridy = 0; clockListConstraints.anchor = GridBagConstraints.NORTH;

        // --- Glue for clockListPanel ---
        glue = (Box.Filler) Box.createVerticalGlue();

        // Initial build calls are done in plugin.startUp after injection
    }

    /**
     * Creates the top panel with dropdowns and add buttons.
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 0, 5));
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        panel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Region Dropdown
        for (TZRegionEnum region : TZRegionEnum.values()) { regionDropdown.addItem(region); }
        regionDropdown.addActionListener(e -> updateTimeZoneDropdown());
        panel.add(regionDropdown);

        // Timezone Dropdown
        panel.add(timezoneDropdown);

        // Add Timezone Button
        JButton addButton = new JButton("Add Timezone");
        addButton.addActionListener(e -> {
            String selectedZoneIdStr = (String) timezoneDropdown.getSelectedItem();
            if (selectedZoneIdStr != null) {
                // Prevent adding fixed clocks again
                if (selectedZoneIdStr.equals(TZClocksPlugin.LOCAL_ZONE_ID) || selectedZoneIdStr.equals(TZClocksPlugin.JAGEX_ZONE_ID)) {
                    JOptionPane.showMessageDialog(this, selectedZoneIdStr + " is already shown in the Game Times panel.", "Clock Exists", JOptionPane.INFORMATION_MESSAGE); return;
                } plugin.addTimezoneToPanel(selectedZoneIdStr);
            }
        });
        panel.add(addButton);

        // Add Tab Button
        JButton addTabButton = new JButton("Add Tab");
        addTabButton.addActionListener(e -> {
            String tabName = JOptionPane.showInputDialog(this, "Enter a name for the tab:", "New Tab", JOptionPane.PLAIN_MESSAGE);
            if (tabName != null && !tabName.trim().isEmpty()) {
                tabName = tabName.trim();
                // Prevent reserved name conflict
                if (tabName.equalsIgnoreCase(TZClocksPlugin.FIXED_TAB_NAME)) {
                    JOptionPane.showMessageDialog(this, "Cannot use the reserved tab name '"+TZClocksPlugin.FIXED_TAB_NAME+"'.", "Reserved Name", JOptionPane.WARNING_MESSAGE); return;
                } plugin.addTab(tabName);
            }
        });
        panel.add(addTabButton);
        return panel;
    }

    /**
     * Builds or clears the fixed "Game Times" tab UI in the south panel
     * based on the plugin's configuration setting. Header is SOUTH, Items are CENTER.
     */
    public void initializeSouthPanel() {
        // Ensure running on Event Dispatch Thread
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::initializeSouthPanel); return;
        }

        southPanel.removeAll(); // Clear previous contents (header, items)

        // *** Check Config Setting ***
        if (!plugin.getConfig().showFixedGameTimesTab()) {
            plugin.setFixedSouthTabClocksMap(new HashMap<>()); // Ensure map is empty if hidden
            southPanel.setVisible(false); // Hide the entire south panel container
        } else {
            southPanel.setVisible(true); // Ensure container is visible if config is true
            boolean isCollapsed = plugin.isFixedTabCollapsed(); // Get current collapsed state from plugin

            // --- Manually Create Header Panel ---
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
            headerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            JPanel leftActions = new JPanel(new BorderLayout(5, 0));
            leftActions.setOpaque(false);
            JLabel collapseButton = new JLabel();
            collapseButton.setOpaque(false);

            // Use corrected icon logic for fixed tab header
            final ImageIcon currentIcon;
            final ImageIcon hoverIcon;
            if (isCollapsed) {
                currentIcon = EXPAND_UP_ICON; // Show UP arrow when collapsed
                hoverIcon = EXPAND_UP_HOVER_ICON;
                collapseButton.setToolTipText("Expand Tab");
            } else {
                currentIcon = COLLAPSE_ICON; // Show RIGHT arrow when expanded
                hoverIcon = COLLAPSE_HOVER_ICON;
                collapseButton.setToolTipText("Collapse Tab");
            }
            collapseButton.setIcon(currentIcon);
            collapseButton.addMouseListener(new MouseAdapter() {
                // Call plugin's method using the constant fixed tab name
                @Override public void mousePressed(MouseEvent e) { plugin.switchTabExpandCollapse(TZClocksPlugin.FIXED_TAB_NAME); }
                @Override public void mouseEntered(MouseEvent e) { collapseButton.setIcon(hoverIcon); }
                @Override public void mouseExited(MouseEvent e) { collapseButton.setIcon(currentIcon); }
            });
            leftActions.add(collapseButton, BorderLayout.WEST);

            JLabel tabNameLabel = new JLabel(TZClocksPlugin.FIXED_TAB_NAME); // Fixed name
            tabNameLabel.setForeground(Color.WHITE);
            tabNameLabel.setToolTipText(TZClocksPlugin.FIXED_TAB_NAME);
            leftActions.add(tabNameLabel, BorderLayout.CENTER);
            headerPanel.add(leftActions, BorderLayout.WEST);
            // No right-side action buttons for fixed tab header

            // --- Create Items Panel (Only if NOT collapsed) ---
            Map<TZClocksItem, TZClocksTabItemPanel> localFixedMap = new HashMap<>(); // Temp map for this build
            if (!isCollapsed) {
                JPanel itemsPanel = new JPanel(new GridBagLayout());
                itemsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
                itemsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                itemsPanel.setOpaque(true);

                GridBagConstraints itemGBC = new GridBagConstraints(); // Constraints for items within this panel
                itemGBC.fill = GridBagConstraints.HORIZONTAL;
                itemGBC.gridwidth = 1; itemGBC.weightx = 1.0; itemGBC.weighty = 0.0;
                itemGBC.gridx = 0; itemGBC.gridy = 0; itemGBC.anchor = GridBagConstraints.NORTH;

                // Get fixed clock IDs and create panels
                List<UUID> fixedClockIds = List.of(TZClocksPlugin.LOCAL_CLOCK_UUID, TZClocksPlugin.JAGEX_CLOCK_UUID);
                int itemIndex = 0;
                for (UUID clockId : fixedClockIds) {
                    Optional<TZClocksItem> clockOpt = plugin.getTimezones().stream()
                            .filter(c -> c.getUuid().equals(clockId))
                            .findFirst();

                    if (clockOpt.isPresent()) {
                        TZClocksItem clock = clockOpt.get();
                        // Create the item panel for this fixed clock
                        TZClocksTabItemPanel itemPanel = new TZClocksTabItemPanel(plugin, clock);
                        localFixedMap.put(clock, itemPanel); // Store reference in local map

                        // Wrap panel for margin
                        JPanel wrapper = new JPanel(new BorderLayout());
                        wrapper.setOpaque(false);
                        if (itemIndex > 0) { wrapper.setBorder(new EmptyBorder(5, 0, 0, 0)); }
                        else { wrapper.setBorder(new EmptyBorder(0,0,0,0)); }
                        wrapper.add(itemPanel, BorderLayout.NORTH);
                        itemsPanel.add(wrapper, itemGBC); // Add wrapper to items panel

                        itemGBC.gridy++;
                        itemIndex++;
                    }
                }
                // Add glue to push items up
                GridBagConstraints glueGBC = new GridBagConstraints();
                glueGBC.gridx = 0; glueGBC.gridy = itemGBC.gridy;
                glueGBC.weighty = 1.0; glueGBC.fill = GridBagConstraints.VERTICAL;
                itemsPanel.add(Box.createVerticalGlue(), glueGBC);

                // Add the populated items panel to the CENTER (above the header)
                southPanel.add(itemsPanel, BorderLayout.CENTER);
            }

            // Add the header panel to the SOUTH (bottom)
            southPanel.add(headerPanel, BorderLayout.SOUTH);

            // Update the plugin's map with the newly created panels (or empty map if collapsed)
            plugin.setFixedSouthTabClocksMap(localFixedMap);
        }

        // Refresh layout of the southPanel itself and its container if visibility changed
        southPanel.revalidate();
        southPanel.repaint();
        if (getParent() != null) {
            getParent().validate(); // Ask container (main panel) to re-layout
        }
    }


    /**
     * Updates the timezone dropdown based on the selected region, filtering fixed zones.
     */
    public void updateTimeZoneDropdown() {
        TZRegionEnum selectedRegion = (TZRegionEnum) regionDropdown.getSelectedItem();
        List<ZoneId> zoneIds = getTimeZoneIdsForRegion(selectedRegion);
        Object previouslySelected = timezoneDropdown.getSelectedItem();
        timezoneDropdown.removeAllItems();
        for (ZoneId zoneId : zoneIds) { timezoneDropdown.addItem(zoneId.toString()); }
        if (previouslySelected != null) { timezoneDropdown.setSelectedItem(previouslySelected); }
    }

    /**
     * Gets ZoneIds for the selected region, filtering out fixed zones.
     */
    private List<ZoneId> getTimeZoneIdsForRegion(TZRegionEnum region) {
        List<ZoneId> zoneIdsResult = new ArrayList<>();
        if (region == TZRegionEnum.ALL) {
            for (TZRegionEnum reg : TZRegionEnum.values()) {
                if (reg != TZRegionEnum.ALL && reg.getZoneIds() != null) { zoneIdsResult.addAll(reg.getZoneIds()); }
            }
        } else if (region != null && region.getZoneIds() != null) {
            zoneIdsResult.addAll(region.getZoneIds());
        }
        // Filter out fixed zone IDs
        zoneIdsResult.removeIf(zid -> zid.getId().equals(TZClocksPlugin.LOCAL_ZONE_ID));
        zoneIdsResult.removeIf(zid -> zid.getId().equals(TZClocksPlugin.JAGEX_ZONE_ID));
        Collections.sort(zoneIdsResult, (z1, z2) -> z1.toString().compareTo(z2.toString()));
        return zoneIdsResult;
    }

    // --- Add/Remove methods trigger updatePanel ---
    public void addTimezonePanel(TZClocksItem item) { updatePanel(); }
    public void removeTimezonePanel(TZClocksItem item) { timezonePanelsMap.remove(item); updatePanel(); }
    public void addTabPanel(TZClocksTab tab) { updatePanel(); }

    /** Revalidates and repaints the panel containing user items. */
    private void updateClockListPanel() { clockListPanel.revalidate(); clockListPanel.repaint(); }

    // --- Getters for UI maps used by plugin ---
    public Map<TZClocksItem, TZClocksItemPanel> getTimezonePanelsMap() { return timezonePanelsMap; }
    public Map<TZClocksTab, TZClocksTabPanel> getTabPanelsMap() { return tabPanelsMap; }

    /**
     * Rebuilds the user items list (CENTER scroll pane). Excludes fixed items.
     */
    public void updatePanel() {
        if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(this::updatePanel); return; }
        // Clear state before rebuilding
        timezonePanelsMap.clear(); tabPanelsMap.clear(); clockListPanel.removeAll();
        clockListConstraints.gridy = 0; // Reset row counter

        // Add USER Tab Panels
        for (TZClocksTab tab : plugin.getTabs()) {
            TZClocksTabPanel tabPanel = new TZClocksTabPanel(plugin, this, tab);
            tabPanelsMap.put(tab, tabPanel); // Store UI reference
            JPanel wrapper = createMarginWrapper(tabPanel);
            clockListPanel.add(wrapper, clockListConstraints); clockListConstraints.gridy++;
        }
        // Add USER Standalone Clock Panels
        for (TZClocksItem clock : plugin.getTimezones()) {
            // Skip fixed clocks
            if (clock.getUuid().equals(TZClocksPlugin.LOCAL_CLOCK_UUID) || clock.getUuid().equals(TZClocksPlugin.JAGEX_CLOCK_UUID)) { continue; }
            // Check if clock is already in a user tab
            boolean isInUserTab = plugin.getTabs().stream().anyMatch(t -> t.getClocks() != null && t.getClocks().contains(clock.getUuid()));
            if (!isInUserTab) {
                TZClocksItemPanel clockPanel = new TZClocksItemPanel(plugin, clock);
                timezonePanelsMap.put(clock, clockPanel); // Store UI reference
                JPanel wrapper = createMarginWrapper(clockPanel);
                clockListPanel.add(wrapper, clockListConstraints); clockListConstraints.gridy++;
            }
        }
        // Add Glue to push items up
        GridBagConstraints glueConstraints = new GridBagConstraints();
        glueConstraints.gridx = 0; glueConstraints.gridy = clockListConstraints.gridy;
        glueConstraints.weighty = 1.0; glueConstraints.fill = GridBagConstraints.VERTICAL;
        clockListPanel.add(glue, glueConstraints);

        updateClockListPanel(); // Refresh layout
    }

    // --- Other Public Methods ---
    public TZClocksPlugin getPlugin() { return plugin; }

    /** Gets user clocks available to add to tabs. */
    public List<TZClocksItem> getAvailableClocks() {
        List<TZClocksItem> availableClocks = plugin.getTimezones().stream()
                .filter(clock -> !clock.getUuid().equals(TZClocksPlugin.LOCAL_CLOCK_UUID) && !clock.getUuid().equals(TZClocksPlugin.JAGEX_CLOCK_UUID))
                .collect(Collectors.toList());
        for (TZClocksTab userTab : plugin.getTabs()) {
            if (userTab.getClocks() != null) {
                userTab.getClocks().forEach(clockIdInTab -> availableClocks.removeIf(availableClock -> availableClock.getUuid().equals(clockIdInTab)) );
            }
        } return availableClocks;
    }

    /** Wraps a panel with margin (if not first item). */
    public JPanel createMarginWrapper(JPanel contentPanel) {
        JPanel marginWrapper = new JPanel(new BorderLayout());
        marginWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
        boolean isFirstItem = (clockListConstraints.gridy == 0);
        marginWrapper.setBorder(isFirstItem ? new EmptyBorder(0, 0, 0, 0) : new EmptyBorder(5, 0, 0, 0));
        marginWrapper.add(contentPanel, BorderLayout.NORTH);
        return marginWrapper;
    }
}