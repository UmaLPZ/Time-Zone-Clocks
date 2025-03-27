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
import java.awt.event.MouseListener; // Import MouseListener
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.tzclocks.tzutilities.TZConstants.*;

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
    private final JPanel timePanel; // Holds icon + time + optional date
    private final TZClocksPlugin plugin;
    // Keep references to icon labels
    private final JLabel toggleIconLabel;
    private final JLabel editButton;
    private final JLabel deleteButton;
    // Placeholder for layout stability when buttons hidden
    private final JLabel editPlaceholder;
    private final JLabel deletePlaceholder;


    static {
        // Icon loading remains the same
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
        // Main layout: Details CENTER, Actions EAST
        setLayout(new BorderLayout(5, 0)); // Keep 5px gap
        setBorder(new EmptyBorder(1, 5, 3, 0)); // Keep original border
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setOpaque(true);

        // --- Details Panel (CENTER) ---
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;

        // Initialize Name Labels
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

        // --- Time Panel (Holds Icon + Time + Optional Date) using BorderLayout ---
        timePanel = new JPanel(new BorderLayout(5, 0)); // Hgap between icon/time and date
        timePanel.setOpaque(false);
        JPanel clockIconAndTimePanel = new JPanel(new BorderLayout(5,0));
        clockIconAndTimePanel.setOpaque(false);
        clockIconAndTimePanel.add(new JLabel(CLOCK_ICON), BorderLayout.WEST);
        clockIconAndTimePanel.add(currentTimeLabel, BorderLayout.CENTER);
        timePanel.add(clockIconAndTimePanel, BorderLayout.CENTER); // Time goes in center
        // calendarPanel is added dynamically to BorderLayout.EAST

        // --- Action Panel (EAST - Apply GridLayout) ---
        // Use GridLayout(1 row, 3 columns) with 3px horizontal gap
        JPanel actionPanel = new JPanel(new GridLayout(1, 3, 3, 0));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(0, 0, 0, 3)); // Padding on the far right

        // --- Create Buttons and Placeholders ---
        toggleIconLabel = new JLabel(TOGGLE_ICON, SwingConstants.CENTER); // Center align icon
        // Listener added below

        editButton = new JLabel(EDIT_ICON, SwingConstants.CENTER); // Center align icon
        // Listener added conditionally below

        deleteButton = new JLabel(DELETE_ICON, SwingConstants.CENTER); // Center align icon
        // Listener added conditionally below

        // Create empty placeholders to maintain column width
        Dimension placeholderSize = new Dimension(EDIT_ICON.getIconWidth(), EDIT_ICON.getIconHeight()); // Use icon size
        editPlaceholder = new JLabel();
        editPlaceholder.setPreferredSize(placeholderSize);
        editPlaceholder.setMinimumSize(placeholderSize);
        deletePlaceholder = new JLabel();
        deletePlaceholder.setPreferredSize(placeholderSize);
        deletePlaceholder.setMinimumSize(placeholderSize);


        // --- Add Components to actionPanel based on logic ---
        if (!isFixedClock()) {
            // Add Toggle Button and Listener (always present conceptually, add listener here)
            toggleIconLabel.setToolTipText("Toggle Calendar");
            toggleIconLabel.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { plugin.toggleMonthDayVisibility(item); }
                @Override public void mouseEntered(MouseEvent e) { toggleIconLabel.setIcon(TOGGLE_HOVER_ICON); }
                @Override public void mouseExited(MouseEvent e) { toggleIconLabel.setIcon(TOGGLE_ICON); }
            });


            // Add Edit Button and Listener
            editButton.setToolTipText("Edit custom name"); // Ensure tooltip is set
            editButton.addMouseListener(new MouseAdapter() { // Add listener
                @Override public void mousePressed(MouseEvent e) { plugin.editClockCustomName(item); }
                @Override public void mouseEntered(MouseEvent e) { editButton.setIcon(EDIT_HOVER_ICON); }
                @Override public void mouseExited(MouseEvent e) { editButton.setIcon(EDIT_ICON); }
            });

            // Add Delete Button and Listener
            deleteButton.setToolTipText("Remove from tab"); // Ensure tooltip is set
            deleteButton.addMouseListener(new MouseAdapter() { // Add listener
                @Override public void mousePressed(MouseEvent e) { if (deleteConfirm()) plugin.removeClockFromUserTab(item); }
                @Override public void mouseEntered(MouseEvent e) { deleteButton.setIcon(DELETE_HOVER_ICON); }
                @Override public void mouseExited(MouseEvent e) { deleteButton.setIcon(DELETE_ICON); }
            });

            // Add in Toggle, Edit, Delete order for non-fixed clocks
            actionPanel.add(toggleIconLabel); // Column 1
            actionPanel.add(editButton);      // Column 2
            actionPanel.add(deleteButton);    // Column 3
        } else {
            // Fixed clock: Only toggle is active
            // Add Toggle Button and Listener
            toggleIconLabel.setToolTipText("Toggle Calendar");
            toggleIconLabel.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { plugin.toggleMonthDayVisibility(item); }
                @Override public void mouseEntered(MouseEvent e) { toggleIconLabel.setIcon(TOGGLE_HOVER_ICON); }
                @Override public void mouseExited(MouseEvent e) { toggleIconLabel.setIcon(TOGGLE_ICON); }
            });

            // Ensure tooltips are null for inactive buttons
            editButton.setToolTipText(null);
            deleteButton.setToolTipText(null);
            // Remove listeners just in case they were somehow added before
            for(MouseListener ml : editButton.getMouseListeners()){ editButton.removeMouseListener(ml); }
            for(MouseListener ml : deleteButton.getMouseListeners()){ deleteButton.removeMouseListener(ml); }

            // Add placeholders first, then the toggle icon in the last slot
            actionPanel.add(editPlaceholder);    // Column 1 (Placeholder)
            actionPanel.add(deletePlaceholder);  // Column 2 (Placeholder)
            actionPanel.add(toggleIconLabel);    // Column 3 (Toggle Icon)
        }

        // --- NEW: Wrapper Panel for Top Alignment ---
        JPanel eastWrapperPanel = new JPanel(new BorderLayout());
        eastWrapperPanel.setOpaque(false);
        eastWrapperPanel.setBorder(new EmptyBorder(0, 0, 0, 3)); // Padding on right
        eastWrapperPanel.add(actionPanel, BorderLayout.NORTH); // Add actionPanel to top of wrapper

        // Add main panels to this component
        add(detailsPanel, BorderLayout.CENTER);
        add(eastWrapperPanel, BorderLayout.EAST); // Add the WRAPPER to the main EAST

        // Initial UI state setup
        updateCustomName(detailsPanel, gbc, namePanel); // Lays out names and adds timePanel
        toggleMonthDayVisibility(); // Adds/removes calendarPanel from timePanel
    }

    // isFixedClock remains the same
    private boolean isFixedClock() {
        return item.getUuid().equals(TZClocksPlugin.LOCAL_CLOCK_UUID) ||
                item.getUuid().equals(TZClocksPlugin.JAGEX_CLOCK_UUID);
    }

    // updateCustomName remains the same
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

    // toggleMonthDayVisibility remains the same (operates on timePanel)
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
            // Add calendarPanel to EAST of timePanel
            timePanel.add(calendarPanel, BorderLayout.EAST);
        }
        timePanel.revalidate();
        timePanel.repaint();
    }

    // deleteConfirm remains the same
    private boolean deleteConfirm() {
        int confirm = JOptionPane.showConfirmDialog(this,
                DELETE_MESSAGE, DELETE_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return confirm == JOptionPane.YES_OPTION;
    }

    // updateTime remains the same
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