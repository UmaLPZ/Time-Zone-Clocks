package com.tzclocks.tzdata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
public class TZClocksItem {
    @Getter private final UUID uuid; // Unique ID for the clock
    @Getter private String name; // Timezone ID
    @Getter @Setter private String currentTime; // Current time in the timezone
    @Getter @Setter private String customName; // Custom name for the clock
}