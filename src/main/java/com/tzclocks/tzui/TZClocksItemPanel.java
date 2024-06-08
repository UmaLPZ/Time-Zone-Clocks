package com.tzclocks.tzui;

import com.tzclocks.TZClocksPlugin;
import com.tzclocks.tzdata.TZClocksItem;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static com.tzclocks.tzutilities.TZConstants.*;

public class TZClocksItemPanel extends JPanel {
    private static final String DELETE_TITLE = "Warning";
    private static final String DELETE_MESSAGE = "Are you sure you want to delete this item?";
    private static final ImageIcon DELETE_ICON;
    private static final ImageIcon DELETE_HOVER_ICON;
    private static final ImageIcon EDIT_ICON;
    private static final ImageIcon EDIT_HOVER_ICON;

    private final TZClocksItem item;
    private final JLabel currentTimeLabel;
    private final JLabel timezoneNameLabel; // Label for displaying the timezone name
    private final JLabel customNameLabel; // Label for displaying the custom name
    private final TZClocksPlugin plugin; // Reference to the plugin

    static {
        final BufferedImage deleteImage = ImageUtil.loadImageResource(TZClocksItemPanel.class, DELETE_ICON_PATH);
        DELETE_ICON = new ImageIcon(deleteImage);
        DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImage, 0.53f));

        final BufferedImage editImage = ImageUtil.loadImageResource(TZClocksItemPanel.class, EDIT_ICON_PATH);
        EDIT_ICON = new ImageIcon(editImage);
        EDIT_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(editImage, 0.53f));
    }

    TZClocksItemPanel(TZClocksPlugin plugin, TZClocksItem item) {
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

        add(timezoneDetailsPanel, BorderLayout.WEST);
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

    private boolean deleteConfirm() { //self-explanatory
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
    } //updates panel with new time zones

    public TZClocksItem getItem() {
        return item;
    }
}