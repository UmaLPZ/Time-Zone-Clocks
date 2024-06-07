package com.tzclocks.tzdata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class TZClocksTab {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private boolean isCollapsed;

    @Getter
    private final List<UUID> clocks;

    public TZClocksTab(String name, List<UUID> clocks) {
        this.name = name;
        this.isCollapsed = true; // Collapsed by default
        this.clocks = clocks;
    }

    public void addClock(UUID clockId) {
        this.clocks.add(clockId);
    }

    public void removeClock(UUID clockId) {
        this.clocks.remove(clockId);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TZClocksTab)) {
            return false;
        }
        final TZClocksTab tab = (TZClocksTab) obj;
        return tab.getName().equals(this.name);
    }
}