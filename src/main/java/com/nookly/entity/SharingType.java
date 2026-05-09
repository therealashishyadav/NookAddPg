package com.nookly.entity;

public enum SharingType {

    ONE_SHARING,
    TWO_SHARING,
    THREE_SHARING,
    FOUR_SHARING,
    FIVE_SHARING;

    public String displayLabel() {
        return switch (this) {
            case ONE_SHARING   -> "Single Room";
            case TWO_SHARING   -> "Twin Sharing";
            case THREE_SHARING -> "Triple Sharing";
            case FOUR_SHARING  -> "Four Sharing";
            case FIVE_SHARING  -> "Five Sharing";
        };
    }

    public int persons() {
        return switch (this) {
            case ONE_SHARING   -> 1;
            case TWO_SHARING   -> 2;
            case THREE_SHARING -> 3;
            case FOUR_SHARING  -> 4;
            case FIVE_SHARING  -> 5;
        };
    }
}