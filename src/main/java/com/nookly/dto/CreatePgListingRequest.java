package com.nookly.dto;

import com.nookly.entity.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// ═════════════════════════════════════════════════════════════════════════════
// REQUEST DTO — what Angular sends when owner creates/updates a PG listing
// ═════════════════════════════════════════════════════════════════════════════

public class CreatePgListingRequest {

    // ── Basic Info ─────────────────────────────────────────────────────────
    @NotBlank(message = "PG name is required")
    private String pgName;

    @NotBlank(message = "Address is required")
    private String fullAddress;

    @NotBlank(message = "City is required")
    private String city;

    private String locality;
    private String pinCode;
    private String googleMapLink;
    private String nearbyLandmarks;

    // ── Media ──────────────────────────────────────────────────────────────
    private String coverImageUrl;
    private List<String> galleryImages;
    private String virtualTourLink;
    private String videoLink;

    // ── Room Details ───────────────────────────────────────────────────────
    private OccupancyType occupancyType;
    private Double roomSizeSqFt;
    private Boolean furnished;
    private Boolean attachedWashroom;
    private Boolean balconyAvailable;
    private Boolean airConditioned;
    private BedType bedType;
    private Boolean mattressProvided;
    private Boolean studyTableAvailable;

    // ── Food ───────────────────────────────────────────────────────────────
    private Boolean foodProvided;
    private String mealTypes;
    private String foodOptions;
    private Boolean cookingAllowed;
    private Boolean commonKitchenAccess;
    private Boolean fridgeAvailable;
    private Boolean microwaveAvailable;

    // ── Amenities ──────────────────────────────────────────────────────────
    private Boolean wifiAvailable;
    private Boolean powerBackupAvailable;
    private Boolean geyserAvailable;
    private Boolean washingMachineAvailable;
    private HousekeepingFrequency housekeepingFrequency;
    private Boolean cctvSurveillance;
    private Boolean securityGuardAvailable;
    private Boolean liftAvailable;
    private Boolean twoWheelerParking;
    private Boolean fourWheelerParking;
    private Boolean loungeAvailable;
    private Boolean recreationAreaAvailable;
    private Boolean gymAvailable;
    private Boolean rooftopAccess;

    // ── Services ───────────────────────────────────────────────────────────
    private Boolean dailyCleaning;
    private Boolean laundryService;
    private Boolean maintenanceOnCall;
    private Boolean waterPurifierAvailable;
    private Boolean dispenserAvailable;

    // ── Rules & Safety ─────────────────────────────────────────────────────
    private String entryExitTimings;
    private Boolean visitorsAllowed;
    private Boolean guestsOvernightAllowed;
    private Double securityDepositAmount;
    private Boolean idVerificationRequired;
    private Boolean fireSafetyAvailable;
    private Boolean smokingAllowed;
    private Boolean petsAllowed;
    private Boolean alcoholAllowed;

    // ── Pricing (general) ──────────────────────────────────────────────────
    private Double depositAmount;
    private Integer noticePeriodDays;
    private Integer lockInPeriodMonths;
    private String additionalChargesInfo;
    private String maintenanceChargesInfo;

    // ── Contact ────────────────────────────────────────────────────────────
    private String ownerName;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String contactNumber;

    private String whatsappNumber;
    private String email;
    private String visitingHours;
    private AvailabilityFor availabilityFor;

    // ── Agreement ──────────────────────────────────────────────────────────
    private AgreementType agreementType;
    private Integer minimumStayMonths;
    private Integer noticePeriodToLeaveDays;
    private String refundPolicy;
    private String houseRulesDocumentUrl;

    // ── Offers ─────────────────────────────────────────────────────────────
    private String specialOffers;
    private String earlyBirdDiscounts;
    private String referralBonuses;

    // ── Availability ───────────────────────────────────────────────────────
    private Boolean immediatePossession;
    private LocalDate availableFromDate;
    private Boolean waitingList;
    private Integer totalRooms;
    private Integer availableRooms;

    // ── Sharing Options (the prices) ───────────────────────────────────────
    @NotEmpty(message = "At least one sharing option is required")
    private List<SharingOptionRequest> sharingOptions;

    // ── Inner DTO — one per sharing type ──────────────────────────────────
    public static class SharingOptionRequest {

        @NotNull(message = "Sharing type is required")
        private SharingType sharingType;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "1.0", message = "Price must be greater than 0")
        private BigDecimal pricePerMonth;

        @Min(value = 1, message = "Total beds must be at least 1")
        private Integer totalBeds;

