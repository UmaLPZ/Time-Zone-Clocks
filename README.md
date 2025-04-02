# Time Zone Clocks
This plugin allows you to add clocks for different time zones to a sidebar on RuneLite. Hopefully this is helpful for anyone that has friends in different time zones or part of a clan with members from all around the world.

This plugin's code is heavily based off the code for the [Market Watcher](https://github.com/BobTabrizi/market-watcher) plugin and some code from the default Timers plugin on RuneLite. I have very little experienced with Java so please feel free to let me know of any mistakes I made and how I can improve the code.

# Current Features
* Clock Tabs to group clocks together
* Toggle calendar for each clock
* Add/edit custom name for each clock
* Fixed tab with Local + Jagex clocks
  * Can be toggled on/off in the plugin config
  * Fixed Tab is located at the bottom of the panel
    * **Collapsed state does not save between sessions!**
* Select from by region/abbreviation or region/city
  * **Initial version only had region/city. Region/Abbrev is now default**
  * List can be selected in the config

# Planned Changes
* Add option to show difference from local time
* Add ability to reposition clocks (also similar to Market Watcher)
* ~~Add option to include the date (for people with friends tomorrow/yesterday)~~ Added in Beta v1.3
* ~~Add categories for organizing clocks (similar to Market Watcher)~~ Added in Beta v1.2
* ~~Ability to rename clocks~~ Added in Beta v1.1

# Known Bugs
* There is currently an issue where turning off the plugin will stop the clocks from updating the time. You will need to restart the client to fix this. I will be looking into an alternative for how the clocks update to fix this.

# Releases

* v1.5
  * Major Update
    * Refracted a bunch of code
    * Added custom names
    * Added Clock Tabs
    * Added Calendar Toggle
    * Added Fixed Tab w/ Local + Jagex Clocks
    * Added New List of Time Zone Names with Abbreviations
    * Changed the look of clock panels

* v1.0
  * Initial Release