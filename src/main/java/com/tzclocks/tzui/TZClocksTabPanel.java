package com.tzclocks.tzui;

import ch.qos.logback.classic.Logger;
import com.tzclocks.TZClocksPlugin;
import com.tzclocks.tzdata.TZClocksItem;
import com.tzclocks.tzdata.TZClocksTab;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tzclocks.tzutilities.TZConstants.*;

public class TZClocksTabPanel extends JPanel {
    private AtomicInteger index = new AtomicInteger(0);
    private static final String DELETE_TITLE = "Delete Tab";
    private static final String DELETE_MESSAGE = "Are you sure you want to delete this tab? This will not delete the clocks.";
    private static final ImageIcon ADD_ICON;
    private static final ImageIcon ADD_HOVER_ICON;
    private static final ImageIcon EDIT_ICON;
    private static final ImageIcon EDIT_HOVER_ICON;
    private static final ImageIcon DELETE_TAB_ICON;
    private static final ImageIcon DELETE_TAB_HOVER_ICON;
    private static final ImageIcon COLLAPSE_ICON;
    private static final ImageIcon COLLAPSE_HOVER_ICON;
    private static final ImageIcon EXPAND_ICON;
    private static final ImageIcon EXPAND_HOVER_ICON;

    private final TZClocksPlugin plugin;
    private final TZClocksTab tab;
    private final TZClocksPluginPanel pluginPanel;
    private final JPanel itemsPanel;
    private final GridBagConstraints constraints = new GridBagConstraints();

