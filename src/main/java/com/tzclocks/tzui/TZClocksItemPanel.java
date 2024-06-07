package com.tzclocks.tzui;

import com.tzclocks.TZClocksPlugin;
import com.tzclocks.tzdata.TZClocksItem;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TZClocksItemPanel extends JPanel {

    private final TZClocksItem item;
    private final JLabel currentTimeLabel;
    private final JLabel timezoneNameLabel;
    private final JLabel customNameLabel;

    TZClocksItemPanel(TZClocksPlugin plugin, TZClocksItem item) {
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
            timezoneNameLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        } else {
            customNameLabel.setText("");
            timezoneNameLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
        }
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