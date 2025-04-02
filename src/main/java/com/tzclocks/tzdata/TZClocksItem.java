package com.tzclocks.tzdata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
public class TZClocksItem {
    @Getter
    private final UUID uuid;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String currentTime;
    @Getter
    @Setter
    private String customName;
    @Getter
    @Setter
    private String showCalendar;
    @Getter
    @Setter
    private String displayName;

}