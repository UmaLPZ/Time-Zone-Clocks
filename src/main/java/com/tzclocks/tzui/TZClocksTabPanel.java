package com.tzclocks.tzui;

import com.tzclocks.TZClocksPlugin;
import com.tzclocks.tzdata.TZClocksItem;
import com.tzclocks.tzdata.TZClocksTab;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

import static com.tzclocks.tzutilities.TZConstants.*;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createEtchedBorder;

public class TZClocksTabPanel extends JPanel {
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
    private static final ImageIcon EXPAND_DOWN_ICON;
    private static final ImageIcon EXPAND_DOWN_HOVER_ICON;


    private final TZClocksPlugin plugin;
    @Getter
    private final TZClocksTab tab;
    private final TZClocksPluginPanel pluginPanel;
    private final JPanel itemsPanel;
    private final GridBagConstraints constraints = new GridBagConstraints();
    @Getter
    private final Map<TZClocksItem, TZClocksTabItemPanel> tabItemPanelsMap = new HashMap<>();

    static {

        final int ICON_SIZE = 12;
        final float ALPHA_NORMAL = 0.7f;
        final float ALPHA_HOVER = 0.53f;

        final BufferedImage addImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksPlugin.class, ADD_ICON_PATH), ICON_SIZE, ICON_SIZE);
        ADD_ICON = new ImageIcon(ImageUtil.alphaOffset(addImage, ALPHA_NORMAL));
        ADD_HOVER_ICON = new ImageIcon(addImage);

        final BufferedImage editImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksPlugin.class, EDIT_ICON_PATH), ICON_SIZE, ICON_SIZE);
        EDIT_ICON = new ImageIcon(ImageUtil.alphaOffset(editImage, ALPHA_NORMAL));
        EDIT_HOVER_ICON = new ImageIcon(editImage);

        final BufferedImage deleteTabImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksPlugin.class, DELETE_ICON_PATH), ICON_SIZE, ICON_SIZE);
        DELETE_TAB_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteTabImage, ALPHA_NORMAL));
        DELETE_TAB_HOVER_ICON = new ImageIcon(deleteTabImage);



        final BufferedImage collapseImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksPlugin.class, COLLAPSE_ICON_PATH), ICON_SIZE, ICON_SIZE);
        COLLAPSE_ICON = new ImageIcon(collapseImage);
        COLLAPSE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(collapseImage, ALPHA_HOVER));


        final BufferedImage expandDownImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(TZClocksPlugin.class, EXPAND_ICON_PATH), ICON_SIZE, ICON_SIZE);
        EXPAND_DOWN_ICON = new ImageIcon(expandDownImage);
        EXPAND_DOWN_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(expandDownImage, ALPHA_HOVER));
    }


    public TZClocksTabPanel(TZClocksPlugin plugin, TZClocksPluginPanel panel, TZClocksTab tab) {
        this.plugin = plugin;
        this.pluginPanel = panel;
        this.tab = tab;


        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createCompoundBorder(createEtchedBorder(EtchedBorder.RAISED), createEmptyBorder(0,0,0,0)));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setOpaque(true);


        boolean isCollapsed = tab.isCollapsed();


        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        headerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));


        JPanel leftActions = new JPanel(new BorderLayout(5, 0));
        leftActions.setOpaque(false);

        JLabel collapseButton = new JLabel();
        collapseButton.setOpaque(false);


        final ImageIcon currentIcon;
        final ImageIcon hoverIcon;
        if (isCollapsed) {
            currentIcon = EXPAND_DOWN_ICON;
            hoverIcon = EXPAND_DOWN_HOVER_ICON;
            collapseButton.setToolTipText("Expand Tab");
        } else {
            currentIcon = COLLAPSE_ICON;
            hoverIcon = COLLAPSE_HOVER_ICON;
            collapseButton.setToolTipText("Collapse Tab");
        }
        collapseButton.setIcon(currentIcon);
        collapseButton.addMouseListener(new MouseAdapter() {

            @Override public void mousePressed(MouseEvent e) { plugin.switchTabExpandCollapse(tab.getName()); }
            @Override public void mouseEntered(MouseEvent e) { collapseButton.setIcon(hoverIcon); }
            @Override public void mouseExited(MouseEvent e) { collapseButton.setIcon(currentIcon); }
        });
        leftActions.add(collapseButton, BorderLayout.WEST);

        JLabel tabNameLabel = new JLabel(tab.getName());
        tabNameLabel.setForeground(Color.WHITE);
        tabNameLabel.setToolTipText(tab.getName());
        leftActions.add(tabNameLabel, BorderLayout.CENTER);

        headerPanel.add(leftActions, BorderLayout.WEST);


        JPanel rightActions = new JPanel();
        rightActions.setLayout(new BoxLayout(rightActions, BoxLayout.X_AXIS));
        rightActions.setOpaque(false);

        JLabel addClockButton = new JLabel(ADD_ICON);
        addClockButton.setToolTipText("Add clock(s) to this tab");
        addClockButton.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { showAddClocksDialog(); }
            @Override public void mouseEntered(MouseEvent e) { addClockButton.setIcon(ADD_HOVER_ICON); }
            @Override public void mouseExited(MouseEvent e) { addClockButton.setIcon(ADD_ICON); }
        });


        JLabel editTabButton = new JLabel(EDIT_ICON);
        editTabButton.setToolTipText("Edit tab name");
        editTabButton.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { plugin.editTab(tab); }
            @Override public void mouseEntered(MouseEvent e) { editTabButton.setIcon(EDIT_HOVER_ICON); }
            @Override public void mouseExited(MouseEvent e) { editTabButton.setIcon(EDIT_ICON); }
        });


        JLabel deleteTabButton = new JLabel(DELETE_TAB_ICON);
        deleteTabButton.setToolTipText("Delete this tab");
        deleteTabButton.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { if (deleteConfirm()) plugin.removeTab(tab); }
            @Override public void mouseEntered(MouseEvent e) { deleteTabButton.setIcon(DELETE_TAB_HOVER_ICON); }
            @Override public void mouseExited(MouseEvent e) { deleteTabButton.setIcon(DELETE_TAB_ICON); }
        });



        rightActions.add(addClockButton);
        rightActions.add(Box.createHorizontalStrut(5));
        rightActions.add(editTabButton);
        rightActions.add(Box.createHorizontalStrut(5));
        rightActions.add(deleteTabButton);


        headerPanel.add(rightActions, BorderLayout.EAST);


        add(headerPanel, BorderLayout.NORTH);



        itemsPanel = new JPanel(new GridBagLayout());
        itemsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        itemsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        itemsPanel.setOpaque(true);


        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 1; constraints.weightx = 1.0; constraints.weighty = 0.0;
        constraints.gridx = 0; constraints.gridy = 0; constraints.anchor = GridBagConstraints.NORTH;


        if (!isCollapsed) {
            int itemIndex = 0;
            List<UUID> clockIds = tab.getClocks() != null ? tab.getClocks() : new ArrayList<>();

            for (UUID clockId : clockIds) {
                Optional<TZClocksItem> clockOpt = plugin.getTimezones().stream()
                        .filter(c -> c.getUuid().equals(clockId))
                        .findFirst();

                if (clockOpt.isPresent()) {
                    TZClocksItem clock = clockOpt.get();

                    TZClocksTabItemPanel itemPanel = new TZClocksTabItemPanel(plugin, clock);
                    tabItemPanelsMap.put(clock, itemPanel);


                    JPanel wrapper = new JPanel(new BorderLayout());
                    wrapper.setOpaque(false);
                    if (itemIndex > 0) { wrapper.setBorder(new EmptyBorder(5, 0, 0, 0)); }
                    else { wrapper.setBorder(new EmptyBorder(0,0,0,0)); }
                    wrapper.add(itemPanel, BorderLayout.NORTH);
                    itemsPanel.add(wrapper, constraints);

                    constraints.gridy++;
                    itemIndex++;
                }
            }

            GridBagConstraints tabGlueConstraints = new GridBagConstraints();
            tabGlueConstraints.gridx = 0; tabGlueConstraints.gridy = constraints.gridy;
            tabGlueConstraints.weighty = 1.0; tabGlueConstraints.fill = GridBagConstraints.VERTICAL;
            itemsPanel.add(Box.createVerticalGlue(), tabGlueConstraints);


            add(itemsPanel, BorderLayout.CENTER);
        }
    }

    /**
     * Shows the dialog for selecting clocks to add to this user tab.
     */
    private void showAddClocksDialog() {
        List<TZClocksItem> clocks = pluginPanel.getAvailableClocks();
        if (clocks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All available clocks are already in tabs.", "No Available Clocks", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        TZClocksSelectionPanel selectionPanel = new TZClocksSelectionPanel(this, pluginPanel, clocks);
        selectionPanel.setOnOk(e -> {
            List<TZClocksItem> selectedClocks = selectionPanel.getSelectedClocks();
            for (TZClocksItem clock : selectedClocks) {

                plugin.addClockToUserTab(clock, tab);
            }
        });
        selectionPanel.show();
    }

    /**
     * Shows confirmation dialog before deleting a user tab.
     */
    private boolean deleteConfirm() {
        int confirm = JOptionPane.showConfirmDialog(this, DELETE_MESSAGE, DELETE_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return confirm == JOptionPane.YES_OPTION;
    }

}