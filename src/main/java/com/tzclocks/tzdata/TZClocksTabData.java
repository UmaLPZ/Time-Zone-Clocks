package com.tzclocks.tzdata;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class TZClocksTabData {
    private String name;
    private boolean isCollapsed;
    private List<UUID> clocks;
}