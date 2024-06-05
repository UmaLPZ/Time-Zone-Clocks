package com.tzclocks.tzui;

import com.tzclocks.TZClocksPlugin;
import com.tzclocks.tzconfig.TZClocksConfig;
import com.tzclocks.tzdata.TZClocksCategory;
import com.tzclocks.tzdata.TZClocksItem;
import com.tzclocks.tzutilities.TZRegionEnum;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
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
    private final Map<TZClocksCategory, JPanel> categoryPanelsMap = new HashMap<>(); // Store category panels
    private final TZClocksPlugin plugin;
    private final TZClocksConfig config;
    private final JComboBox<String> timezoneDropdown;
    private JPanel clockListPanel; // Panel to hold the list of clocks
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final ImageIcon COLLAPSE_ICON;
    private static final ImageIcon EXPAND_ICON;
    private static final ImageIcon DELETE_ICON;
    private static final ImageIcon DELETE_HOVER_ICON;
    private static final ImageIcon EDIT_ICON;
    private static final ImageIcon EDIT_HOVER_ICON;
    private static final ImageIcon ADD_ICON;
    private static final ImageIcon ADD_HOVER_ICON;


    static {
        final BufferedImage collapseImage = ImageUtil.loadImageResource(TZClocksPluginPanel.class, "/tzcollapseicon.png");
        COLLAPSE_ICON = new ImageIcon(collapseImage);
        EXPAND_ICON = new ImageIcon(ImageUtil.loadImageResource(TZClocksPluginPanel.class, "/tzexpandicon.png"));

        final BufferedImage deleteImage = ImageUtil.loadImageResource(TZClocksItemPanel.class, "/tzdeleteicon.png");
        DELETE_ICON = new ImageIcon(deleteImage);
        DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImage, 0.53f));

        final BufferedImage editImage = ImageUtil.loadImageResource(TZClocksItemPanel.class, "/tzediticon.png");
        EDIT_ICON = new ImageIcon(editImage);
        EDIT_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(editImage, 0.53f));

        final BufferedImage addImage = ImageUtil.loadImageResource(TZClocksItemPanel.class, "/tzaddicon.png");
        ADD_ICON = new ImageIcon(addImage);
        ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addImage, 0.53f));
    }


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
        setLayout(new GridBagLayout()); // Use GridBagLayout for main panel

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JPanel topPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        topPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        regionDropdown = new JComboBox<>(); //dropdown for selecting the region
        for (TZRegionEnum region : TZRegionEnum.values()) {
            regionDropdown.addItem(region); // Use the existing name field
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

        JButton addCategoryButton = new JButton("Add Category");
        addCategoryButton.addActionListener(e -> {
            String categoryName = JOptionPane.showInputDialog(TZClocksPluginPanel.this,
                    "Enter a name for the category:", "New Category", JOptionPane.PLAIN_MESSAGE);
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                plugin.addCategory(categoryName);
            }
        });
        topPanel.add(addCategoryButton);

        add(topPanel, gbc);

        gbc.gridy++;
        gbc.weighty = 1; // Make scroll pane expand vertically
        gbc.fill = GridBagConstraints.BOTH;

        clockListPanel = new JPanel(new GridBagLayout());
        clockListPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        JScrollPane scrollPane = new JScrollPane(clockListPanel); // Add clockPanel directly to JScrollPane
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        scrollPane.getVerticalScrollBar().setBorder(new EmptyBorder(5, 5, 0, 0));
        add(scrollPane, gbc); // Add scroll pane to the main panel

        scheduler.scheduleAtFixedRate(this::refreshTimeDisplays, 0, 1, TimeUnit.SECONDS);
        updateTimeZoneDropdown();
    }

    private List<ZoneId> getTimeZoneIdsForRegion(TZRegionEnum region) { //self-explanatory. gets the time zone IDs from TZRegionEnum
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

    private void updateTimeZoneDropdown() { //self-explanatory. updates the time zone dropdown to match region
        TZRegionEnum selectedRegion = (TZRegionEnum) regionDropdown.getSelectedItem();
        List<ZoneId> zoneIds = getTimeZoneIdsForRegion(selectedRegion);

        timezoneDropdown.removeAllItems();

        if (selectedRegion == TZRegionEnum.SPECIAL_TIMES) {
            timezoneDropdown.addItem("Local Time#" + ZoneId.systemDefault()); // Use "#" as a separator
            timezoneDropdown.addItem("Jagex Time#Europe/London");
        } else {
            for (ZoneId zoneId : zoneIds) {
                timezoneDropdown.addItem(zoneId.toString());
            }
        }
    }

    public void addTimezonePanel(TZClocksItem item) { //adds the time zone panel
        TZClocksItemPanel timeZonePanel = new TZClocksItemPanel(plugin, item);
        TZClocksItemPanels.add(timeZonePanel);
        timezonePanelsMap.put(item, timeZonePanel);

        // Find the panel for the clock's category
        JPanel categoryPanel = categoryPanelsMap.get(getCategoryForClock(item));
        if (categoryPanel != null) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            gbc.gridx = 0;
            gbc.gridy = categoryPanel.getComponentCount(); // Add at the end

            JPanel containerPanel = new JPanel(new BorderLayout());
            containerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
            containerPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
            containerPanel.add(timeZonePanel, BorderLayout.CENTER);

            categoryPanel.add(containerPanel, gbc);
            categoryPanel.revalidate();
            categoryPanel.repaint();
        }
    }

    private TZClocksCategory getCategoryForClock(TZClocksItem clock) {
        for (TZClocksCategory category : plugin.getCategories()) {
            if (category.getClocks().contains(clock.getUuid())) {
                return category;
            }
        }
        return null; // Or handle the case where the clock doesn't belong to a category
    }


    public void removeTimezonePanel(TZClocksItem item) { //removes time zone from panel
        TZClocksItemPanel panelToRemove = timezonePanelsMap.remove(item);
        if (panelToRemove != null) {
            for (Component component : clockListPanel.getComponents()) {
                if (component instanceof JPanel && ((JPanel) component).getComponent(0) == panelToRemove) {
                    clockListPanel.remove(component);
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
        for (TZClocksItem item : plugin.getTimezones()) {
            ZoneId zoneId = ZoneId.of(item.getName());
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            item.setCurrentTime(now.format(formatter));
        }
        for (TZClocksItemPanel panel : TZClocksItemPanels) {
            panel.updateTime();
        }
    }

    public void removeAllClocks() { //removes clocks to prevent re-adding previously selected clocks. there might be an alternative
        clockListPanel.removeAll();
        TZClocksItemPanels.clear();
        timezonePanelsMap.clear();
        revalidate();
        repaint();
    }

    // Category Panel Methods

    public void addCategoryPanel(TZClocksCategory category) {
        JPanel categoryPanel = createCategoryPanel(category);
        categoryPanelsMap.put(category, categoryPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = clockListPanel.getComponentCount();

        clockListPanel.add(categoryPanel, gbc);
        clockListPanel.revalidate();
        clockListPanel.repaint();
    }

    private JPanel createCategoryPanel(TZClocksCategory category) {
        JPanel categoryPanel = new JPanel(new GridBagLayout());
        categoryPanel.setBorder(new EmptyBorder(5, 5, 5, 0));
        categoryPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Expand/Collapse Button
        JLabel collapseButton = new JLabel(category.isCollapsed() ? EXPAND_ICON : COLLAPSE_ICON);
        collapseButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                toggleCategoryCollapse(category);
            }
        });
        headerPanel.add(collapseButton, BorderLayout.WEST);

        // Category Name
        JLabel categoryNameLabel = new JLabel(category.getName());
        categoryNameLabel.setForeground(Color.WHITE);
        headerPanel.add(categoryNameLabel, BorderLayout.CENTER);

        // Add, Rename, Delete Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));
        buttonPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Rename button
        JLabel renameButton = new JLabel(EDIT_ICON);
        renameButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                String newName = JOptionPane.showInputDialog("Enter new name:", category.getName());
                if (newName != null && !newName.trim().isEmpty()) {
                    plugin.renameCategory(category, newName);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                renameButton.setIcon(EDIT_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                renameButton.setIcon(EDIT_ICON);
            }
        });
        buttonPanel.add(renameButton);

        // Add button
        JLabel addButton = new JLabel(ADD_ICON);
        addButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                showAddClocksDialog(category);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                addButton.setIcon(ADD_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                addButton.setIcon(ADD_ICON);
            }
        });
        buttonPanel.add(addButton);

        // Delete button
        JLabel deleteButton = new JLabel(DELETE_ICON);
        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int result = JOptionPane.showConfirmDialog(TZClocksPluginPanel.this,
                        "Delete category '" + category.getName() + "'?\n" +
                                "Do you want to delete the clocks in this category as well?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    plugin.deleteCategory(category, true); // Delete category and clocks
                } else if (result == JOptionPane.NO_OPTION) {
                    plugin.deleteCategory(category, false); // Delete category only
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                deleteButton.setIcon(DELETE_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                deleteButton.setIcon(DELETE_ICON);
            }
        });
        buttonPanel.add(deleteButton);

        headerPanel.add(buttonPanel, BorderLayout.EAST);
        categoryPanel.add(headerPanel, gbc);

        // Clocks Panel (initially empty)
        gbc.gridy++;
        JPanel clocksPanel = new JPanel(new GridBagLayout());
        clocksPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        categoryPanel.add(clocksPanel, gbc);

        return categoryPanel;
    }

    public void renameCategoryPanel(TZClocksCategory category) {
        JPanel categoryPanel = categoryPanelsMap.get(category);
        if (categoryPanel != null) {
            Component[] components = categoryPanel.getComponents();
            if (components.length > 0 && components[0] instanceof JPanel) {
                JPanel headerPanel = (JPanel) components[0];
                Component[] headerComponents = headerPanel.getComponents();
                if (headerComponents.length > 1 && headerComponents[1] instanceof JLabel) {
                    JLabel categoryNameLabel = (JLabel) headerComponents[1];
                    categoryNameLabel.setText(category.getName());
                }
            }
        }
    }

    public void removeCategoryPanel(TZClocksCategory category) {
        JPanel categoryPanel = categoryPanelsMap.remove(category);
        if (categoryPanel != null) {
            clockListPanel.remove(categoryPanel);
            clockListPanel.revalidate();
            clockListPanel.repaint();
        }
    }


    private void toggleCategoryCollapse(TZClocksCategory category) {
        category.setCollapsed(!category.isCollapsed());
        JPanel categoryPanel = categoryPanelsMap.get(category);
        if (categoryPanel != null) {
            Component[] components = categoryPanel.getComponents();
            if (components.length > 1 && components[1] instanceof JPanel) {
                JPanel clocksPanel = (JPanel) components[1];
                clocksPanel.removeAll(); // Clear existing clocks

                if (!category.isCollapsed()) {
                    for (UUID clockId : category.getClocks()) {
                        Optional<TZClocksItem> clockItem = plugin.getTimezones().stream()
                                .filter(item -> item.getUuid().equals(clockId))
                                .findFirst();
                        clockItem.ifPresent(this::addTimezonePanel);
                    }
                }

                // Update collapse/expand icon
                Component[] headerComponents = ((JPanel) components[0]).getComponents();
                if (headerComponents.length > 0 && headerComponents[0] instanceof JLabel) {
                    JLabel collapseButton = (JLabel) headerComponents[0];
                    collapseButton.setIcon(category.isCollapsed() ? EXPAND_ICON : COLLAPSE_ICON);
                }

                clocksPanel.revalidate();
                clocksPanel.repaint();
            }
        }
    }

    private void showAddClocksDialog(TZClocksCategory category) {
        List<TZClocksItem> uncategorizedClocks = getUncategorizedClocks();

        if (uncategorizedClocks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are no uncategorized clocks to add.",
                    "No Uncategorized Clocks", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JList<TZClocksItem> clockList = new JList<>(uncategorizedClocks.toArray(new TZClocksItem[0]));
        clockList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        clockList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TZClocksItem) {
                    setText(((TZClocksItem) value).getName());
                }
                return component;
            }
        });

        JScrollPane scrollPane = new JScrollPane(clockList);
        int result = JOptionPane.showConfirmDialog(this, scrollPane, "Add Clocks to " + category.getName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            List<TZClocksItem> selectedClocks = clockList.getSelectedValuesList();
            for (TZClocksItem clock : selectedClocks) {
                // Move the clock from "Uncategorized" to the selected category
                plugin.getCategories().get(0).removeClock(clock.getUuid());
                category.addClock(clock.getUuid());

                // Update the UI
                removeTimezonePanel(clock); // Remove from "Uncategorized"
                addTimezonePanel(clock); // Add to the selected category
            }
            plugin.dataManager.saveData();
        }
    }

    private List<TZClocksItem> getUncategorizedClocks() {
        List<TZClocksItem> uncategorized = new ArrayList<>();
        for (TZClocksItem clock : plugin.getTimezones()) {
            if (getCategoryForClock(clock) == plugin.getCategories().get(0)) {
                uncategorized.add(clock);
            }
        }
        return uncategorized;
    }
}