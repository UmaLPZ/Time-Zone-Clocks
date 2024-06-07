package com.tzclocks.tzdata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TZClocksTab {
    private String name;
    private List<UUID> clocks;
    private boolean isCollapsed; // Change field name to isCollapsed

    public TZClocksTab(String name) {
        this.name = name;
        this.clocks = new ArrayList<>();
        this.isCollapsed = true; // Tabs are collapsed by default
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UUID> getClocks() {
        return clocks;
    }

    public void addClock(UUID clockId) {
        clocks.add(clockId);
    }

    public void removeClock(UUID clockId) {
        clocks.remove(clockId);
    }

    public boolean isCollapsed() {
        return isCollapsed;
    }
    public void setCollapsed(boolean collapsed) {
        this.isCollapsed = collapsed;
    }
}