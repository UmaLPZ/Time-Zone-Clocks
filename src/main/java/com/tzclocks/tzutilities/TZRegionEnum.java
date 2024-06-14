package com.tzclocks.tzutilities;

import java.time.ZoneId;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TZRegionEnum { //List of regions and their zoneIDs. Filtered down to only show unique UTC sections
    ALL("All Regions"),
    AFRICA("Africa",
            ZoneId.of("Africa/Abidjan"), ZoneId.of("Africa/Accra"), ZoneId.of("Africa/Algiers"),
            ZoneId.of("Africa/Bissau"), ZoneId.of("Africa/Cairo"), ZoneId.of("Africa/Casablanca"),
            ZoneId.of("Africa/El_Aaiun"), ZoneId.of("Africa/Johannesburg"), ZoneId.of("Africa/Juba"),
            ZoneId.of("Africa/Khartoum"), ZoneId.of("Africa/Lagos"), ZoneId.of("Africa/Maputo"),
            ZoneId.of("Africa/Monrovia"), ZoneId.of("Africa/Nairobi"), ZoneId.of("Africa/Ndjamena"),
            ZoneId.of("Africa/Sao_Tome"), ZoneId.of("Africa/Tripoli"), ZoneId.of("Africa/Tunis"),
            ZoneId.of("Africa/Windhoek")),
    AMERICA("America",
            ZoneId.of("America/Adak"), ZoneId.of("America/Anchorage"), ZoneId.of("America/Araguaina"),
            ZoneId.of("America/Argentina/Buenos_Aires"), ZoneId.of("America/Bogota"), ZoneId.of("America/Caracas"),
            ZoneId.of("America/Chicago"), ZoneId.of("America/Denver"), ZoneId.of("America/Godthab"),
            ZoneId.of("America/Los_Angeles"), ZoneId.of("America/Mexico_City"), ZoneId.of("America/New_York"),
            ZoneId.of("America/Noronha"), ZoneId.of("America/Phoenix"), ZoneId.of("America/Santiago"),
            ZoneId.of("America/Sao_Paulo"), ZoneId.of("America/St_Johns"), ZoneId.of("America/Tijuana")),

    ANTARCTICA("Antarctica",
            ZoneId.of("Antarctica/Casey"), ZoneId.of("Antarctica/Davis"), ZoneId.of("Antarctica/DumontDUrville"),
            ZoneId.of("Antarctica/Macquarie"), ZoneId.of("Antarctica/Mawson"), ZoneId.of("Antarctica/McMurdo"),
            ZoneId.of("Antarctica/Palmer"), ZoneId.of("Antarctica/Rothera"), ZoneId.of("Antarctica/Syowa"),
            ZoneId.of("Antarctica/Troll"), ZoneId.of("Antarctica/Vostok")),
    ASIA("Asia",
            ZoneId.of("Asia/Aden"), ZoneId.of("Asia/Almaty"), ZoneId.of("Asia/Amman"), ZoneId.of("Asia/Anadyr"),
            ZoneId.of("Asia/Aqtau"), ZoneId.of("Asia/Aqtobe"), ZoneId.of("Asia/Ashgabat"), ZoneId.of("Asia/Atyrau"),
            ZoneId.of("Asia/Baghdad"), ZoneId.of("Asia/Bahrain"), ZoneId.of("Asia/Baku"), ZoneId.of("Asia/Bangkok"),
            ZoneId.of("Asia/Barnaul"), ZoneId.of("Asia/Beirut"), ZoneId.of("Asia/Bishkek"), ZoneId.of("Asia/Brunei"),
            ZoneId.of("Asia/Chita"), ZoneId.of("Asia/Choibalsan"), ZoneId.of("Asia/Colombo"), ZoneId.of("Asia/Damascus"),
            ZoneId.of("Asia/Dhaka"), ZoneId.of("Asia/Dili"), ZoneId.of("Asia/Dubai"), ZoneId.of("Asia/Dushanbe"),
            ZoneId.of("Asia/Famagusta"), ZoneId.of("Asia/Gaza"), ZoneId.of("Asia/Hebron"), ZoneId.of("Asia/Ho_Chi_Minh"),
            ZoneId.of("Asia/Hong_Kong"), ZoneId.of("Asia/Hovd"), ZoneId.of("Asia/Irkutsk"), ZoneId.of("Asia/Jakarta"),
            ZoneId.of("Asia/Jayapura"), ZoneId.of("Asia/Jerusalem"), ZoneId.of("Asia/Kabul"), ZoneId.of("Asia/Kamchatka"),
            ZoneId.of("Asia/Karachi"), ZoneId.of("Asia/Kathmandu"), ZoneId.of("Asia/Khandyga"), ZoneId.of("Asia/Kolkata"),
            ZoneId.of("Asia/Krasnoyarsk"), ZoneId.of("Asia/Kuala_Lumpur"), ZoneId.of("Asia/Kuching"), ZoneId.of("Asia/Kuwait"),
            ZoneId.of("Asia/Macau"), ZoneId.of("Asia/Magadan"), ZoneId.of("Asia/Makassar"), ZoneId.of("Asia/Manila"),
            ZoneId.of("Asia/Muscat"), ZoneId.of("Asia/Nicosia"), ZoneId.of("Asia/Novokuznetsk"), ZoneId.of("Asia/Novosibirsk"),
            ZoneId.of("Asia/Omsk"), ZoneId.of("Asia/Oral"), ZoneId.of("Asia/Pontianak"), ZoneId.of("Asia/Pyongyang"),
            ZoneId.of("Asia/Qatar"), ZoneId.of("Asia/Qostanay"), ZoneId.of("Asia/Qyzylorda"), ZoneId.of("Asia/Riyadh"),
            ZoneId.of("Asia/Sakhalin"), ZoneId.of("Asia/Samarkand"), ZoneId.of("Asia/Seoul"), ZoneId.of("Asia/Shanghai"),
            ZoneId.of("Asia/Singapore"), ZoneId.of("Asia/Srednekolymsk"), ZoneId.of("Asia/Taipei"), ZoneId.of("Asia/Tashkent"),
            ZoneId.of("Asia/Tbilisi"), ZoneId.of("Asia/Tehran"), ZoneId.of("Asia/Thimphu"), ZoneId.of("Asia/Tokyo"),
            ZoneId.of("Asia/Tomsk"), ZoneId.of("Asia/Ulaanbaatar"), ZoneId.of("Asia/Urumqi"), ZoneId.of("Asia/Ust-Nera"),
            ZoneId.of("Asia/Vladivostok"), ZoneId.of("Asia/Yakutsk"), ZoneId.of("Asia/Yangon"), ZoneId.of("Asia/Yekaterinburg"),
            ZoneId.of("Asia/Yerevan")),
    ATLANTIC("Atlantic",
            ZoneId.of("Atlantic/Azores"), ZoneId.of("Atlantic/Bermuda"), ZoneId.of("Atlantic/Canary"),
            ZoneId.of("Atlantic/Cape_Verde"), ZoneId.of("Atlantic/Faroe"), ZoneId.of("Atlantic/Madeira"),
            ZoneId.of("Atlantic/Reykjavik"), ZoneId.of("Atlantic/South_Georgia"), ZoneId.of("Atlantic/Stanley")),
    AUSTRALIA("Australia",
            ZoneId.of("Australia/Adelaide"), ZoneId.of("Australia/Brisbane"),
            ZoneId.of("Australia/Darwin"), ZoneId.of("Australia/Eucla"), ZoneId.of("Australia/Hobart"),
            ZoneId.of("Australia/Lord_Howe"), ZoneId.of("Australia/Melbourne"),
            ZoneId.of("Australia/Perth"), ZoneId.of("Australia/Sydney")),
    EUROPE("Europe",
            ZoneId.of("Europe/Amsterdam"), ZoneId.of("Europe/Andorra"), ZoneId.of("Europe/Astrakhan"),
            ZoneId.of("Europe/Athens"), ZoneId.of("Europe/Belgrade"), ZoneId.of("Europe/Berlin"),
            ZoneId.of("Europe/Bratislava"), ZoneId.of("Europe/Brussels"), ZoneId.of("Europe/Bucharest"),
            ZoneId.of("Europe/Budapest"), ZoneId.of("Europe/Busingen"), ZoneId.of("Europe/Chisinau"),
            ZoneId.of("Europe/Copenhagen"), ZoneId.of("Europe/Dublin"), ZoneId.of("Europe/Gibraltar"),
            ZoneId.of("Europe/Guernsey"), ZoneId.of("Europe/Helsinki"), ZoneId.of("Europe/Isle_of_Man"),
            ZoneId.of("Europe/Istanbul"), ZoneId.of("Europe/Jersey"), ZoneId.of("Europe/Kaliningrad"),
            ZoneId.of("Europe/Kiev"), ZoneId.of("Europe/Kirov"), ZoneId.of("Europe/Lisbon"),
            ZoneId.of("Europe/Ljubljana"), ZoneId.of("Europe/Luxembourg"), ZoneId.of("Europe/Madrid"),
            ZoneId.of("Europe/Malta"), ZoneId.of("Europe/Mariehamn"), ZoneId.of("Europe/Minsk"),
            ZoneId.of("Europe/Monaco"), ZoneId.of("Europe/Moscow"), ZoneId.of("Europe/Oslo"),
            ZoneId.of("Europe/Paris"), ZoneId.of("Europe/Podgorica"), ZoneId.of("Europe/Prague"),
            ZoneId.of("Europe/Riga"), ZoneId.of("Europe/Rome"), ZoneId.of("Europe/Samara"),
            ZoneId.of("Europe/San_Marino"), ZoneId.of("Europe/Sarajevo"), ZoneId.of("Europe/Saratov"),
            ZoneId.of("Europe/Simferopol"), ZoneId.of("Europe/Skopje"), ZoneId.of("Europe/Sofia"),
            ZoneId.of("Europe/Stockholm"), ZoneId.of("Europe/Tallinn"), ZoneId.of("Europe/Tirane"),
            ZoneId.of("Europe/Ulyanovsk"), ZoneId.of("Europe/Uzhgorod"), ZoneId.of("Europe/Vaduz"),
            ZoneId.of("Europe/Vatican"), ZoneId.of("Europe/Vienna"), ZoneId.of("Europe/Vilnius"),
            ZoneId.of("Europe/Volgograd"), ZoneId.of("Europe/Warsaw"), ZoneId.of("Europe/Zagreb"),
            ZoneId.of("Europe/Zaporozhye"), ZoneId.of("Europe/Zurich")),
    INDIAN("Indian",
            ZoneId.of("Indian/Antananarivo"), ZoneId.of("Indian/Chagos"), ZoneId.of("Indian/Christmas"),
            ZoneId.of("Indian/Cocos"), ZoneId.of("Indian/Comoro"), ZoneId.of("Indian/Kerguelen"),
            ZoneId.of("Indian/Mahe"), ZoneId.of("Indian/Maldives"), ZoneId.of("Indian/Mauritius"),
            ZoneId.of("Indian/Mayotte"), ZoneId.of("Indian/Reunion")),
    PACIFIC("Pacific",
            ZoneId.of("Pacific/Apia"), ZoneId.of("Pacific/Auckland"), ZoneId.of("Pacific/Bougainville"),
            ZoneId.of("Pacific/Chatham"), ZoneId.of("Pacific/Chuuk"), ZoneId.of("Pacific/Easter"),
            ZoneId.of("Pacific/Efate"), ZoneId.of("Pacific/Enderbury"), ZoneId.of("Pacific/Fakaofo"),
            ZoneId.of("Pacific/Fiji"), ZoneId.of("Pacific/Funafuti"), ZoneId.of("Pacific/Galapagos"),
            ZoneId.of("Pacific/Gambier"), ZoneId.of("Pacific/Guadalcanal"), ZoneId.of("Pacific/Guam"),
            ZoneId.of("Pacific/Honolulu"), ZoneId.of("Pacific/Kiritimati"), ZoneId.of("Pacific/Kosrae"),
            ZoneId.of("Pacific/Kwajalein"), ZoneId.of("Pacific/Majuro"), ZoneId.of("Pacific/Marquesas"),
            ZoneId.of("Pacific/Midway"), ZoneId.of("Pacific/Nauru"), ZoneId.of("Pacific/Niue"),
            ZoneId.of("Pacific/Norfolk"), ZoneId.of("Pacific/Noumea"), ZoneId.of("Pacific/Pago_Pago"),
            ZoneId.of("Pacific/Palau"), ZoneId.of("Pacific/Pitcairn"), ZoneId.of("Pacific/Pohnpei"),
            ZoneId.of("Pacific/Port_Moresby"), ZoneId.of("Pacific/Rarotonga"), ZoneId.of("Pacific/Saipan"),
            ZoneId.of("Pacific/Tahiti"), ZoneId.of("Pacific/Tarawa"), ZoneId.of("Pacific/Tongatapu"),
            ZoneId.of("Pacific/Wake"), ZoneId.of("Pacific/Wallis")),

    ;

    private final String name;
    private final List<ZoneId> zoneIds;

    TZRegionEnum(String name, ZoneId... zoneIds) {
        this.name = name;
        this.zoneIds = List.of(zoneIds);
    }
    @Override
    public String toString() {
        return name; // Return the name field
    }

    public List<ZoneId> getZoneIds() {
        return zoneIds;
    } //sends zoneIds to the plugin panel
}