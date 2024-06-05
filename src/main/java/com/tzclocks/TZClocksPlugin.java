package com.tzclocks;

import com.tzclocks.tzconfig.TZClocksConfig;
import com.tzclocks.tzdata.TZClocksCategory;
import com.tzclocks.tzdata.TZClocksDataManager;
import com.tzclocks.tzdata.TZClocksItem;
import com.tzclocks.tzdata.TZFormatEnum;
import com.tzclocks.tzui.TZClocksPluginPanel;
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
import java.util.Optional;
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
	public TZClocksDataManager dataManager; // Make dataManager public

	private TZClocksPluginPanel panel;
	private NavigationButton navButton;

	@Getter
	@Setter
	private List<TZClocksItem> timezones = new ArrayList<>();

	@Getter
	private List<TZClocksCategory> categories = new ArrayList<>();


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

		// Add default "Uncategorized" category
		categories.add(new TZClocksCategory("Uncategorized"));

		refreshTimezonePanels();
		panel.activatePanel();

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

		// Add the clock to the "Uncategorized" category
		categories.get(0).addClock(newItem.getUuid());

		panel.addTimezonePanel(newItem);
		dataManager.saveData();
	}

	public void removeTimezoneFromPanel(TZClocksItem item) { //removes clock from the panel and updates data in the data manager
		timezones.remove(item);

		// Remove clock from its category
		removeFromCategory(item.getUuid());

		dataManager.saveData();
		SwingUtilities.invokeLater(() -> panel.removeTimezonePanel(item));
	}

	public void refreshTimezonePanels() { //refreshes panel on start up
		SwingUtilities.invokeLater(() -> {
			panel.removeAllClocks();
			for (TZClocksCategory category : categories) {
				panel.addCategoryPanel(category);
				if (!category.isCollapsed()) {
					for (UUID clockId : category.getClocks()) {
						Optional<TZClocksItem> clockItem = timezones.stream()
								.filter(item -> item.getUuid().equals(clockId))
								.findFirst();
						clockItem.ifPresent(panel::addTimezonePanel);
					}
				}
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

	// Category Management Methods

	public void addCategory(String categoryName) {
		categories.add(new TZClocksCategory(categoryName));
		SwingUtilities.invokeLater(() -> panel.addCategoryPanel(categories.get(categories.size() - 1)));
		dataManager.saveData();
	}

	public void renameCategory(TZClocksCategory category, String newName) {
		category.setName(newName);
		SwingUtilities.invokeLater(() -> panel.renameCategoryPanel(category));
		dataManager.saveData();
	}

	public void deleteCategory(TZClocksCategory category, boolean deleteClocks) {
		if (deleteClocks) {
			// Remove clocks from the plugin's list
			category.getClocks().forEach(clockId -> timezones.removeIf(item -> item.getUuid().equals(clockId)));
		} else {
			// Move clocks to "Uncategorized" category
			TZClocksCategory uncategorized = categories.get(0);
			category.getClocks().forEach(uncategorized::addClock);
		}

		categories.remove(category);
		SwingUtilities.invokeLater(() -> panel.removeCategoryPanel(category));
		dataManager.saveData();
	}

	// Helper method to remove a clock from its current category
	private void removeFromCategory(UUID clockId) {
		for (TZClocksCategory category : categories) {
			if (category.getClocks().contains(clockId)) {
				category.removeClock(clockId);
				break;
			}
		}
	}
}