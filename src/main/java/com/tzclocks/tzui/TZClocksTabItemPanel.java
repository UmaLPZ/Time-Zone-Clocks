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
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.tzclocks.tzutilities.TZConstants.*;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createRaisedBevelBorder;

public class TZClocksTabItemPanel extends JPanel {
    private static final String DELETE_TITLE = "Remove Clock";
    private static final String DELETE_MESSAGE = "Are you sure you want to remove this clock from the tab?";
    private static final ImageIcon DELETE_ICON;
    private static final ImageIcon DELETE_HOVER_ICON;
    private static final ImageIcon EDIT_ICON;
    private static final ImageIcon EDIT_HOVER_ICON;
    private static final ImageIcon GLOBE_ICON;
    private static final ImageIcon CLOCK_ICON;
    private static final ImageIcon CALENDAR_ICON;
    private static final ImageIcon TOGGLE_ICON;
    private static final ImageIcon TOGGLE_HOVER_ICON;

    private final TZClocksItem item;
    private final JLabel currentTimeLabel;
    private final JLabel timezoneNameLabel;
    private final JLabel customNameLabel;
    private final JLabel dayMonthLabel;
    private final JPanel calendarPanel;
    private final JPanel timePanel;
    private final TZClocksPlugin plugin;

    private final JLabel toggleIconLabel;
    private final JLabel editButton;
    private final JLabel deleteButton;

    private final JLabel editPlaceholder;
    private final JLabel deletePlaceholder;


