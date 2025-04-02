package com.tzclocks.tzutilities;

import lombok.Getter;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Helper class to store a ZoneId along with a user-friendly display name.
 * The toString() method provides the display name for use in UI components like JComboBox.
 */
public class NamedZoneId implements Comparable<NamedZoneId> {

    @Getter
    private final String displayName;
    @Getter
    private final ZoneId zoneId;

    public NamedZoneId(String displayName, ZoneId zoneId) {
        if (displayName == null || displayName.trim().isEmpty()) {
            this.displayName = (zoneId != null) ? zoneId.getId() : "Unknown Zone";
        } else {
            this.displayName = displayName;
        }
        this.zoneId = Objects.requireNonNull(zoneId, "ZoneId cannot be null");
    }

    /**
     * Returns the user-friendly display name. This is used by JComboBox for rendering.
     * @return The display name.
     */
    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Compares based on the display name for sorting purposes.
     */
    @Override
    public int compareTo(NamedZoneId other) {
        return this.displayName.compareToIgnoreCase(other.displayName);
    }

    /**
     * Compares based on the display name for equality.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedZoneId that = (NamedZoneId) o;
        return Objects.equals(displayName, that.displayName);
    }

    /**
     * Hashes based on the display name.
     */
    @Override
    public int hashCode() {
        return Objects.hash(displayName);
    }
}