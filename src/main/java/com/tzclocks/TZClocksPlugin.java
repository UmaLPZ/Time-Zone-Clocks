package com.tzclocks;

import com.tzclocks.tzconfig.TZClocksConfig;
import com.tzclocks.tzdata.TZClocksDataManager;
import com.tzclocks.tzdata.TZClocksItem;
import com.tzclocks.tzdata.TZClocksTab;
import com.tzclocks.tzutilities.TZFormatEnum;
import com.tzclocks.tzui.TZClocksPluginPanel;
import com.tzclocks.tzui.TZClocksTabPanel;
import com.google.inject.Provides;
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
		name = "Time Zone clocks"
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
	private List<TZClocksTab> tabs = new ArrayList<>();


	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(); //updates the clocks as scheduled. Might be a better alternative

	@Override
	protected void startUp() throws Exception { //starts plugin when loading client
		panel = new TZClocksPluginPanel(this, config);
		final BufferedImage icon = ImageUtil.loadImageResource(TZClocksPlugin.class, PANEL_ICON_PATH);
		navButton = NavigationButton.builder()
				.tooltip("Timezones")
				.icon(icon)
				.priority(5)
				.panel(panel)
				.build();
		clientToolbar.addNavigation(navButton);
		dataManager.loadData();
		refreshTimezonePanels();
		//panel.activatePanel(); // Removed call to activatePanel

		scheduler.scheduleAtFixedRate(this::updateTimezoneData, 0, 1, TimeUnit.SECONDS);
	}

	@Override
	protected void shutDown() throws Exception { //shuts down plugin
		clientToolbar.removeNavigation(navButton); //removes from navigation bar, same as other plugins
		dataManager.saveData(); //saves data one last time before closing
		scheduler.shutdown(); //shuts down scheduler. might be better alternative
	}

	@Provides
	TZClocksConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(TZClocksConfig.class);
	} //supposedly saves to the RL config manager. Not actually sure but other plugins use the same thing. Need to actually spend time learning how it works

	public void addTimezoneToPanel(String timezoneId) { //adds clock to the panel and saves data to data manager
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

		panel.addTimezonePanel(newItem);
		dataManager.saveData();
	}

	public void removeTimezoneFromPanel(TZClocksItem item) { //removes clock from the panel and updates data in the data manager
		timezones.remove(item);
		removeClockFromTab(item); // Remove from its tab, if any
		dataManager.saveData();
		SwingUtilities.invokeLater(() -> panel.removeTimezonePanel(item));
	}

	public void refreshTimezonePanels() { //refreshes panel on start up
		SwingUtilities.invokeLater(() -> {
			panel.removeAllClocks();
			for (TZClocksItem item : timezones) {
				panel.addTimezonePanel(item);
			}
			// Add tab panels after adding clocks
			for (TZClocksTab tab : tabs) {
				panel.addTabPanel(tab);
			}
		});
	}

	public void updateTimezoneData() { //updates time based on scheduler
		DateTimeFormatter formatter = getFormatter();
		for (TZClocksItem item : timezones) {
			ZoneId zoneId = ZoneId.of(item.getName());
			ZonedDateTime now = ZonedDateTime.now(zoneId);
			String currentTime = now.format(formatter);
			item.setCurrentTime(currentTime);
		}
		SwingUtilities.invokeLater(() -> panel.refreshTimeDisplays());
	}
	public DateTimeFormatter getFormatter() { //formats based on selected option from config
		if (config.getTZFormatMode() == TZFormatEnum.TWENTY_FOUR_HOUR) {
			return DateTimeFormatter.ofPattern("HH:mm:ss");
		} else {
			return DateTimeFormatter.ofPattern("hh:mm:ss a");
		}
	}

	// Tab Management Methods

	public void addTab(String tabName) {
		tabs.add(new TZClocksTab(tabName));
		SwingUtilities.invokeLater(() -> panel.addTabPanel(tabs.get(tabs.size() - 1)));
		dataManager.saveData();
	}

	public void editTab(TZClocksTab tab) {
		String newName = JOptionPane.showInputDialog(panel, "Enter new name:", tab.getName());
		if (newName != null && !newName.trim().isEmpty()) {
			renameTab(tab, newName);
		}
	}

	public void renameTab(TZClocksTab tab, String newName) {
		tab.setName(newName);
		// Refresh the tab panel to reflect the name change
		SwingUtilities.invokeLater(() -> {
			TZClocksTabPanel tabPanel = panel.getTabPanelsMap().get(tab);
			if (tabPanel != null) {
				tabPanel.updateTabName(newName);
			}
		});
		dataManager.saveData();
	}


	public void removeTab(TZClocksTab tab) {
		tabs.remove(tab);
		SwingUtilities.invokeLater(() -> panel.removeTabPanel(tab));
		dataManager.saveData();
	}

	public void removeClockFromTab(TZClocksItem clock) {
		// Find the tab containing the clock
		for (TZClocksTab tab : tabs) {
			if (tab.getClocks().contains(clock.getUuid())) {
				tab.removeClock(clock.getUuid());

				// Refresh the tab panel to reflect the removal
				SwingUtilities.invokeLater(() -> {
					TZClocksTabPanel tabPanel = panel.getTabPanelsMap().get(tab);
					if (tabPanel != null) {
						tabPanel.toggleTabCollapse(); // This will refresh the clocks displayed in the tab
					}
				});

				break; // No need to continue searching
			}
		}
	}

	public void switchTabExpandCollapse(TZClocksTab tab) {
		tab.setCollapsed(!tab.isCollapsed());

		SwingUtilities.invokeLater(() -> {
			TZClocksTabPanel tabPanel = panel.getTabPanelsMap().get(tab);
			if (tabPanel != null) {
				tabPanel.toggleTabCollapse();

				// After collapsing/expanding, refresh the main panel
				panel.removeAllClocks(); // Remove all clocks from the main panel

				// Re-add clocks based on their tab state
				for (TZClocksItem item : timezones) {
					TZClocksTabPanel parentTab = getTabForClock(item);
					if (parentTab == null || !parentTab.getTab().isCollapsed()) {
						panel.addTimezonePanel(item);
					}
				}
			}
		});
		dataManager.saveData();
	}

	// Helper method to find the tab containing a clock
	private TZClocksTabPanel getTabForClock(TZClocksItem clock) {
		for (TZClocksTab tab : tabs) {
			if (tab.getClocks().contains(clock.getUuid())) {
				return panel.getTabPanelsMap().get(tab);
			}
		}
		return null;
	}

	public void setTabs(List<TZClocksTab> tabs) {
		this.tabs = tabs;
	}
}