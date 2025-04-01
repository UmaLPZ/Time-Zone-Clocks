package com.tzclocks;

import com.google.inject.Provides;
import com.tzclocks.tzconfig.TZClocksConfig;
import com.tzclocks.tzdata.TZClocksDataManager;
import com.tzclocks.tzdata.TZClocksItem;
import com.tzclocks.tzdata.TZClocksTab;
import com.tzclocks.tzui.TZClocksItemPanel;
import com.tzclocks.tzui.TZClocksPluginPanel;
import com.tzclocks.tzui.TZClocksTabItemPanel;
import com.tzclocks.tzui.TZClocksTabPanel;
import com.tzclocks.tzutilities.TZFormatEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.tzclocks.tzutilities.TZConstants.PANEL_ICON_PATH;
import static java.time.ZoneId.systemDefault;

@Slf4j
@PluginDescriptor(
		name = "Time Zone Clocks"
)
public class TZClocksPlugin extends Plugin {
	public static final String CONFIG_GROUP = "tzconfig";


	public static final UUID LOCAL_CLOCK_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
	public static final String LOCAL_ZONE_ID = String.valueOf(systemDefault());
	public static final String LOCAL_DISPLAY_NAME = "Mine";
	public static final UUID JAGEX_CLOCK_UUID = UUID.fromString("00000000-0000-0000-0000-000000000002");
	public static final String JAGEX_ZONE_ID = "Europe/London";
	public static final String JAGEX_DISPLAY_NAME = "Jagex Time";
	public static final String FIXED_TAB_NAME = "Game Times";

	@Inject private Client client;
	@Inject private ClientThread clientThread;
	@Inject private ClientToolbar clientToolbar;
	@Inject @Getter private TZClocksConfig config;
	@Inject public TZClocksDataManager dataManager;
	@Getter private TZClocksPluginPanel panel;
	private NavigationButton navButton;

	@Getter @Setter private List<TZClocksItem> timezones = new ArrayList<>();
	@Getter @Setter private List<TZClocksTab> tabs = new ArrayList<>();

	@Getter @Setter
	private Map<TZClocksItem, TZClocksTabItemPanel> fixedSouthTabClocksMap = new HashMap<>();

	@Getter private boolean fixedTabCollapsed = false;

	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private boolean isActive;

	@Override
	protected void startUp() throws Exception {
		log.info("Starting Time Zone Clocks");
		dataManager.loadData();


		updateOrAddFixedClock(LOCAL_CLOCK_UUID, LOCAL_ZONE_ID, LOCAL_DISPLAY_NAME);
		updateOrAddFixedClock(JAGEX_CLOCK_UUID, JAGEX_ZONE_ID, JAGEX_DISPLAY_NAME);

		panel = injector.getInstance(TZClocksPluginPanel.class);
		SwingUtilities.invokeLater(() -> panel.initializeSouthPanel());

		final BufferedImage icon = ImageUtil.loadImageResource(TZClocksPlugin.class, PANEL_ICON_PATH);
		navButton = NavigationButton.builder()
				.tooltip("Timezones")
				.icon(icon)
				.priority(5)
				.panel(panel)
				.build();
		clientToolbar.addNavigation(navButton);


		SwingUtilities.invokeLater(() -> panel.updateDropdowns());

		SwingUtilities.invokeLater(() -> panel.updatePanel());

		scheduler.scheduleAtFixedRate(this::updateTimezoneData, 0, 1, TimeUnit.SECONDS);
		isActive = true;
	}


	private void updateOrAddFixedClock(UUID uuid, String zoneId, String displayName) {
		TZClocksItem existing = timezones.stream().filter(item -> item.getUuid().equals(uuid)).findFirst().orElse(null);
		if (existing == null) {
			timezones.add(new TZClocksItem(uuid, zoneId, "", displayName, null, displayName));
		} else {

			existing.setName(zoneId);
			existing.setDisplayName(displayName);

			if (existing.getCustomName() == null || existing.getCustomName().trim().isEmpty()) {
				existing.setCustomName(displayName);
			}
		}
	}


