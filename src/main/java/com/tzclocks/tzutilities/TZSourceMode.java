package com.tzclocks.tzutilities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TZSourceMode {
    REGIONAL("Region / Zone ID"),
    ABBREVIATION("Abbreviation / Offset");

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}