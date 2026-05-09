package com.nookly.entity;

public enum BedType {

    SINGLE,
    DOUBLE,
    BUNK,
    SOFA_BED;

    public String displayLabel() {
        return switch (this) {
            case SINGLE   -> "Single Bed";
            case DOUBLE   -> "Double Bed";
            case BUNK     -> "Bunk Bed";
            case SOFA_BED -> "Sofa Bed";
        };
    }
}