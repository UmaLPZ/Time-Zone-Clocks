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
    private static final ImageIcon DELETE_HOVER_ICON;;

    private final TZClocksItem item;
    private final JLabel currentTimeLabel;

    static {
        final BufferedImage deleteImage = ImageUtil.loadImageResource(TZClocksItemPanel.class, DELETE_ICON_PATH);
        DELETE_ICON = new ImageIcon(deleteImage);
        DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImage, 0.53f));


    }

    TZClocksItemPanel(TZClocksPlugin plugin, TZClocksItem item) { //format and buttons for added time zones
        this.item = item;
        setLayout(new BorderLayout(5, 0));
        setBorder(new EmptyBorder(5, 5, 5, 0));

        int itemIndex = plugin.getTimezones().indexOf(item); //need to check if actually used or not
        int itemsSize = plugin.getTimezones().size(); //same as above

        JPanel timezoneDetailsPanel = new JPanel(new GridLayout(2, 1));
        timezoneDetailsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JLabel timezoneName = new JLabel();
        timezoneName.setForeground(Color.WHITE);
        timezoneName.setText(item.getName());
        timezoneDetailsPanel.add(timezoneName);

        currentTimeLabel = new JLabel();
        currentTimeLabel.setForeground(Color.WHITE);
        currentTimeLabel.setText(item.getCurrentTime());
        timezoneDetailsPanel.add(currentTimeLabel);
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBackground(new Color(0, 0, 0, 0));
        actionPanel.setOpaque(false);
        JLabel deleteItem = new JLabel(DELETE_ICON);
        deleteItem.setBorder(new EmptyBorder(0, 0, 0, 3));
        deleteItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (deleteConfirm()) {
                    plugin.removeTimezoneFromPanel(item);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                deleteItem.setIcon(DELETE_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                deleteItem.setIcon(DELETE_ICON);
            }
        });
        actionPanel.add(deleteItem, BorderLayout.NORTH);

        add(timezoneDetailsPanel, BorderLayout.WEST);
        add(actionPanel, BorderLayout.EAST);
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
}