package com.tzclocks;

import com.google.inject.Provides;
import com.tzclocks.tzconfig.TZClocksConfig;
import com.tzclocks.tzdata.TZClocksDataManager;
import com.tzclocks.tzdata.TZClocksItem;
import com.tzclocks.tzdata.TZClocksTab;
import com.tzclocks.tzui.TZClocksTabPanel;
import com.tzclocks.tzutilities.TZFormatEnum;
import com.tzclocks.tzui.TZClocksItemPanel;
import com.tzclocks.tzui.TZClocksPluginPanel;
import com.tzclocks.tzui.TZClocksTabItemPanel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.tzclocks.tzutilities.TZConstants.PANEL_ICON_PATH;

@Slf4j
@PluginDescriptor(
		name = "Time Zone Clocks"
)
public class TZClocksPlugin extends Plugin {
	public static final String CONFIG_GROUP = "tzconfig";

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	@Setter
	private TZClocksConfig config;

	@Inject
	public TZClocksDataManager dataManager;

	@Getter
	private TZClocksPluginPanel panel;

	private NavigationButton navButton;

	@Getter
	@Setter
	private List<TZClocksItem> timezones = new ArrayList<>();

	@Getter
	@Setter
	private List<TZClocksTab> tabs = new ArrayList<>();

	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	@Override
	protected void startUp() throws Exception {
		panel = injector.getInstance(TZClocksPluginPanel.class);
		final BufferedImage icon = ImageUtil.loadImageResource(TZClocksPlugin.class, PANEL_ICON_PATH);
		navButton = NavigationButton.builder()
				.tooltip("Timezones")
				.icon(icon)
				.priority(5)
				.panel(panel)
				.build();
		clientToolbar.addNavigation(navButton);
		dataManager.loadData();
		SwingUtilities.invokeLater(() -> panel.updatePanel());

		scheduler.scheduleAtFixedRate(this::updateTimezoneData, 0, 1, TimeUnit.SECONDS);
	}

	@Override
	protected void shutDown() {
		clientToolbar.removeNavigation(navButton);
		scheduler.shutdown();
	}

