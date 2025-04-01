package com.tzclocks.tzconfig;

import com.tzclocks.TZClocksPlugin;
import com.tzclocks.tzutilities.TZFormatEnum;
import com.tzclocks.tzutilities.TZSourceMode;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(TZClocksPlugin.CONFIG_GROUP)
public interface TZClocksConfig extends Config {


	@ConfigSection(
			name = "Display",
			description = "Clock display settings",
			position = 0
	)
	String displaySection = "display";

	@ConfigItem(
			position = 1,
			keyName = "tzFormat",
			name = "Time Format",
			description = "Select 12-hour or 24-hour format",
			section = displaySection
	)
	default TZFormatEnum getTZFormatMode() {
		return TZFormatEnum.TWELVE_HOUR;
	}

	@ConfigItem(
			position = 2,
			keyName = "showFixedGameTimesTab",
			name = "Show Local/Jagex Tab",
			description = "Shows/hides the fixed 'Local+Jagex' tab (UTC, Server Time) at the bottom.",
			section = displaySection
	)
	default boolean showFixedGameTimesTab() {
		return true;
	}

	@ConfigSection(
			name = "Timezone Source",
			description = "How timezones are selected in the panel",
			position = 3
	)
	String sourceSection = "source";


	@ConfigItem(
			position = 4,
			keyName = "timezoneSourceMode",
			name = "Selection Mode",
			description = "Choose how to select timezones: by region/city or by region/abbreviation.",
			section = sourceSection
	)
	default TZSourceMode getTimezoneSourceMode() {
		return TZSourceMode.ABBREVIATION;
	}
}