    static {

        final int ICON_SIZE = 10;
        final float ALPHA_HOVER = 0.53f;

        final BufferedImage deleteImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksTabItemPanel.class, DELETE_ICON_PATH), ICON_SIZE, ICON_SIZE);
        DELETE_ICON = new ImageIcon(deleteImage);
        DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImage, ALPHA_HOVER));

        final BufferedImage editImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksTabItemPanel.class, EDIT_ICON_PATH), ICON_SIZE, ICON_SIZE);
        EDIT_ICON = new ImageIcon(editImage);
        EDIT_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(editImage, ALPHA_HOVER));

        final BufferedImage toggleImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksItemPanel.class, TOGGLE_ICON_PATH), ICON_SIZE, ICON_SIZE);
        TOGGLE_ICON = new ImageIcon(toggleImage);
        TOGGLE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(toggleImage, ALPHA_HOVER));

        final BufferedImage globeImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksItemPanel.class, GLOBE_ICON_PATH), ICON_SIZE, ICON_SIZE);
        GLOBE_ICON = new ImageIcon(globeImage);

        final BufferedImage clockImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksItemPanel.class, CLOCK_ICON_PATH), ICON_SIZE, ICON_SIZE);
        CLOCK_ICON = new ImageIcon(clockImage);

        final BufferedImage calendarImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksItemPanel.class, CALENDAR_ICON_PATH), ICON_SIZE, ICON_SIZE);
        CALENDAR_ICON = new ImageIcon(calendarImage);
    }

    public TZClocksTabItemPanel(TZClocksPlugin plugin, TZClocksItem item) {
        this.plugin = plugin;
        this.item = item;

        setLayout(new BorderLayout(5, 0));
        setBorder(BorderFactory.createCompoundBorder(createRaisedBevelBorder(), createEmptyBorder(3,5,5,5)));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setOpaque(true);


        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;


        customNameLabel = new JLabel();
        customNameLabel.setForeground(Color.WHITE);

        timezoneNameLabel = new JLabel(truncateString(item.getDisplayName(),20));
        timezoneNameLabel.setForeground(Color.WHITE);
        timezoneNameLabel.setToolTipText(item.getDisplayName());
        JPanel namePanel = new JPanel(new BorderLayout(5, 0));
        namePanel.setOpaque(false);
        namePanel.add(new JLabel(GLOBE_ICON), BorderLayout.WEST);
        namePanel.add(timezoneNameLabel, BorderLayout.CENTER);


        currentTimeLabel = new JLabel(item.getCurrentTime());
        currentTimeLabel.setForeground(Color.WHITE);

        dayMonthLabel = new JLabel();
        dayMonthLabel.setForeground(Color.WHITE);
        calendarPanel = new JPanel(new BorderLayout(0, 0));
        calendarPanel.setOpaque(false);
        calendarPanel.add(new JLabel(CALENDAR_ICON), BorderLayout.WEST);
        calendarPanel.add(dayMonthLabel, BorderLayout.CENTER);


        timePanel = new JPanel(new BorderLayout(5, 0));
        timePanel.setOpaque(false);
        JPanel clockIconAndTimePanel = new JPanel(new BorderLayout(5,0));
        clockIconAndTimePanel.setOpaque(false);
        clockIconAndTimePanel.add(new JLabel(CLOCK_ICON), BorderLayout.WEST);
        clockIconAndTimePanel.add(currentTimeLabel, BorderLayout.CENTER);
        timePanel.add(clockIconAndTimePanel, BorderLayout.CENTER);




        JPanel actionPanel = new JPanel(new GridLayout(1, 3, 3, 0));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(0, 0, 0, 3));


        toggleIconLabel = new JLabel(TOGGLE_ICON, SwingConstants.CENTER);


        editButton = new JLabel(EDIT_ICON, SwingConstants.CENTER);


        deleteButton = new JLabel(DELETE_ICON, SwingConstants.CENTER);



        Dimension placeholderSize = new Dimension(EDIT_ICON.getIconWidth(), EDIT_ICON.getIconHeight());
        editPlaceholder = new JLabel();
        editPlaceholder.setPreferredSize(placeholderSize);
        editPlaceholder.setMinimumSize(placeholderSize);
        deletePlaceholder = new JLabel();
        deletePlaceholder.setPreferredSize(placeholderSize);
        deletePlaceholder.setMinimumSize(placeholderSize);



        if (!isFixedClock()) {

            toggleIconLabel.setToolTipText("Toggle Calendar");
            toggleIconLabel.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { plugin.toggleMonthDayVisibility(item); }
                @Override public void mouseEntered(MouseEvent e) { toggleIconLabel.setIcon(TOGGLE_HOVER_ICON); }
                @Override public void mouseExited(MouseEvent e) { toggleIconLabel.setIcon(TOGGLE_ICON); }
            });



            editButton.setToolTipText("Edit custom name");
            editButton.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { plugin.editClockCustomName(item); }
                @Override public void mouseEntered(MouseEvent e) { editButton.setIcon(EDIT_HOVER_ICON); }
                @Override public void mouseExited(MouseEvent e) { editButton.setIcon(EDIT_ICON); }
            });


            deleteButton.setToolTipText("Remove from tab");
            deleteButton.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { if (deleteConfirm()) plugin.removeClockFromUserTab(item); }
                @Override public void mouseEntered(MouseEvent e) { deleteButton.setIcon(DELETE_HOVER_ICON); }
                @Override public void mouseExited(MouseEvent e) { deleteButton.setIcon(DELETE_ICON); }
            });


            actionPanel.add(toggleIconLabel);
            actionPanel.add(editButton);
            actionPanel.add(deleteButton);
        } else {


            toggleIconLabel.setToolTipText("Toggle Calendar");
            toggleIconLabel.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { plugin.toggleMonthDayVisibility(item); }
                @Override public void mouseEntered(MouseEvent e) { toggleIconLabel.setIcon(TOGGLE_HOVER_ICON); }
                @Override public void mouseExited(MouseEvent e) { toggleIconLabel.setIcon(TOGGLE_ICON); }
            });


            editButton.setToolTipText(null);
            deleteButton.setToolTipText(null);

            for(MouseListener ml : editButton.getMouseListeners()){ editButton.removeMouseListener(ml); }
            for(MouseListener ml : deleteButton.getMouseListeners()){ deleteButton.removeMouseListener(ml); }


            actionPanel.add(editPlaceholder);
            actionPanel.add(deletePlaceholder);
            actionPanel.add(toggleIconLabel);
        }


        JPanel eastWrapperPanel = new JPanel(new BorderLayout());
        eastWrapperPanel.setOpaque(false);
        eastWrapperPanel.setBorder(new EmptyBorder(0, 0, 0, 3));
        eastWrapperPanel.add(actionPanel, BorderLayout.NORTH);


        add(detailsPanel, BorderLayout.CENTER);
        add(eastWrapperPanel, BorderLayout.EAST);


        updateCustomName(detailsPanel, gbc, namePanel);
        toggleMonthDayVisibility();
    }


    private boolean isFixedClock() {
        return item.getUuid().equals(TZClocksPlugin.LOCAL_CLOCK_UUID) ||
                item.getUuid().equals(TZClocksPlugin.JAGEX_CLOCK_UUID);
    }


    private void updateCustomName(JPanel detailsPanel, GridBagConstraints gbc, JPanel namePanel) {
        detailsPanel.removeAll();
        if (item.getCustomName() != null && !item.getCustomName().isEmpty()) {
            customNameLabel.setText(item.getCustomName());
            gbc.gridy = 0; detailsPanel.add(customNameLabel, gbc);
            gbc.gridy = 1; detailsPanel.add(namePanel, gbc);
            timezoneNameLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
            gbc.gridy = 2; detailsPanel.add(timePanel, gbc);
        } else {
            gbc.gridy = 0; detailsPanel.add(namePanel, gbc);
            timezoneNameLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
            gbc.gridy = 1; detailsPanel.add(timePanel, gbc);
        }
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }


    private void toggleMonthDayVisibility() {
        boolean calendarVisible = false;
        for (Component comp : timePanel.getComponents()) {
            if (comp == calendarPanel) {
                calendarVisible = true;
                break;
            }
        }
        if (calendarVisible) { timePanel.remove(calendarPanel); }
        if (item.getShowCalendar() != null) {
            try {
                ZoneId zoneId = ZoneId.of(item.getName());
                ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
                LocalDate currentDate = zonedDateTime.toLocalDate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
                dayMonthLabel.setText(currentDate.format(formatter));
            } catch (Exception e) { dayMonthLabel.setText("?? ??"); }

            timePanel.add(calendarPanel, BorderLayout.EAST);
        }
        timePanel.revalidate();
        timePanel.repaint();
    }


    private boolean deleteConfirm() {
        int confirm = JOptionPane.showConfirmDialog(this,
                DELETE_MESSAGE, DELETE_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return confirm == JOptionPane.YES_OPTION;
    }


    public void updateTime() {
        currentTimeLabel.setText(item.getCurrentTime());
        if (item.getShowCalendar() != null) {
            try {
                ZoneId zoneId = ZoneId.of(item.getName());
                ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
                LocalDate currentDate = zonedDateTime.toLocalDate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
                String currentFormattedDate = currentDate.format(formatter);
                if (!dayMonthLabel.getText().equals(currentFormattedDate)) {
                    dayMonthLabel.setText(currentFormattedDate);
                }
            } catch (Exception e) {
                if (!dayMonthLabel.getText().equals("?? ??")) {
                    dayMonthLabel.setText("?? ??");
                }
            }
        }
    }

}