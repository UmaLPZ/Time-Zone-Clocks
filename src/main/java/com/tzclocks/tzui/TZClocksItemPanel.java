package com.tzclocks.tzui;

import com.tzclocks.TZClocksPlugin;
// No config needed here directly
// import com.tzclocks.tzconfig.TZClocksConfig;
import com.tzclocks.tzdata.TZClocksItem;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

// No Inject needed here directly
// import javax.inject.Inject;
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
    // Message is different: delete the item entirely
    private static final String DELETE_MESSAGE = "Are you sure you want to permanently delete this clock?";
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
    private final JPanel calendarPanel; // Panel holding calendar icon and label
    private final JPanel timePanel; // Panel holding clock icon, time label, and calendar panel
    private final TZClocksPlugin plugin;

    static {
        // Icon loading remains the same as TZClocksTabItemPanel
        final int ICON_SIZE = 10;
        final float ALPHA_HOVER = 0.53f;

        final BufferedImage deleteImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksItemPanel.class, DELETE_ICON_PATH), ICON_SIZE, ICON_SIZE);
        DELETE_ICON = new ImageIcon(deleteImage);
        DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImage, ALPHA_HOVER));

        final BufferedImage editImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksItemPanel.class, EDIT_ICON_PATH), ICON_SIZE, ICON_SIZE);
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

    TZClocksItemPanel(TZClocksPlugin plugin, TZClocksItem item) {
        this.plugin = plugin;
        this.item = item;
        setLayout(new BorderLayout(5, 0));
        setBorder(new EmptyBorder(3, 5, 3, 0)); // Original padding
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setOpaque(true);

        // --- Details Panel (CENTER) ---
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;

        customNameLabel = new JLabel();
        customNameLabel.setForeground(Color.WHITE);

        timezoneNameLabel = new JLabel(item.getName());
        timezoneNameLabel.setForeground(Color.WHITE);
        JPanel namePanel = new JPanel(new BorderLayout(5, 0));
        namePanel.setOpaque(false);
        namePanel.add(new JLabel(GLOBE_ICON), BorderLayout.WEST);
        namePanel.add(timezoneNameLabel, BorderLayout.CENTER);

        // Initialize Time/Date Components
        currentTimeLabel = new JLabel(item.getCurrentTime());
        currentTimeLabel.setForeground(Color.WHITE);

        dayMonthLabel = new JLabel();
        dayMonthLabel.setForeground(Color.WHITE);
        calendarPanel = new JPanel(new BorderLayout(0, 0)); // Icon + Date
        calendarPanel.setOpaque(false);
        calendarPanel.add(new JLabel(CALENDAR_ICON), BorderLayout.WEST);
        calendarPanel.add(dayMonthLabel, BorderLayout.CENTER);

        timePanel = new JPanel(new BorderLayout(5, 0)); // Hgap between icon/time and date
        timePanel.setOpaque(false);
        JPanel clockIconAndTimePanel = new JPanel(new BorderLayout(5,0));
        clockIconAndTimePanel.setOpaque(false);
        clockIconAndTimePanel.add(new JLabel(CLOCK_ICON), BorderLayout.WEST);
        clockIconAndTimePanel.add(currentTimeLabel, BorderLayout.CENTER);
        timePanel.add(clockIconAndTimePanel, BorderLayout.CENTER); // Time goes in center


        // --- Action Panel (EAST) ---
        JPanel actionPanel = new JPanel(new BorderLayout(0, 0)); // Use BorderLayout
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(0, 0, 0, 3)); // Padding on the far right

        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        togglePanel.setOpaque(false);
        JLabel toggleButton = new JLabel(TOGGLE_ICON);
        toggleButton.setToolTipText("Toggle Calendar");
        toggleButton.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { plugin.toggleMonthDayVisibility(item); }
            @Override public void mouseEntered(MouseEvent e) { toggleButton.setIcon(TOGGLE_HOVER_ICON); }
            @Override public void mouseExited(MouseEvent e) { toggleButton.setIcon(TOGGLE_ICON); }
        });
        togglePanel.add(toggleButton);
        // Add deletePanel to the EAST part of actionPanel
        actionPanel.add(togglePanel, BorderLayout.WEST);

        JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        editPanel.setOpaque(false);
        JLabel editButton = new JLabel(EDIT_ICON);
        editButton.setToolTipText("Edit custom name");

        editButton.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { plugin.editClockCustomName(item); }
            @Override public void mouseEntered(MouseEvent e) { editButton.setIcon(EDIT_HOVER_ICON); }
            @Override public void mouseExited(MouseEvent e) { editButton.setIcon(EDIT_ICON); }
        });
        editPanel.add(editButton);
        // Add bottomActionsPanel to the WEST part of actionPanel
        actionPanel.add(editPanel, BorderLayout.CENTER);

        // Panel for Delete Button (Aligned Right in EAST slot)
        JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
        deletePanel.setOpaque(false);
        JLabel deleteButton = new JLabel(DELETE_ICON);
        deleteButton.setToolTipText("Delete this clock permanently"); // Updated tooltip
        deleteButton.addMouseListener(new MouseAdapter() {
            // Delete action is different: calls removeTimezoneFromPanel
            @Override public void mousePressed(MouseEvent e) { if (deleteConfirm()) plugin.removeTimezoneFromPanel(item); }
            @Override public void mouseEntered(MouseEvent e) { deleteButton.setIcon(DELETE_HOVER_ICON); }
            @Override public void mouseExited(MouseEvent e) { deleteButton.setIcon(DELETE_ICON); }
        });
        deletePanel.add(deleteButton);
        // Add deletePanel to the EAST part of actionPanel
        actionPanel.add(deletePanel, BorderLayout.EAST);

        // Add main panels to this component
        add(detailsPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.EAST);

        // Initial UI state setup
        updateCustomName(detailsPanel, gbc, namePanel); // Lays out names and adds timePanel
        toggleMonthDayVisibility(); // Adds/removes calendarPanel from timePanel
    }

    /**
     * Updates layout based on custom name presence. Adds name rows and the timePanel.
     */
    private void updateCustomName(JPanel detailsPanel, GridBagConstraints gbc, JPanel namePanel) {
        detailsPanel.removeAll();
        if (item.getCustomName() != null && !item.getCustomName().isEmpty()) {
            customNameLabel.setText(item.getCustomName());
            gbc.gridy = 0; detailsPanel.add(customNameLabel, gbc); // Row 0: Custom Name
            gbc.gridy = 1; detailsPanel.add(namePanel, gbc); // Row 1: Original Name
            timezoneNameLabel.setBorder(new EmptyBorder(5, 0, 0, 0)); // Spacing below original name
            gbc.gridy = 2; detailsPanel.add(timePanel, gbc); // Row 2: Time Row
        } else {
            gbc.gridy = 0; detailsPanel.add(namePanel, gbc); // Row 0: Original Name
            timezoneNameLabel.setBorder(new EmptyBorder(0, 0, 0, 0)); // No spacing needed
            gbc.gridy = 1; detailsPanel.add(timePanel, gbc); // Row 1: Time Row
        }
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    /**
     * Updates the tooltip for the date toggle button.
     */

    /**
     * Adds or removes the calendar panel within the timePanel (in EAST position).
     */
    private void toggleMonthDayVisibility() {
        timePanel.remove(calendarPanel); // Always remove first
        if (item.getShowCalendar() != null) { // Check flag
            try { // Update date text
                ZoneId zoneId = ZoneId.of(item.getName());
                ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
                LocalDate currentDate = zonedDateTime.toLocalDate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
                dayMonthLabel.setText(currentDate.format(formatter));
            } catch (Exception e) { dayMonthLabel.setText("?? ??"); }
            // Add calendar panel to the EAST side of the time panel
            timePanel.add(calendarPanel, BorderLayout.EAST);
        }
        // Refresh layout of the time panel
        timePanel.revalidate();
        timePanel.repaint();
        // Update tooltip for the toggle button
    }

    /**
     * Shows confirmation dialog for deleting the clock permanently.
     */
    private boolean deleteConfirm() {
        int confirm = JOptionPane.showConfirmDialog(this,
                DELETE_MESSAGE, // Use the specific message for this panel
                DELETE_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return confirm == JOptionPane.YES_OPTION;
    }

    /**
     * Updates the displayed time and potentially the date. Called by the plugin scheduler.
     */
    public void updateTime() {
        // Always update the time label
        currentTimeLabel.setText(item.getCurrentTime());

        // Update date label only if it's supposed to be visible
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

    // Removed paintComponent override - not needed
}