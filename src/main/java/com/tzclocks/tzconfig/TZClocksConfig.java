package com.tzclocks.tzconfig;

import java.util.List;

import com.tzclocks.TZClocksPlugin;
import com.tzclocks.tzutilities.TZFormatEnum;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;


@ConfigGroup(TZClocksPlugin.CONFIG_GROUP)
public interface TZClocksConfig extends Config {
		//format dropdown
		@ConfigItem(
			position = 1,
			keyName = "tzFormat",
			name = "Time Format",
			description = "Select 12-hour or 24-hour format"
	)
	default TZFormatEnum getTZFormatMode() {
		return TZFormatEnum.TWELVE_HOUR;
	} //default format

	void setTimeFormat(TZFormatEnum tzFormat); //says no usages but is important
	void setTimezones(List<String> timezones); //also says no usages but also important
}