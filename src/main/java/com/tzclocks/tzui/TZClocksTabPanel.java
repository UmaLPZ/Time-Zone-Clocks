package com.tzclocks.tzui;

import com.tzclocks.TZClocksPlugin;
import com.tzclocks.tzdata.TZClocksItem;
import com.tzclocks.tzdata.TZClocksTab;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.tzclocks.tzutilities.TZConstants.*;

public class TZClocksTabPanel extends JPanel {
    private static final String DELETE_TITLE = "Delete Tab";
    private static final String DELETE_MESSAGE = "Are you sure you want to delete this tab? This will not delete the clocks.";
    // Icons
    private static final ImageIcon ADD_ICON;
    private static final ImageIcon ADD_HOVER_ICON;
    private static final ImageIcon EDIT_ICON;
    private static final ImageIcon EDIT_HOVER_ICON;
    private static final ImageIcon DELETE_TAB_ICON;
    private static final ImageIcon DELETE_TAB_HOVER_ICON;
    // --- Updated Icon Definitions ---
    private static final ImageIcon COLLAPSE_ICON;       // Right Arrow (shown when EXPANDED) -> Collapses
    private static final ImageIcon COLLAPSE_HOVER_ICON;
    private static final ImageIcon EXPAND_DOWN_ICON;    // Down Arrow (shown when USER tab COLLAPSED) -> Expands Down
    private static final ImageIcon EXPAND_DOWN_HOVER_ICON;
    // Up arrow icons not needed here

    private final TZClocksPlugin plugin;
    @Getter
    private final TZClocksTab tab; // User tab data
    private final TZClocksPluginPanel pluginPanel;
    private final JPanel itemsPanel;
    private final GridBagConstraints constraints = new GridBagConstraints();
    @Getter
    private final Map<TZClocksItem, TZClocksTabItemPanel> tabItemPanelsMap = new HashMap<>();

