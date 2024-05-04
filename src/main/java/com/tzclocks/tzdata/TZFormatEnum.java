package com.tzclocks.tzdata;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TZFormatEnum { //time zone formats
    TWELVE_HOUR("12-Hour"),
    TWENTY_FOUR_HOUR("24-Hour");

    private final String name;

    @Override
    public String toString()
    {
        return name;
    }
}

