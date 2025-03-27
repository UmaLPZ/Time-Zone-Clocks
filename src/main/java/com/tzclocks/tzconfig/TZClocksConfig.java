package com.tzclocks.tzconfig;

// Keep List import if other list-based configs are added later
// import java.util.List;

import com.tzclocks.TZClocksPlugin;
import com.tzclocks.tzutilities.TZFormatEnum;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;


@ConfigGroup(TZClocksPlugin.CONFIG_GROUP) // Ensure this matches the plugin's CONFIG_GROUP constant
public interface TZClocksConfig extends Config {

	@ConfigItem(
			position = 1,
			keyName = "tzFormat",
			name = "Time Format",
			description = "Select 12-hour or 24-hour format"
	)
	default TZFormatEnum getTZFormatMode() {
		return TZFormatEnum.TWELVE_HOUR;
	} //default format

	@ConfigItem(
			position = 2, // Position this below the format option
			keyName = "showFixedGameTimesTab",
			name = "Show Game Times Tab",
			description = "Shows/hides the fixed 'Game Times' tab (UTC, Server Time) at the bottom."
	)
	default boolean showFixedGameTimesTab() {
		return true; // Default to showing the tab
	}

}