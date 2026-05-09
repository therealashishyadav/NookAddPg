package com.nookly.entity;

public enum HousekeepingFrequency {

    DAILY,
    ALTERNATE_DAYS,
    WEEKLY,
    MONTHLY,
    NONE;

    public String displayLabel() {
        return switch (this) {
            case DAILY          -> "Daily";
            case ALTERNATE_DAYS -> "Alternate Days";
            case WEEKLY         -> "Weekly";
            case MONTHLY        -> "Monthly";
            case NONE           -> "Not Available";
        };
    }
}