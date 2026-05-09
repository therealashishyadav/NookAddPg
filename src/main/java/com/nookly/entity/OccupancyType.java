package com.nookly.entity;

public enum OccupancyType {

    GIRLS,
    BOYS,
    COED;

    public String displayLabel() {
        return switch (this) {
            case GIRLS -> "Girls";
            case BOYS  -> "Boys";
            case COED  -> "Coed";
        };
    }
}