package com.tzclocks.tzui;

import com.tzclocks.TZClocksPlugin;
import com.tzclocks.tzdata.TZClocksItem;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


public class TZClocksSelectionPanel {
    private final JList<TZClocksItem> clockList;
    private ActionListener okEvent;
    private final JDialog dialog;
    private final TZClocksPluginPanel pluginPanel;

    private static final String OK = "Ok";
    private static final String CANCEL = "Cancel";
    private static final String TITLE = "Select Clocks";
    private static final String MESSAGE = "Select clocks to add to this tab";
    private static final String SUBMESSAGE = "Ctrl+Click to select multiple clocks";

    public TZClocksSelectionPanel(Component parent, TZClocksPluginPanel pluginPanel, List<TZClocksItem> clocks) {
        this.pluginPanel = pluginPanel;
        this.clockList = new JList<>(clocks.toArray(new TZClocksItem[0]));
        this.clockList.setBackground(ColorScheme.DARKER_GRAY_COLOR);


        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel message = new JLabel(MESSAGE);
        message.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel subMessage = new JLabel(SUBMESSAGE);
        subMessage.setHorizontalAlignment(SwingConstants.CENTER);

        topPanel.add(message, BorderLayout.NORTH);
        topPanel.add(subMessage, BorderLayout.CENTER);


        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setPreferredSize(new Dimension(250, 300));


        clockList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TZClocksItem) {
                    TZClocksItem clock = (TZClocksItem) value;
                    setText(clock.getCustomName() != null ? clock.getCustomName() : clock.getName());
                }
                return component;
            }
        });

        JScrollPane scrollPane = new JScrollPane(clockList);
        scrollPane.setBorder(new EmptyBorder(0, 5, 0, 5));

        centerPanel.add(topPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JOptionPane optionPane = new JOptionPane(centerPanel);

        JButton okButton = new JButton(OK);
        okButton.addActionListener(this::onOkButtonClick);

        JButton cancelButton = new JButton(CANCEL);
        cancelButton.addActionListener(this::onCancelButtonClick);

        optionPane.setOptions(new Object[]{okButton, cancelButton});

        dialog = optionPane.createDialog(parent, TITLE);
        dialog.setTitle(TITLE);
    }

    public List<TZClocksItem> getSelectedClocks() {
        return clockList.getSelectedValuesList();
    }

    public void setOnOk(ActionListener event) {
        this.okEvent = event;
    }

    private void onOkButtonClick(ActionEvent e) {
        if (okEvent != null) {
            okEvent.actionPerformed(e);
        }


        SwingUtilities.invokeLater(() -> pluginPanel.updatePanel());

        dialog.setVisible(false);
    }

    private void onCancelButtonClick(ActionEvent e) {
        dialog.setVisible(false);
    }

    public void show() {
        dialog.setVisible(true);
    }
}