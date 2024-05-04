package com.tzclocks.tzdata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class TZClocksItem {
    @Getter private String name; // Timezone ID
    @Getter @Setter private String currentTime; // Current time in the timezone
}