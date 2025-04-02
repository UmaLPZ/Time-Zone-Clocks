package com.tzclocks.tzutilities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TZSourceMode {
    ABBREVIATION("Region/Abbreviation"),
    REGIONAL("Region/Zone ID");

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}