    static {
        // Consistent Icon Loading
        final int ICON_SIZE = 12;
        final float ALPHA_NORMAL = 0.7f;
        final float ALPHA_HOVER = 0.53f;

        final BufferedImage addImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksPlugin.class, ADD_ICON_PATH), ICON_SIZE, ICON_SIZE);
        ADD_ICON = new ImageIcon(ImageUtil.alphaOffset(addImage, ALPHA_NORMAL));
        ADD_HOVER_ICON = new ImageIcon(addImage);

        final BufferedImage editImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksPlugin.class, EDIT_ICON_PATH), ICON_SIZE, ICON_SIZE);
        EDIT_ICON = new ImageIcon(ImageUtil.alphaOffset(editImage, ALPHA_NORMAL));
        EDIT_HOVER_ICON = new ImageIcon(editImage);

        final BufferedImage deleteTabImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksPlugin.class, DELETE_ICON_PATH), ICON_SIZE, ICON_SIZE);
        DELETE_TAB_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteTabImage, ALPHA_NORMAL));
        DELETE_TAB_HOVER_ICON = new ImageIcon(deleteTabImage);

        // --- Corrected Icon Loading based on User Definitions ---
        // Right Arrow -> Collapse
        final BufferedImage collapseImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksPlugin.class, COLLAPSE_ICON_PATH), ICON_SIZE, ICON_SIZE);
        COLLAPSE_ICON = new ImageIcon(collapseImage);
        COLLAPSE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(collapseImage, ALPHA_HOVER));

        // Down Arrow -> Expand Down (User Tabs)
        final BufferedImage expandDownImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksPlugin.class, EXPAND_ICON_PATH), ICON_SIZE, ICON_SIZE);
        EXPAND_DOWN_ICON = new ImageIcon(expandDownImage);
        EXPAND_DOWN_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(expandDownImage, ALPHA_HOVER));
    }

    // Constructor specifically for USER tabs
    public TZClocksTabPanel(TZClocksPlugin plugin, TZClocksPluginPanel panel, TZClocksTab tab) {
        this.plugin = plugin;
        this.pluginPanel = panel;
        this.tab = tab;

        // Main panel layout
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(0,0,0,0)); // External spacing handled by margin wrappers
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setOpaque(true);

        // Use the collapsed state directly from the user TZClocksTab object
        boolean isCollapsed = tab.isCollapsed();

        // --- Header Panel (BorderLayout) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR); // Consistent background
        headerPanel.setBorder(new EmptyBorder(5, 5, 5, 5)); // Padding inside header

        // Left Actions: Collapse/Expand + Name
        JPanel leftActions = new JPanel(new BorderLayout(5, 0));
        leftActions.setOpaque(false);

        JLabel collapseButton = new JLabel();
        collapseButton.setOpaque(false);

        // Use corrected icon logic for USER tabs
        final ImageIcon currentIcon;
        final ImageIcon hoverIcon;
        if (isCollapsed) {
            currentIcon = EXPAND_DOWN_ICON; // Show DOWN arrow when collapsed
            hoverIcon = EXPAND_DOWN_HOVER_ICON;
            collapseButton.setToolTipText("Expand Tab");
        } else {
            currentIcon = COLLAPSE_ICON; // Show RIGHT arrow when expanded
            hoverIcon = COLLAPSE_HOVER_ICON;
            collapseButton.setToolTipText("Collapse Tab");
        }
        collapseButton.setIcon(currentIcon);
        collapseButton.addMouseListener(new MouseAdapter() {
            // Pass tab name (String) to plugin's handler
            @Override public void mousePressed(MouseEvent e) { plugin.switchTabExpandCollapse(tab.getName()); }
            @Override public void mouseEntered(MouseEvent e) { collapseButton.setIcon(hoverIcon); }
            @Override public void mouseExited(MouseEvent e) { collapseButton.setIcon(currentIcon); }
        });
        leftActions.add(collapseButton, BorderLayout.WEST);

        JLabel tabNameLabel = new JLabel(tab.getName());
        tabNameLabel.setForeground(Color.WHITE);
        tabNameLabel.setToolTipText(tab.getName());
        leftActions.add(tabNameLabel, BorderLayout.CENTER);

        headerPanel.add(leftActions, BorderLayout.WEST);

        // Right Actions: Add, Edit, Delete (Always visible for user tabs)
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightActions.setOpaque(false);

        JLabel addClockButton = new JLabel(ADD_ICON);
        addClockButton.setToolTipText("Add clock(s) to this tab");
        addClockButton.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { showAddClocksDialog(); }
            @Override public void mouseEntered(MouseEvent e) { addClockButton.setIcon(ADD_HOVER_ICON); }
            @Override public void mouseExited(MouseEvent e) { addClockButton.setIcon(ADD_ICON); }
        });
        rightActions.add(addClockButton);

        JLabel editTabButton = new JLabel(EDIT_ICON);
        editTabButton.setToolTipText("Edit tab name");
        editTabButton.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { plugin.editTab(tab); }
            @Override public void mouseEntered(MouseEvent e) { editTabButton.setIcon(EDIT_HOVER_ICON); }
            @Override public void mouseExited(MouseEvent e) { editTabButton.setIcon(EDIT_ICON); }
        });
        rightActions.add(editTabButton);

        JLabel deleteTabButton = new JLabel(DELETE_TAB_ICON);
        deleteTabButton.setToolTipText("Delete this tab");
        deleteTabButton.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { if (deleteConfirm()) plugin.removeTab(tab); }
            @Override public void mouseEntered(MouseEvent e) { deleteTabButton.setIcon(DELETE_TAB_HOVER_ICON); }
            @Override public void mouseExited(MouseEvent e) { deleteTabButton.setIcon(DELETE_TAB_ICON); }
        });
        rightActions.add(deleteTabButton);

        headerPanel.add(rightActions, BorderLayout.EAST);

        // Add Header to this main Tab Panel
        add(headerPanel, BorderLayout.NORTH);

        // --- Items Panel (Clocks) ---
        itemsPanel = new JPanel(new GridBagLayout());
        itemsPanel.setBorder(new EmptyBorder(5, 5, 5, 5)); // Padding around list of clocks
        itemsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        itemsPanel.setOpaque(true);

        // Configure constraints for items within itemsPanel
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 1; constraints.weightx = 1.0; constraints.weighty = 0.0;
        constraints.gridx = 0; constraints.gridy = 0; constraints.anchor = GridBagConstraints.NORTH;

        // Populate itemsPanel only if NOT collapsed
        if (!isCollapsed) {
            int itemIndex = 0;
            List<UUID> clockIds = tab.getClocks() != null ? tab.getClocks() : new ArrayList<>();

            for (UUID clockId : clockIds) {
                Optional<TZClocksItem> clockOpt = plugin.getTimezones().stream()
                        .filter(c -> c.getUuid().equals(clockId))
                        .findFirst();

                if (clockOpt.isPresent()) {
                    TZClocksItem clock = clockOpt.get();
                    // Create the specific item panel for this clock within this tab
                    TZClocksTabItemPanel itemPanel = new TZClocksTabItemPanel(plugin, clock);
                    tabItemPanelsMap.put(clock, itemPanel); // Store reference for updates

                    // Wrap the item panel for margin spacing
                    JPanel wrapper = new JPanel(new BorderLayout());
                    wrapper.setOpaque(false);
                    if (itemIndex > 0) { wrapper.setBorder(new EmptyBorder(5, 0, 0, 0)); } // Add top margin if not first
                    else { wrapper.setBorder(new EmptyBorder(0,0,0,0)); } // No margin for first
                    wrapper.add(itemPanel, BorderLayout.NORTH);
                    itemsPanel.add(wrapper, constraints); // Add wrapper to the grid

                    constraints.gridy++; // Increment grid row
                    itemIndex++;
                }
            }
            // Add vertical glue to push items to the top within itemsPanel
            GridBagConstraints tabGlueConstraints = new GridBagConstraints();
            tabGlueConstraints.gridx = 0; tabGlueConstraints.gridy = constraints.gridy; // After last item
            tabGlueConstraints.weighty = 1.0; tabGlueConstraints.fill = GridBagConstraints.VERTICAL;
            itemsPanel.add(Box.createVerticalGlue(), tabGlueConstraints);

            // Add the populated itemsPanel to this main Tab Panel
            add(itemsPanel, BorderLayout.CENTER);
        }
    }

    /**
     * Shows the dialog for selecting clocks to add to this user tab.
     */
    private void showAddClocksDialog() {
        List<TZClocksItem> clocks = pluginPanel.getAvailableClocks(); // Get user clocks not in tabs
        if (clocks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All available clocks are already in tabs.", "No Available Clocks", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // Create and configure the selection panel
        TZClocksSelectionPanel selectionPanel = new TZClocksSelectionPanel(this, pluginPanel, clocks);
        selectionPanel.setOnOk(e -> { // Define action on OK click
            List<TZClocksItem> selectedClocks = selectionPanel.getSelectedClocks();
            for (TZClocksItem clock : selectedClocks) {
                // Call plugin method to handle data change and UI update
                plugin.addClockToUserTab(clock, tab);
            }
        });
        selectionPanel.show(); // Display the dialog
    }

    /**
     * Shows confirmation dialog before deleting a user tab.
     */
    private boolean deleteConfirm() {
        int confirm = JOptionPane.showConfirmDialog(this, DELETE_MESSAGE, DELETE_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return confirm == JOptionPane.YES_OPTION; // Return true if user clicked Yes
    }

    // Getters if needed externally
    // public Map<TZClocksItem, TZClocksTabItemPanel> getTabItemPanelsMap() { return tabItemPanelsMap; }
}