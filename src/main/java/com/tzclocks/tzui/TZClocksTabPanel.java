package com.tzclocks.tzui;

import com.tzclocks.TZClocksPlugin;
import com.tzclocks.tzdata.TZClocksItem;
import com.tzclocks.tzdata.TZClocksTab;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.tzclocks.tzutilities.TZConstants.*;

public class TZClocksTabPanel extends JPanel {
    private static final ImageIcon COLLAPSE_ICON;
    private static final ImageIcon EXPAND_ICON;
    private static final ImageIcon DELETE_ICON;
    private static final ImageIcon DELETE_HOVER_ICON;
    private static final ImageIcon EDIT_ICON;
    private static final ImageIcon EDIT_HOVER_ICON;
    private static final ImageIcon ADD_ICON;
    private static final ImageIcon ADD_HOVER_ICON;

    private final TZClocksPlugin plugin;
    private final TZClocksTab tab;
    private JPanel clocksPanel;

    static {
        final BufferedImage collapseImage = ImageUtil.loadImageResource(TZClocksTabPanel.class, "/tzcollapseicon.png");
        COLLAPSE_ICON = new ImageIcon(collapseImage);
        EXPAND_ICON = new ImageIcon(ImageUtil.loadImageResource(TZClocksTabPanel.class, "/tzexpandicon.png"));

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

    public TZClocksTabPanel(TZClocksPlugin plugin, TZClocksTab tab) {
        this.plugin = plugin;
        this.tab = tab;

        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(5, 5, 5, 0));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Expand/Collapse Button
        JLabel collapseButton = new JLabel(tab.isCollapsed() ? EXPAND_ICON : COLLAPSE_ICON);
        collapseButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                toggleTabCollapse();
            }
        });
        headerPanel.add(collapseButton, BorderLayout.WEST);

        // Tab Name
        JLabel tabNameLabel = new JLabel(tab.getName());
        tabNameLabel.setForeground(Color.WHITE);
        headerPanel.add(tabNameLabel, BorderLayout.CENTER);

        // Add, Rename, Delete Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));
        buttonPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Rename button
        JLabel renameButton = new JLabel(EDIT_ICON);
        renameButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                String newName = JOptionPane.showInputDialog("Enter new name:", tab.getName());
                if (newName != null && !newName.trim().isEmpty()) {
                    plugin.renameTab(tab, newName);
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
                showAddClocksDialog();
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
                int result = JOptionPane.showConfirmDialog(TZClocksTabPanel.this,
                        "Delete tab '" + tab.getName() + "'?\n" +
                                "Do you want to delete the clocks in this tab as well?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    plugin.deleteTab(tab, true); // Delete tab and clocks
                } else if (result == JOptionPane.NO_OPTION) {
                    plugin.deleteTab(tab, false); // Delete tab only
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
        add(headerPanel, gbc);

        // Clocks Panel (initially empty)
        gbc.gridy++;
        clocksPanel = new JPanel(new GridBagLayout());
        clocksPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        add(clocksPanel, gbc);
    }

    // Add getTab() method
    public TZClocksTab getTab() {
        return tab;
    }

    public void toggleTabCollapse() { // Change access modifier to public
        tab.setCollapsed(!tab.isCollapsed());
        if (tab.isCollapsed()) {
            // Remove all clocks from the clocksPanel when collapsing
            clocksPanel.removeAll();
        } else {
            // Add clocks to the clocksPanel when expanding
            for (UUID clockId : tab.getClocks()) {
                Optional<TZClocksItem> clockItem = plugin.getTimezones().stream()
                        .filter(item -> item.getUuid().equals(clockId))
                        .findFirst();

                clockItem.ifPresent(item -> {
                    TZClocksItemPanel clockPanel = plugin.getPanel().getTimezonePanelsMap().get(item);
                    if (clockPanel != null) {
                        addClockToTab(clockPanel);
                    }
                });
            }
        }
        updateCollapseIcon();
        clocksPanel.revalidate();
        clocksPanel.repaint();
    }

    private void updateCollapseIcon() {
        Component[] headerComponents = ((JPanel) getComponents()[0]).getComponents(); // Get components of the header panel
        if (headerComponents.length > 0 && headerComponents[0] instanceof JLabel) {
            JLabel collapseButton = (JLabel) headerComponents[0];
            collapseButton.setIcon(tab.isCollapsed() ? EXPAND_ICON : COLLAPSE_ICON);
        }
    }

    private void showAddClocksDialog() {
        List<TZClocksItem> availableClocks = getAvailableClocks();

        if (availableClocks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All clocks are already categorized.",
                    "No Available Clocks", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JList<TZClocksItem> clockList = new JList<>(availableClocks.toArray(new TZClocksItem[0]));
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
        int result = JOptionPane.showConfirmDialog(this, scrollPane, "Add Clocks to " + tab.getName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            List<TZClocksItem> selectedClocks = clockList.getSelectedValuesList();
            for (TZClocksItem clock : selectedClocks) {
                tab.addClock(clock.getUuid());
                addClockToTab(plugin.getPanel().getTimezonePanelsMap().get(clock));
            }
            plugin.dataManager.saveData();
        }
    }

    // Method to add a clock panel to this tab
    private void addClockToTab(TZClocksItemPanel clockPanel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = clocksPanel.getComponentCount(); // Add at the end

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        containerPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
        containerPanel.add(clockPanel, BorderLayout.CENTER);

        clocksPanel.add(containerPanel, gbc);
        clocksPanel.revalidate();
        clocksPanel.repaint();
    }

    private List<TZClocksItem> getAvailableClocks() {
        List<TZClocksItem> availableClocks = new ArrayList<>(plugin.getTimezones());
        for (TZClocksTab t : plugin.getTabs()) { // Updated to getTabs()
            availableClocks.removeIf(clock -> t.getClocks().contains(clock.getUuid()));
        }
        return availableClocks;
    }

}