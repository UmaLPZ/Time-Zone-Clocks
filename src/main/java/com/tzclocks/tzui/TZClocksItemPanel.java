package com.tzclocks.tzui;

import com.tzclocks.TZClocksPlugin;
import com.tzclocks.tzconfig.TZClocksConfig;
import com.tzclocks.tzdata.TZClocksItem;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.tzclocks.tzutilities.TZConstants.*;

public class TZClocksItemPanel extends JPanel {

    private static final String DELETE_TITLE = "Warning";
    private static final String DELETE_MESSAGE = "Are you sure you want to delete this item?";
    private static final ImageIcon DELETE_ICON;
    private static final ImageIcon DELETE_HOVER_ICON;
    private static final ImageIcon EDIT_ICON;
    private static final ImageIcon EDIT_HOVER_ICON;
    private static final ImageIcon GLOBE_ICON;
    private static final ImageIcon CLOCK_ICON;
    private static final ImageIcon CALENDAR_ICON;
    private static final ImageIcon TOGGLE_ICON;

    private final TZClocksItem item;
    private final JLabel currentTimeLabel;
    private final JLabel timezoneNameLabel;
    private final JLabel customNameLabel;
    private final JLabel dayMonthLabel;
      private JPanel calendarPanel;
    private JPanel timePanel;

    private final TZClocksPlugin plugin;

    static {
        final BufferedImage deleteImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksItemPanel.class, DELETE_ICON_PATH), 10, 10);
        DELETE_ICON = new ImageIcon(deleteImage);
        DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImage, 0.53f));

        final BufferedImage editImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksItemPanel.class, EDIT_ICON_PATH), 10, 10);
        EDIT_ICON = new ImageIcon(editImage);
        EDIT_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(editImage, 0.53f));

        final BufferedImage toggleImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksItemPanel.class, TOGGLE_ICON_PATH), 10, 10);
        TOGGLE_ICON = new ImageIcon(toggleImage);

        final BufferedImage globeImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksItemPanel.class, GLOBE_ICON_PATH), 10, 10);
        GLOBE_ICON = new ImageIcon(globeImage);

        final BufferedImage clockImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksItemPanel.class, CLOCK_ICON_PATH), 10, 10);
        CLOCK_ICON = new ImageIcon(clockImage);

        final BufferedImage calendarImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksItemPanel.class, CALENDAR_ICON_PATH), 10, 10);
        CALENDAR_ICON = new ImageIcon(calendarImage);
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
        JPanel namePanel = new JPanel(new BorderLayout(5, 0));
        namePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        JLabel globeIconLabel = new JLabel(GLOBE_ICON);
        namePanel.add(globeIconLabel, BorderLayout.WEST);
        namePanel.add(timezoneNameLabel, BorderLayout.CENTER);
        timezoneDetailsPanel.add(namePanel, gbc);

        gbc.gridy++;
        currentTimeLabel = new JLabel();
        currentTimeLabel.setForeground(Color.WHITE);
        currentTimeLabel.setText(item.getCurrentTime());


        timePanel = new JPanel(new BorderLayout(5, 0));
        timePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JLabel clockIconLabel = new JLabel(CLOCK_ICON);
        timePanel.add(clockIconLabel, BorderLayout.WEST);


        calendarPanel = new JPanel(new BorderLayout(0, 0));
        calendarPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        JLabel calendarIconLabel = new JLabel(CALENDAR_ICON);
        calendarPanel.add(calendarIconLabel, BorderLayout.WEST);


        ZoneId zoneId = ZoneId.of(item.getName());
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        LocalDate currentDate = zonedDateTime.toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        String monthDayText = currentDate.format(formatter);


        dayMonthLabel = new JLabel();
        dayMonthLabel.setForeground(Color.WHITE);
        dayMonthLabel.setText(monthDayText);
        calendarPanel.add(dayMonthLabel, BorderLayout.CENTER);
        timePanel.add(currentTimeLabel, BorderLayout.CENTER);

        timezoneDetailsPanel.add(timePanel, gbc);


        updateCustomName();
        toggleMonthDayVisibility();

        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        actionPanel.setBackground(new Color(0, 0, 0, 0));
        actionPanel.setOpaque(false);


        JLabel toggleIcon = new JLabel(TOGGLE_ICON);
        toggleIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                plugin.toggleMonthDayVisibility(item);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                toggleIcon.setToolTipText(item.getShowCalendar() != null && item.getShowCalendar().equals("active") ? "Hide Date" : "Show Date");
            }
        });

        actionPanel.add(toggleIcon);


        JLabel editButton = new JLabel(EDIT_ICON);
        editButton.setBorder(new EmptyBorder(0, 0, 0, 5));
        editButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                plugin.editClockCustomName(item);
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


        JLabel deleteButton = new JLabel(DELETE_ICON);
        deleteButton.setBorder(new EmptyBorder(0, 0, 0, 3));
        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (deleteConfirm()) {
                    plugin.removeTimezoneFromPanel(item);
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
            timezoneNameLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        } else {
            customNameLabel.setText("");
            timezoneNameLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
        }
    }

    private void toggleMonthDayVisibility() {
        if (item.getShowCalendar() != null) {
            item.setShowCalendar("active");
            timePanel.add(calendarPanel, BorderLayout.EAST);
        } else {
            item.setShowCalendar(null);
            timePanel.remove(calendarPanel);
        }
        timePanel.revalidate();
        timePanel.repaint();
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

}