        private List<String> amenities;

        // getters & setters
        public SharingType getSharingType() { return sharingType; }
        public void setSharingType(SharingType sharingType) { this.sharingType = sharingType; }

        public BigDecimal getPricePerMonth() { return pricePerMonth; }
        public void setPricePerMonth(BigDecimal pricePerMonth) { this.pricePerMonth = pricePerMonth; }

        public Integer getTotalBeds() { return totalBeds; }
        public void setTotalBeds(Integer totalBeds) { this.totalBeds = totalBeds; }

        public List<String> getAmenities() { return amenities; }
        public void setAmenities(List<String> amenities) { this.amenities = amenities; }
    }

    // ── Getters & Setters ──────────────────────────────────────────────────
    public String getPgName() { return pgName; }
    public void setPgName(String pgName) { this.pgName = pgName; }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }

    public String getPinCode() { return pinCode; }
    public void setPinCode(String pinCode) { this.pinCode = pinCode; }

    public String getGoogleMapLink() { return googleMapLink; }
    public void setGoogleMapLink(String googleMapLink) { this.googleMapLink = googleMapLink; }

    public String getNearbyLandmarks() { return nearbyLandmarks; }
    public void setNearbyLandmarks(String nearbyLandmarks) { this.nearbyLandmarks = nearbyLandmarks; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public List<String> getGalleryImages() { return galleryImages; }
    public void setGalleryImages(List<String> galleryImages) { this.galleryImages = galleryImages; }

    public String getVirtualTourLink() { return virtualTourLink; }
    public void setVirtualTourLink(String virtualTourLink) { this.virtualTourLink = virtualTourLink; }

    public String getVideoLink() { return videoLink; }
    public void setVideoLink(String videoLink) { this.videoLink = videoLink; }

    public OccupancyType getOccupancyType() { return occupancyType; }
    public void setOccupancyType(OccupancyType occupancyType) { this.occupancyType = occupancyType; }

    public Double getRoomSizeSqFt() { return roomSizeSqFt; }
    public void setRoomSizeSqFt(Double roomSizeSqFt) { this.roomSizeSqFt = roomSizeSqFt; }

    public Boolean getFurnished() { return furnished; }
    public void setFurnished(Boolean furnished) { this.furnished = furnished; }

    public Boolean getAttachedWashroom() { return attachedWashroom; }
    public void setAttachedWashroom(Boolean attachedWashroom) { this.attachedWashroom = attachedWashroom; }

    public Boolean getBalconyAvailable() { return balconyAvailable; }
    public void setBalconyAvailable(Boolean balconyAvailable) { this.balconyAvailable = balconyAvailable; }

    public Boolean getAirConditioned() { return airConditioned; }
    public void setAirConditioned(Boolean airConditioned) { this.airConditioned = airConditioned; }

    public BedType getBedType() { return bedType; }
    public void setBedType(BedType bedType) { this.bedType = bedType; }

    public Boolean getMattressProvided() { return mattressProvided; }
    public void setMattressProvided(Boolean mattressProvided) { this.mattressProvided = mattressProvided; }

    public Boolean getStudyTableAvailable() { return studyTableAvailable; }
    public void setStudyTableAvailable(Boolean studyTableAvailable) { this.studyTableAvailable = studyTableAvailable; }

    public Boolean getFoodProvided() { return foodProvided; }
    public void setFoodProvided(Boolean foodProvided) { this.foodProvided = foodProvided; }

    public String getMealTypes() { return mealTypes; }
    public void setMealTypes(String mealTypes) { this.mealTypes = mealTypes; }

    public String getFoodOptions() { return foodOptions; }
    public void setFoodOptions(String foodOptions) { this.foodOptions = foodOptions; }

    public Boolean getCookingAllowed() { return cookingAllowed; }
    public void setCookingAllowed(Boolean cookingAllowed) { this.cookingAllowed = cookingAllowed; }

    public Boolean getCommonKitchenAccess() { return commonKitchenAccess; }
    public void setCommonKitchenAccess(Boolean commonKitchenAccess) { this.commonKitchenAccess = commonKitchenAccess; }

    public Boolean getFridgeAvailable() { return fridgeAvailable; }
    public void setFridgeAvailable(Boolean fridgeAvailable) { this.fridgeAvailable = fridgeAvailable; }

    public Boolean getMicrowaveAvailable() { return microwaveAvailable; }
    public void setMicrowaveAvailable(Boolean microwaveAvailable) { this.microwaveAvailable = microwaveAvailable; }

    public Boolean getWifiAvailable() { return wifiAvailable; }
    public void setWifiAvailable(Boolean wifiAvailable) { this.wifiAvailable = wifiAvailable; }

    public Boolean getPowerBackupAvailable() { return powerBackupAvailable; }
    public void setPowerBackupAvailable(Boolean powerBackupAvailable) { this.powerBackupAvailable = powerBackupAvailable; }

    public Boolean getGeyserAvailable() { return geyserAvailable; }
    public void setGeyserAvailable(Boolean geyserAvailable) { this.geyserAvailable = geyserAvailable; }

    public Boolean getWashingMachineAvailable() { return washingMachineAvailable; }
    public void setWashingMachineAvailable(Boolean washingMachineAvailable) { this.washingMachineAvailable = washingMachineAvailable; }

    public HousekeepingFrequency getHousekeepingFrequency() { return housekeepingFrequency; }
    public void setHousekeepingFrequency(HousekeepingFrequency h) { this.housekeepingFrequency = h; }

    public Boolean getCctvSurveillance() { return cctvSurveillance; }
    public void setCctvSurveillance(Boolean cctvSurveillance) { this.cctvSurveillance = cctvSurveillance; }

    public Boolean getSecurityGuardAvailable() { return securityGuardAvailable; }
    public void setSecurityGuardAvailable(Boolean v) { this.securityGuardAvailable = v; }

    public Boolean getLiftAvailable() { return liftAvailable; }
    public void setLiftAvailable(Boolean liftAvailable) { this.liftAvailable = liftAvailable; }

    public Boolean getTwoWheelerParking() { return twoWheelerParking; }
    public void setTwoWheelerParking(Boolean v) { this.twoWheelerParking = v; }

    public Boolean getFourWheelerParking() { return fourWheelerParking; }
    public void setFourWheelerParking(Boolean v) { this.fourWheelerParking = v; }

    public Boolean getLoungeAvailable() { return loungeAvailable; }
    public void setLoungeAvailable(Boolean v) { this.loungeAvailable = v; }

    public Boolean getRecreationAreaAvailable() { return recreationAreaAvailable; }
    public void setRecreationAreaAvailable(Boolean v) { this.recreationAreaAvailable = v; }

    public Boolean getGymAvailable() { return gymAvailable; }
    public void setGymAvailable(Boolean gymAvailable) { this.gymAvailable = gymAvailable; }

    public Boolean getRooftopAccess() { return rooftopAccess; }
    public void setRooftopAccess(Boolean rooftopAccess) { this.rooftopAccess = rooftopAccess; }

    public Boolean getDailyCleaning() { return dailyCleaning; }
    public void setDailyCleaning(Boolean dailyCleaning) { this.dailyCleaning = dailyCleaning; }

    public Boolean getLaundryService() { return laundryService; }
    public void setLaundryService(Boolean laundryService) { this.laundryService = laundryService; }

    public Boolean getMaintenanceOnCall() { return maintenanceOnCall; }
    public void setMaintenanceOnCall(Boolean maintenanceOnCall) { this.maintenanceOnCall = maintenanceOnCall; }

    public Boolean getWaterPurifierAvailable() { return waterPurifierAvailable; }
    public void setWaterPurifierAvailable(Boolean v) { this.waterPurifierAvailable = v; }

    public Boolean getDispenserAvailable() { return dispenserAvailable; }
    public void setDispenserAvailable(Boolean dispenserAvailable) { this.dispenserAvailable = dispenserAvailable; }

    public String getEntryExitTimings() { return entryExitTimings; }
    public void setEntryExitTimings(String entryExitTimings) { this.entryExitTimings = entryExitTimings; }

    public Boolean getVisitorsAllowed() { return visitorsAllowed; }
    public void setVisitorsAllowed(Boolean visitorsAllowed) { this.visitorsAllowed = visitorsAllowed; }

    public Boolean getGuestsOvernightAllowed() { return guestsOvernightAllowed; }
    public void setGuestsOvernightAllowed(Boolean v) { this.guestsOvernightAllowed = v; }

    public Double getSecurityDepositAmount() { return securityDepositAmount; }
    public void setSecurityDepositAmount(Double v) { this.securityDepositAmount = v; }

    public Boolean getIdVerificationRequired() { return idVerificationRequired; }
    public void setIdVerificationRequired(Boolean v) { this.idVerificationRequired = v; }

    public Boolean getFireSafetyAvailable() { return fireSafetyAvailable; }
    public void setFireSafetyAvailable(Boolean v) { this.fireSafetyAvailable = v; }

    public Boolean getSmokingAllowed() { return smokingAllowed; }
    public void setSmokingAllowed(Boolean smokingAllowed) { this.smokingAllowed = smokingAllowed; }

    public Boolean getPetsAllowed() { return petsAllowed; }
    public void setPetsAllowed(Boolean petsAllowed) { this.petsAllowed = petsAllowed; }

    public Boolean getAlcoholAllowed() { return alcoholAllowed; }
    public void setAlcoholAllowed(Boolean alcoholAllowed) { this.alcoholAllowed = alcoholAllowed; }

    public Double getDepositAmount() { return depositAmount; }
    public void setDepositAmount(Double depositAmount) { this.depositAmount = depositAmount; }

    public Integer getNoticePeriodDays() { return noticePeriodDays; }
    public void setNoticePeriodDays(Integer noticePeriodDays) { this.noticePeriodDays = noticePeriodDays; }

    public Integer getLockInPeriodMonths() { return lockInPeriodMonths; }
    public void setLockInPeriodMonths(Integer lockInPeriodMonths) { this.lockInPeriodMonths = lockInPeriodMonths; }

    public String getAdditionalChargesInfo() { return additionalChargesInfo; }
    public void setAdditionalChargesInfo(String v) { this.additionalChargesInfo = v; }

    public String getMaintenanceChargesInfo() { return maintenanceChargesInfo; }
    public void setMaintenanceChargesInfo(String v) { this.maintenanceChargesInfo = v; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getWhatsappNumber() { return whatsappNumber; }
    public void setWhatsappNumber(String whatsappNumber) { this.whatsappNumber = whatsappNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getVisitingHours() { return visitingHours; }
    public void setVisitingHours(String visitingHours) { this.visitingHours = visitingHours; }

    public AvailabilityFor getAvailabilityFor() { return availabilityFor; }
    public void setAvailabilityFor(AvailabilityFor availabilityFor) { this.availabilityFor = availabilityFor; }

    public AgreementType getAgreementType() { return agreementType; }
    public void setAgreementType(AgreementType agreementType) { this.agreementType = agreementType; }

    public Integer getMinimumStayMonths() { return minimumStayMonths; }
    public void setMinimumStayMonths(Integer minimumStayMonths) { this.minimumStayMonths = minimumStayMonths; }

    public Integer getNoticePeriodToLeaveDays() { return noticePeriodToLeaveDays; }
    public void setNoticePeriodToLeaveDays(Integer v) { this.noticePeriodToLeaveDays = v; }

    public String getRefundPolicy() { return refundPolicy; }
    public void setRefundPolicy(String refundPolicy) { this.refundPolicy = refundPolicy; }

    public String getHouseRulesDocumentUrl() { return houseRulesDocumentUrl; }
    public void setHouseRulesDocumentUrl(String v) { this.houseRulesDocumentUrl = v; }

    public String getSpecialOffers() { return specialOffers; }
    public void setSpecialOffers(String specialOffers) { this.specialOffers = specialOffers; }

    public String getEarlyBirdDiscounts() { return earlyBirdDiscounts; }
    public void setEarlyBirdDiscounts(String earlyBirdDiscounts) { this.earlyBirdDiscounts = earlyBirdDiscounts; }

    public String getReferralBonuses() { return referralBonuses; }
    public void setReferralBonuses(String referralBonuses) { this.referralBonuses = referralBonuses; }

    public Boolean getImmediatePossession() { return immediatePossession; }
    public void setImmediatePossession(Boolean immediatePossession) { this.immediatePossession = immediatePossession; }

    public LocalDate getAvailableFromDate() { return availableFromDate; }
    public void setAvailableFromDate(LocalDate availableFromDate) { this.availableFromDate = availableFromDate; }

    public Boolean getWaitingList() { return waitingList; }
    public void setWaitingList(Boolean waitingList) { this.waitingList = waitingList; }

    public Integer getTotalRooms() { return totalRooms; }
    public void setTotalRooms(Integer totalRooms) { this.totalRooms = totalRooms; }

    public Integer getAvailableRooms() { return availableRooms; }
    public void setAvailableRooms(Integer availableRooms) { this.availableRooms = availableRooms; }

    public List<SharingOptionRequest> getSharingOptions() { return sharingOptions; }
    public void setSharingOptions(List<SharingOptionRequest> sharingOptions) { this.sharingOptions = sharingOptions; }
}