	@Provides
	TZClocksConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(TZClocksConfig.class);
	}

	public void addTimezoneToPanel(String timezoneId) {
		String customName = null;

		if (timezoneId.equals(ZoneId.systemDefault().toString())) {
			customName = "Local Time";
		} else if (timezoneId.equals("Europe/London")) {
			customName = "Jagex Time";
		}

		ZoneId zoneId = ZoneId.of(timezoneId);
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		DateTimeFormatter formatter = getFormatter();
		String currentTime = now.format(formatter);
		TZClocksItem newItem = new TZClocksItem(UUID.randomUUID(), timezoneId, currentTime, customName);
		timezones.add(newItem);

		dataManager.saveData();
		SwingUtilities.invokeLater(() -> panel.updatePanel());
	}

	public void removeTimezoneFromPanel(TZClocksItem item) {
		timezones.remove(item);
		removeClockFromTab(item);
		dataManager.saveData();
		SwingUtilities.invokeLater(() -> panel.updatePanel());
	}

	public void addTab(String tabName) {
		tabs.add(new TZClocksTab(tabName, new ArrayList<>()));
		SwingUtilities.invokeLater(() -> panel.addTabPanel(tabs.get(tabs.size() - 1)));
		dataManager.saveData();
	}

	public void editTab(TZClocksTab tab) {
		String name = JOptionPane.showInputDialog(panel, "Enter the name of this tab (30 chars max).", "Edit Tab", JOptionPane.PLAIN_MESSAGE);

		if (name == null || name.isEmpty()) {
			return;
		}

		if (name.length() > 30) {
			name = name.substring(0, 30);
		}

		String tabName = name;
		if (tabs.stream().noneMatch(t -> t.getName().equals(tabName))) {
			tab.setName(tabName);
			dataManager.saveData();
			SwingUtilities.invokeLater(() -> panel.updatePanel());
		}
	}

	public void removeTab(TZClocksTab tab) {
		for (UUID clockId : tab.getClocks()) {
			TZClocksItem clock = timezones.stream()
					.filter(item -> item.getUuid().equals(clockId))
					.findFirst()
					.orElse(null);
			if (clock != null) {
				panel.addTimezonePanel(clock);
			}
		}

		tabs.remove(tab);
		dataManager.saveData();
		SwingUtilities.invokeLater(() -> panel.updatePanel());
	}

	public void switchTabExpandCollapse(TZClocksTab tab) {
		tab.setCollapsed(!tab.isCollapsed());
		dataManager.saveData();

		// Update the collapse/expand icon of the tab panel
		SwingUtilities.invokeLater(() -> {
			TZClocksTabPanel tabPanel = panel.getTabPanelsMap().get(tab); // Get the tab panel from the map
			if (tabPanel != null) {
				tabPanel.updateCollapseIcon(); // Update the icon
			}
		});

		SwingUtilities.invokeLater(() -> panel.updatePanel()); // Refresh the entire panel
	}

	public void removeClockFromTab(TZClocksItem clock) {
		for (TZClocksTab tab : tabs) {
			if (tab.getClocks().contains(clock.getUuid())) {
				tab.removeClock(clock.getUuid());

				SwingUtilities.invokeLater(() -> {
					// Refresh the panel to reflect the removal
					TZClocksTabPanel tabPanel = panel.getTabPanelsMap().get(tab);
					if (tabPanel != null) {
						tabPanel.toggleTabCollapse(); // Only call toggleTabCollapse once
					}
				});
				break;
			}
		}
	}

	public void updateTimezoneData() {
		DateTimeFormatter formatter = getFormatter();
		for (TZClocksItem item : timezones) {
			ZoneId zoneId = ZoneId.of(item.getName());
			ZonedDateTime now = ZonedDateTime.now(zoneId);
			String currentTime = now.format(formatter);
			item.setCurrentTime(currentTime);

			SwingUtilities.invokeLater(() -> {
				TZClocksItemPanel clockPanel = panel.getTimezonePanelsMap().get(item);
				if (clockPanel != null) {
					clockPanel.updateTime();
				}

				for (TZClocksTab tab : tabs) {
					if (tab.getClocks().contains(item.getUuid()) && !tab.isCollapsed()) {
						TZClocksTabPanel tabPanel = panel.getTabPanelsMap().get(tab);
						if (tabPanel != null) {
							TZClocksTabItemPanel tabItemPanel = tabPanel.getTabItemPanelsMap().get(item);
							if (tabItemPanel != null) {
								tabItemPanel.updateTime();
							}
						}
						break;
					}
				}
			});
		}
	}

	public DateTimeFormatter getFormatter() {
		if (config.getTZFormatMode() == TZFormatEnum.TWENTY_FOUR_HOUR) {
			return DateTimeFormatter.ofPattern("HH:mm:ss");
		} else {
			return DateTimeFormatter.ofPattern("hh:mm:ss a");
		}
	}

	public void addClockToTab(TZClocksItem clock, TZClocksTab tab) {
		TZClocksTabPanel tabPanel = panel.getTabPanelsMap().get(tab);
		if (tabPanel != null) {
			TZClocksTabItemPanel itemPanel = new TZClocksTabItemPanel(this, clock);
			tabPanel.getTabItemPanelsMap().put(clock, itemPanel); // Add to the map

			GridBagConstraints constraints = tabPanel.getConstraints(); // Get constraints from the tab panel
			if (tabPanel.getIndex().getAndIncrement() > 0) {
				tabPanel.getItemsPanel().add(tabPanel.createMarginWrapper(itemPanel), constraints);
			} else {
				tabPanel.getItemsPanel().add(itemPanel, constraints);
			}
			constraints.gridy++;
			tabPanel.getItemsPanel().revalidate();
			tabPanel.getItemsPanel().repaint();
		}
	}
}