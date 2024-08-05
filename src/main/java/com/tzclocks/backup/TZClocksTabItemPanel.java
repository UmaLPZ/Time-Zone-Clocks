package com.tzclocks.backup;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static com.tzclocks.tzutilities.TZConstants.DELETE_ICON_PATH;
import static com.tzclocks.tzutilities.TZConstants.EDIT_ICON_PATH;

public class TZClocksTabItemPanel extends JPanel {
    private static final String DELETE_TITLE = "Warning";
    private static final String DELETE_MESSAGE = "Are you sure you want to remove this clock from the tab?";
    private static final ImageIcon DELETE_ICON;
    private static final ImageIcon DELETE_HOVER_ICON;
    private static final ImageIcon EDIT_ICON; // Add edit icon
    private static final ImageIcon EDIT_HOVER_ICON; // Add edit hover icon

    private final TZClocksItem item;
    private final JLabel currentTimeLabel;
    private final JLabel timezoneNameLabel; // Label for displaying the timezone name
    private final JLabel customNameLabel; // Label for displaying the custom name
    private final TZClocksPlugin plugin; // Reference to the plugin

    static {
        final BufferedImage deleteImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksTabItemPanel.class, DELETE_ICON_PATH), 10, 10);
        DELETE_ICON = new ImageIcon(deleteImage);
        DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImage, 0.53f));

        // Load and resize the edit icon
        final BufferedImage editImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksTabItemPanel.class, EDIT_ICON_PATH), 10, 10);
        EDIT_ICON = new ImageIcon(editImage);
        EDIT_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(editImage, 0.53f));
    }

   public TZClocksTabItemPanel(TZClocksPlugin plugin, TZClocksItem item) {
        this.plugin = plugin;
        this.item = item;
        setLayout(new BorderLayout(5, 0));
        setBorder(new EmptyBorder(5, 5, 5, 0));

        JPanel timezoneDetailsPanel = new JPanel(new GridBagLayout());
        timezoneDetailsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;

        customNameLabel = new JLabel();
        customNameLabel.setForeground(Color.WHITE);
        timezoneDetailsPanel.add(customNameLabel, gbc);

        gbc.gridy++;
        timezoneNameLabel = new JLabel();
        timezoneNameLabel.setForeground(Color.WHITE);
        timezoneNameLabel.setText(item.getName());
        timezoneDetailsPanel.add(timezoneNameLabel, gbc);

        gbc.gridy++;
        currentTimeLabel = new JLabel();
        currentTimeLabel.setForeground(Color.WHITE);
        currentTimeLabel.setText(item.getCurrentTime());
        timezoneDetailsPanel.add(currentTimeLabel, gbc);

        updateCustomName();

        // Action Panel (Delete, Edit)
       JPanel actionPanel = new JPanel(new GridLayout(2, 1, 0, 0)); // 2 rows, 1 column
       actionPanel.setBackground(new Color(0, 0, 0, 0));
       actionPanel.setOpaque(false);

        // Edit button
        JLabel editButton = new JLabel(EDIT_ICON);
        editButton.setBorder(new EmptyBorder(0, 0, 0, 5));
        editButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                plugin.editClockCustomName(item); // Call editClockCustomName in TZClocksPlugin
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                editButton.setIcon(EDIT_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                editButton.setIcon(EDIT_ICON);
            }
        });
        actionPanel.add(editButton);

        // Delete button
        JLabel deleteButton = new JLabel(DELETE_ICON);
        deleteButton.setBorder(new EmptyBorder(0, 0, 0, 3));
        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (deleteConfirm()) {
                    plugin.removeClockFromTab(item);
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
        actionPanel.add(deleteButton);

        add(timezoneDetailsPanel, BorderLayout.WEST);
        add(actionPanel, BorderLayout.EAST);
    }

    private void updateCustomName() {
        if (item.getCustomName() != null) {
            customNameLabel.setText(item.getCustomName());
            timezoneNameLabel.setBorder(new EmptyBorder(5, 0, 0, 0)); // Add spacing above timezone name
        } else {
            customNameLabel.setText(""); // Blank by default
            timezoneNameLabel.setBorder(new EmptyBorder(0, 0, 0, 0)); // Remove spacing
        }
    }


    private boolean deleteConfirm() {
        int confirm = JOptionPane.showConfirmDialog(this,
                DELETE_MESSAGE, DELETE_TITLE, JOptionPane.YES_NO_OPTION);
        return confirm == JOptionPane.YES_NO_OPTION;
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(ColorScheme.DARKER_GRAY_COLOR);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    public void updateTime() {
        currentTimeLabel.setText(item.getCurrentTime());
    }

    public TZClocksItem getItem() {
        return item;
    }
}