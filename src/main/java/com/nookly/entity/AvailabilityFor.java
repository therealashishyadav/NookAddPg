package com.nookly.entity;

public enum AvailabilityFor {

    STUDENTS,
    WORKING_PROFESSIONALS,
    BOTH;

    public String displayLabel() {
        return switch (this) {
            case STUDENTS              -> "Students";
            case WORKING_PROFESSIONALS -> "Working Professionals";
            case BOTH                  -> "Students & Professionals";
        };
    }
}