	@Override
	protected void shutDown() {
		log.info("Stopping Time Zone Clocks");
		if (dataManager != null) { dataManager.saveData(); }
		if (clientToolbar != null && navButton != null) { clientToolbar.removeNavigation(navButton); }
		if (scheduler != null && !scheduler.isShutdown()) { scheduler.shutdown(); }
		isActive = false; panel = null; fixedSouthTabClocksMap.clear(); timezones.clear(); tabs.clear();
	}

	@Provides
	TZClocksConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(TZClocksConfig.class);
	}

	/**
	 * Handles configuration changes.
	 */
	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals(CONFIG_GROUP)) { return; }


		if (event.getKey().equals("showFixedGameTimesTab")) {
			log.debug("Config changed for showFixedGameTimesTab, rebuilding south panel.");
			if (panel != null) {
				SwingUtilities.invokeLater(() -> {
					panel.initializeSouthPanel();
					panel.revalidate(); panel.repaint();
				});
			}
		}


		if (event.getKey().equals("timezoneSourceMode")) {
			log.debug("Config changed for timezoneSourceMode, updating dropdowns.");
			if (panel != null) {

				SwingUtilities.invokeLater(() -> panel.updateDropdowns());
			}
		}


		if (event.getKey().equals("tzFormat")) {


			updateTimezoneData();
		}
	}





	public void addTimezoneToPanel(String zoneIdString, String displayName) {
		if (zoneIdString.equals(LOCAL_ZONE_ID) || zoneIdString.equals(JAGEX_ZONE_ID)) {
			log.warn("Attempted to add fixed timezone {} via addTimezoneToPanel.", zoneIdString);
			SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(panel, zoneIdString + " is already shown in the Game Times panel.", "Clock Exists", JOptionPane.INFORMATION_MESSAGE) ); return;
		}
		boolean alreadyExists = timezones.stream().anyMatch(item -> item.getName().equalsIgnoreCase(zoneIdString) && !item.getUuid().equals(LOCAL_CLOCK_UUID) && !item.getUuid().equals(JAGEX_CLOCK_UUID));
		if (alreadyExists) { SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(panel, displayName + " (" + zoneIdString + ") has already been added.", "Clock Exists", JOptionPane.INFORMATION_MESSAGE) ); return; }

		clientThread.invokeLater(() -> {
			String customName = null; String showCalendar = null;
			try {
				ZoneId zoneId = ZoneId.of(zoneIdString); ZonedDateTime now = ZonedDateTime.now(zoneId);
				DateTimeFormatter formatter = getFormatter(); String currentTime = now.format(formatter);
				TZClocksItem newItem = new TZClocksItem(UUID.randomUUID(), zoneIdString, currentTime, customName, showCalendar, displayName);
				timezones.add(newItem); dataManager.saveData(); SwingUtilities.invokeLater(() -> panel.updatePanel());
			} catch (Exception e) { log.error("Failed to add timezone: {}", zoneIdString, e); SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(panel, "Failed to add timezone: " + zoneIdString, "Error", JOptionPane.ERROR_MESSAGE) ); }
		});
	}

	public void removeTimezoneFromPanel(TZClocksItem item) {
		if (item.getUuid().equals(LOCAL_CLOCK_UUID) || item.getUuid().equals(JAGEX_CLOCK_UUID)) { log.warn("Attempted to remove fixed timezone item {} via user action.", item.getName()); return; }
		timezones.remove(item); removeClockFromUserTab(item); dataManager.saveData(); SwingUtilities.invokeLater(() -> panel.updatePanel());
	}

	public void editClockCustomName(TZClocksItem clock) {
		String currentEditName = clock.getCustomName() != null ? clock.getCustomName() : clock.getDisplayName();
		String newName = JOptionPane.showInputDialog(panel, "Enter a custom name for the clock (blank to reset):", currentEditName);
		if (newName != null) {
			if (newName.trim().isEmpty()) { clock.setCustomName(null); }
			else { clock.setCustomName(newName.trim()); }

			if (!clock.getUuid().equals(LOCAL_CLOCK_UUID) && !clock.getUuid().equals(JAGEX_CLOCK_UUID)) { dataManager.saveData(); }

			SwingUtilities.invokeLater(() -> {
				panel.updatePanel();
				if (fixedSouthTabClocksMap != null && (clock.getUuid().equals(LOCAL_CLOCK_UUID) || clock.getUuid().equals(JAGEX_CLOCK_UUID))) { panel.initializeSouthPanel(); }
			});
		}
	}

	public void toggleMonthDayVisibility(TZClocksItem item) {
		if (item.getShowCalendar() == null) { item.setShowCalendar("active"); } else { item.setShowCalendar(null); }
		if (!item.getUuid().equals(LOCAL_CLOCK_UUID) && !item.getUuid().equals(JAGEX_CLOCK_UUID)) { dataManager.saveData(); }
		SwingUtilities.invokeLater(() -> {
			panel.updatePanel();
			if (fixedSouthTabClocksMap != null && (item.getUuid().equals(LOCAL_CLOCK_UUID) || item.getUuid().equals(JAGEX_CLOCK_UUID))) { panel.initializeSouthPanel(); }
		});
	}

	public void addTab(String tabName) {
		if (tabName.equalsIgnoreCase(FIXED_TAB_NAME)) { log.warn("Attempted to add user tab with reserved name '{}'.", FIXED_TAB_NAME); SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(panel, "Cannot use the reserved tab name '"+FIXED_TAB_NAME+"'.", "Reserved Name", JOptionPane.WARNING_MESSAGE) ); return; }
		boolean nameExists = tabs.stream().anyMatch(tab -> tab.getName().equalsIgnoreCase(tabName));
		if (nameExists) { SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(panel, "A tab with the name '" + tabName + "' already exists.", "Name Exists", JOptionPane.WARNING_MESSAGE) ); return; }
		clientThread.invokeLater(() -> { tabs.add(new TZClocksTab(tabName, new ArrayList<>())); dataManager.saveData(); SwingUtilities.invokeLater(() -> panel.updatePanel()); });
	}

	public void editTab(TZClocksTab tab) {
		String newName = JOptionPane.showInputDialog(panel, "Enter the name for this tab (30 chars max).", tab.getName());
		if (newName == null || newName.trim().isEmpty()) { return; }
		newName = newName.trim(); if (newName.length() > 30) { newName = newName.substring(0, 30); }
		if (newName.equalsIgnoreCase(FIXED_TAB_NAME)) { SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(panel, "Cannot use the reserved tab name '"+FIXED_TAB_NAME+"'.", "Reserved Name", JOptionPane.WARNING_MESSAGE) ); return; }
		String finalNewName = newName; boolean nameExists = tabs.stream().filter(t -> t != tab).anyMatch(t -> t.getName().equalsIgnoreCase(finalNewName));
		if (nameExists) { SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(panel, "Another tab with the name '" + finalNewName + "' already exists.", "Name Exists", JOptionPane.WARNING_MESSAGE) ); return; }
		if (!tab.getName().equals(newName)) { tab.setName(newName); dataManager.saveData(); SwingUtilities.invokeLater(() -> panel.updatePanel()); }
	}

	public void removeTab(TZClocksTab tab) { tabs.remove(tab); dataManager.saveData(); SwingUtilities.invokeLater(() -> panel.updatePanel()); }

	public void switchTabExpandCollapse(String tabName) {
		if (tabName.equalsIgnoreCase(FIXED_TAB_NAME)) { fixedTabCollapsed = !fixedTabCollapsed; if (panel != null) { SwingUtilities.invokeLater(() -> panel.initializeSouthPanel()); } }
		else { tabs.stream().filter(t -> t.getName().equalsIgnoreCase(tabName)).findFirst().ifPresent(userTab -> { userTab.setCollapsed(!userTab.isCollapsed()); dataManager.saveData(); if (panel != null) { SwingUtilities.invokeLater(() -> panel.updatePanel()); } }); }
	}

	public void removeClockFromUserTab(TZClocksItem clock) { for (TZClocksTab tab : tabs) {
		if (tab.getClocks() != null && tab.getClocks().contains(clock.getUuid())) {
			tab.removeClock(clock.getUuid()); dataManager.saveData(); if (panel != null) { SwingUtilities.invokeLater(() -> panel.updatePanel()); } break; }
		}
	}

	public void addClockToUserTab(TZClocksItem clock, TZClocksTab tab) {
		if (tabs.contains(tab) && !clock.getUuid().equals(LOCAL_CLOCK_UUID) && !clock.getUuid().equals(JAGEX_CLOCK_UUID)) {
			removeClockFromUserTab(clock);
			if (tab.getClocks() != null) { tab.addClock(clock.getUuid()); } else { log.error("Target user tab '{}' has null clock list!", tab.getName()); }
			dataManager.saveData(); if (panel != null) { SwingUtilities.invokeLater(() -> panel.updatePanel()); }
		} else { log.warn("Attempted invalid addClockToUserTab operation. Clock: {}, Tab: {}", clock.getName(), tab.getName()); }
	}


	public void updateTimezoneData() {
		DateTimeFormatter formatter = getFormatter();
		boolean showFixedTab = config.showFixedGameTimesTab();

		for (TZClocksItem item : timezones) {
			try {
				ZoneId zoneId = ZoneId.of(item.getName());
				ZonedDateTime now = ZonedDateTime.now(zoneId); String currentTime = now.format(formatter); item.setCurrentTime(currentTime);
				SwingUtilities.invokeLater(() -> {
					boolean updated = false;
					if (showFixedTab && !fixedTabCollapsed && (item.getUuid().equals(LOCAL_CLOCK_UUID) || item.getUuid().equals(JAGEX_CLOCK_UUID))) {
						TZClocksTabItemPanel southItemPanel = fixedSouthTabClocksMap.get(item);
						if (southItemPanel != null) { southItemPanel.updateTime(); updated = true; }
						else { log.trace("South panel item not found in map for fixed clock {}", item.getName()); }
					}
					if (!updated && panel != null) {
						TZClocksItemPanel clockPanel = panel.getTimezonePanelsMap().get(item);
						if (clockPanel != null) { clockPanel.updateTime(); updated = true; }
					}
					if (!updated && panel != null) {
						for (TZClocksTab userTab : tabs) {
							if (!userTab.isCollapsed() && userTab.getClocks() != null && userTab.getClocks().contains(item.getUuid())) {
								TZClocksTabPanel userTabPanel = panel.getTabPanelsMap().get(userTab);
								if (userTabPanel != null) {
									TZClocksTabItemPanel userTabItemPanel = userTabPanel.getTabItemPanelsMap().get(item);
									if (userTabItemPanel != null) { userTabItemPanel.updateTime(); updated = true; }
									else { log.trace("User tab item panel not found in map for clock {} in tab {}", item.getName(), userTab.getName()); }
								} else { log.trace("User tab panel not found in map for tab {}", userTab.getName()); } break;
							}
						}
					}
				});
			} catch (Exception e) { log.error("Failed to update time for timezone item: {} ({})", item.getName(), item.getUuid(), e); }
		}
	}

	public DateTimeFormatter getFormatter() {
		if (config != null && config.getTZFormatMode() == TZFormatEnum.TWENTY_FOUR_HOUR) {
			return DateTimeFormatter.ofPattern("HH:mm:ss");
		} else { return DateTimeFormatter.ofPattern("hh:mm:ss a"); }
	}
}