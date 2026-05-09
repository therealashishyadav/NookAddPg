package com.nookly.entity;

public enum AgreementType {

    RENTAL_AGREEMENT,
    LEAVE_AND_LICENSE,
    NONE;

    public String displayLabel() {
        return switch (this) {
            case RENTAL_AGREEMENT  -> "Rental Agreement";
            case LEAVE_AND_LICENSE -> "Leave & License";
            case NONE              -> "No Agreement";
        };
    }
}