    static {
        final BufferedImage addImage = ImageUtil.loadImageResource(TZClocksPlugin.class, ADD_ICON_PATH);
        ADD_ICON = new ImageIcon(ImageUtil.alphaOffset(addImage, 0.53f));
        ADD_HOVER_ICON = new ImageIcon(addImage);

        final BufferedImage editImage = ImageUtil.loadImageResource(TZClocksPlugin.class, EDIT_ICON_PATH);
        EDIT_ICON = new ImageIcon(ImageUtil.alphaOffset(editImage, 0.53f));
        EDIT_HOVER_ICON = new ImageIcon(editImage);

        final BufferedImage deleteTabImage = ImageUtil.loadImageResource(TZClocksPlugin.class, DELETE_ICON_PATH);
        DELETE_TAB_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteTabImage, 0.53f));
        DELETE_TAB_HOVER_ICON = new ImageIcon(deleteTabImage);

        final BufferedImage collapseImage = ImageUtil.loadImageResource(TZClocksPlugin.class, COLLAPSE_ICON_PATH);
        COLLAPSE_ICON = new ImageIcon(collapseImage);
        COLLAPSE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(collapseImage, 0.53f));

        final BufferedImage expandImage = ImageUtil.loadImageResource(TZClocksPlugin.class, EXPAND_ICON_PATH);
        EXPAND_ICON = new ImageIcon(expandImage);
        EXPAND_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(expandImage, 0.53f));
    }

    public TZClocksTabPanel(TZClocksPlugin plugin, TZClocksPluginPanel panel, TZClocksTab tab) {
        setLayout(new BorderLayout(5, 0));
        setBorder(new EmptyBorder(5, 5, 5, 0));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);

        this.plugin = plugin;
        this.pluginPanel = panel;
        this.tab = tab;

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel leftActions = new JPanel(new BorderLayout());
        leftActions.setOpaque(false);

        JLabel tabName = new JLabel();
        tabName.setForeground(Color.WHITE);
        tabName.setBorder(new EmptyBorder(0, 5, 0, 0));
        tabName.setPreferredSize(new Dimension(120, 0));
        tabName.setText(tab.getName());
        tabName.setToolTipText((tab.getName()));

        JLabel collapseButton = new JLabel();
        collapseButton.setOpaque(false);

        itemsPanel = new JPanel();
        itemsPanel.setLayout(new GridBagLayout());
        itemsPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
        itemsPanel.setOpaque(false);

        if (tab.isCollapsed()) {
            tabName.setPreferredSize(new Dimension(120, 0));

            collapseButton.setIcon(COLLAPSE_ICON);
            collapseButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    plugin.switchTabExpandCollapse(tab);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    collapseButton.setIcon(COLLAPSE_HOVER_ICON);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    collapseButton.setIcon(COLLAPSE_ICON);
                }
            });

            leftActions.add(tabName, BorderLayout.EAST);
            leftActions.add(collapseButton, BorderLayout.WEST);
            topPanel.add(leftActions, BorderLayout.WEST);

            add(topPanel, BorderLayout.CENTER);
        } else {
            collapseButton.setIcon(EXPAND_ICON);
            collapseButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    plugin.switchTabExpandCollapse(tab);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    collapseButton.setIcon(EXPAND_HOVER_ICON);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    collapseButton.setIcon(EXPAND_ICON);
                }
            });

            leftActions.add(tabName, BorderLayout.EAST);
            leftActions.add(collapseButton, BorderLayout.WEST);

            topPanel.add(leftActions, BorderLayout.WEST);

            JPanel rightActions = new JPanel(new BorderLayout());
            rightActions.setBorder(new EmptyBorder(0, 0, 0, 5));
            rightActions.setOpaque(false);

            JLabel deleteBtn = new JLabel(DELETE_TAB_ICON);
            deleteBtn.setVerticalAlignment(SwingConstants.CENTER);
            deleteBtn.setBorder(new EmptyBorder(0, 0, 0, 5));
            deleteBtn.setOpaque(false);
            deleteBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (deleteConfirm()) {
                        plugin.removeTab(tab);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    deleteBtn.setIcon(DELETE_TAB_HOVER_ICON);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    deleteBtn.setIcon(DELETE_TAB_ICON);
                }
            });

            rightActions.add(deleteBtn, BorderLayout.LINE_START);

            JLabel edit = new JLabel(EDIT_ICON);
            edit.setVerticalAlignment(SwingConstants.CENTER);
            edit.setBorder(new EmptyBorder(0, 0, 0, 5));
            edit.setOpaque(false);
            edit.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    plugin.editTab(tab);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    edit.setIcon(EDIT_HOVER_ICON);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    edit.setIcon(EDIT_ICON);
                }
            });

            rightActions.add(edit, BorderLayout.CENTER);

            JLabel addItem = new JLabel(ADD_ICON);
            addItem.setOpaque(false);
            addItem.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    showAddClocksDialog();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    addItem.setIcon(ADD_HOVER_ICON);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    addItem.setIcon(ADD_ICON);
                }
            });
            rightActions.add(addItem, BorderLayout.LINE_END);

            topPanel.add(rightActions, BorderLayout.EAST);

            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.gridwidth = 1;
            constraints.weightx = 1;
            constraints.gridx = 0;
            constraints.gridy = 1;

            index.set(0);
            for (UUID clockId : tab.getClocks()) {
                Optional<TZClocksItem> clock = plugin.getTimezones().stream()
                        .filter(c -> c.getUuid().equals(clockId))
                        .findFirst();
                clock.ifPresent(c -> {
                    TZClocksTabItemPanel itemPanel = new TZClocksTabItemPanel(plugin, c);
                    if (index.getAndIncrement() > 0) {
                        itemsPanel.add(createMarginWrapper(itemPanel), constraints);
                    } else {
                        itemsPanel.add(itemPanel, constraints);
                    }
                    constraints.gridy++;
                });
            }
            add(topPanel, BorderLayout.NORTH);
            add(itemsPanel, BorderLayout.CENTER);
        }
    }

    private void showAddClocksDialog() {
        List<TZClocksItem> clocks = pluginPanel.getAvailableClocks();

        if (clocks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All clocks are already in tabs.",
                    "No Available Clocks", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        TZClocksSelectionPanel selectionPanel = new TZClocksSelectionPanel(this, pluginPanel, clocks);
        selectionPanel.setOnOk(e -> {
            List<TZClocksItem> selectedClocks = selectionPanel.getSelectedClocks();
            for (TZClocksItem clock : selectedClocks) {
                tab.addClock(clock.getUuid());

                // Remove clock from main panel and add to tab
                pluginPanel.removeTimezonePanel(clock);
                addClockToTab(clock); // Pass the clock (TZClocksItem) to addClockToTab
            }
            plugin.dataManager.saveData();
            pluginPanel.updatePanel();
        });
        selectionPanel.show();
    }

    private void addClockToTab(TZClocksItem clock) {
        TZClocksTabItemPanel itemPanel = new TZClocksTabItemPanel(plugin, clock);
        if (index.get() > 0) {
            itemsPanel.add(createMarginWrapper(itemPanel), constraints);
            index.getAndIncrement();
        } else {
            itemsPanel.add(itemPanel, constraints);
            index.incrementAndGet();
        }
        constraints.gridy++;
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private boolean deleteConfirm() {
        int confirm = JOptionPane.showConfirmDialog(this,
                DELETE_MESSAGE, DELETE_TITLE, JOptionPane.YES_NO_OPTION);

        return confirm == JOptionPane.YES_NO_OPTION;
    }

    private JPanel createMarginWrapper(JPanel panel) {
        JPanel marginWrapper = new JPanel(new BorderLayout());
        marginWrapper.setOpaque(false);
        marginWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
        marginWrapper.add(panel, BorderLayout.NORTH);
        return marginWrapper;
    }

    public void updateTabName(String newName) {
        Component[] components = getComponents();
        if (components.length > 0 && components[0] instanceof JPanel) {
            JPanel headerPanel = (JPanel) components[0];
            Component[] headerComponents = headerPanel.getComponents();
            if (headerComponents.length > 1 && headerComponents[1] instanceof JLabel) {
                JLabel tabNameLabel = (JLabel) headerComponents[1];
                tabNameLabel.setText(newName);
            }
        }
    }

    public TZClocksTab getTab() {
        return tab;
    }

    public void toggleTabCollapse() {
        tab.setCollapsed(!tab.isCollapsed());
        if (tab.isCollapsed()) {
            itemsPanel.removeAll();
        } else {
            constraints.gridy = 1;
            for (UUID clockId : tab.getClocks()) {
                Optional<TZClocksItem> clockItem = plugin.getTimezones().stream()
                        .filter(item -> item.getUuid().equals(clockId))
                        .findFirst();

                clockItem.ifPresent(this::addClockToTab);
            }
        }
        updateCollapseIcon();
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private void updateCollapseIcon() {
        Component[] headerComponents = ((JPanel) getComponents()[0]).getComponents();
        if (headerComponents.length > 0 && headerComponents[0] instanceof JLabel) {
            JLabel collapseButton = (JLabel) headerComponents[0];
            collapseButton.setIcon(tab.isCollapsed() ? EXPAND_ICON : COLLAPSE_ICON);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(ColorScheme.DARKER_GRAY_COLOR);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }
}