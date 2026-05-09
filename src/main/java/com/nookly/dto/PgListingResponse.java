package com.nookly.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// What Angular receives back — clean, flat, ready to render on the card/detail page

public class PgListingResponse {

    // ── Identity ───────────────────────────────────────────────────────────
    private Long id;
    private Long ownerId;

    // ── Basic Info ─────────────────────────────────────────────────────────
    private String pgName;
    private String fullAddress;
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

    // ── Room ───────────────────────────────────────────────────────────────
    private String occupancyType;       // "GIRLS" / "BOYS" / "COED"
    private String occupancyLabel;      // "Girls" / "Boys" / "Coed"
    private Double roomSizeSqFt;
    private Boolean furnished;
    private Boolean attachedWashroom;
    private Boolean balconyAvailable;
    private Boolean airConditioned;
    private String bedType;
    private String bedTypeLabel;
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
    private String housekeepingFrequency;
    private String housekeepingLabel;
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

    // ── Pricing ────────────────────────────────────────────────────────────
    private BigDecimal lowestPrice;     // ← "₹6,500 Onwards" — computed from sharingOptions
    private Double depositAmount;
    private Integer noticePeriodDays;
    private Integer lockInPeriodMonths;
    private String additionalChargesInfo;
    private String maintenanceChargesInfo;

    // ── Contact ────────────────────────────────────────────────────────────
    private String ownerName;
    private String contactNumber;
    private String whatsappNumber;
    private String email;
    private String visitingHours;
    private String availabilityFor;
    private String availabilityLabel;

    // ── Agreement ──────────────────────────────────────────────────────────
    private String agreementType;
    private String agreementLabel;
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

    // ── Status ─────────────────────────────────────────────────────────────
    private Boolean isActive;
    private Boolean isVerified;
    private Boolean isBrandNew;
    private Boolean isPartnerVerified;
    private Double rating;
    private Integer totalReviews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Sharing Options ← THE KEY PART ────────────────────────────────────
    // Each PG returns only the sharing types it actually offers
    private List<SharingOptionResponse> sharingOptions;

    // ── Inner response — one per sharing type ─────────────────────────────
    public static class SharingOptionResponse {
        private Long id;
        private String sharingType;       
        private String label;              
        private int persons;               
        private BigDecimal pricePerMonth;  
        private Integer totalBeds;
        private Integer availableBeds;
        private List<String> amenities;
        private Boolean isAvailable;

        // getters & setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getSharingType() { return sharingType; }
        public void setSharingType(String sharingType) { this.sharingType = sharingType; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public int getPersons() { return persons; }
        public void setPersons(int persons) { this.persons = persons; }

        public BigDecimal getPricePerMonth() { return pricePerMonth; }
        public void setPricePerMonth(BigDecimal pricePerMonth) { this.pricePerMonth = pricePerMonth; }

        public Integer getTotalBeds() { return totalBeds; }
        public void setTotalBeds(Integer totalBeds) { this.totalBeds = totalBeds; }

        public Integer getAvailableBeds() { return availableBeds; }
        public void setAvailableBeds(Integer availableBeds) { this.availableBeds = availableBeds; }

        public List<String> getAmenities() { return amenities; }
        public void setAmenities(List<String> amenities) { this.amenities = amenities; }

        public Boolean getIsAvailable() { return isAvailable; }
        public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    }

    // ── Getters & Setters ──────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

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

    public String getOccupancyType() { return occupancyType; }
    public void setOccupancyType(String occupancyType) { this.occupancyType = occupancyType; }

    public String getOccupancyLabel() { return occupancyLabel; }
    public void setOccupancyLabel(String occupancyLabel) { this.occupancyLabel = occupancyLabel; }

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

    public String getBedType() { return bedType; }
    public void setBedType(String bedType) { this.bedType = bedType; }

    public String getBedTypeLabel() { return bedTypeLabel; }
    public void setBedTypeLabel(String bedTypeLabel) { this.bedTypeLabel = bedTypeLabel; }

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

    public String getHousekeepingFrequency() { return housekeepingFrequency; }
    public void setHousekeepingFrequency(String housekeepingFrequency) { this.housekeepingFrequency = housekeepingFrequency; }

    public String getHousekeepingLabel() { return housekeepingLabel; }
    public void setHousekeepingLabel(String housekeepingLabel) { this.housekeepingLabel = housekeepingLabel; }

    public Boolean getCctvSurveillance() { return cctvSurveillance; }
    public void setCctvSurveillance(Boolean cctvSurveillance) { this.cctvSurveillance = cctvSurveillance; }

    public Boolean getSecurityGuardAvailable() { return securityGuardAvailable; }
    public void setSecurityGuardAvailable(Boolean securityGuardAvailable) { this.securityGuardAvailable = securityGuardAvailable; }

    public Boolean getLiftAvailable() { return liftAvailable; }
    public void setLiftAvailable(Boolean liftAvailable) { this.liftAvailable = liftAvailable; }

    public Boolean getTwoWheelerParking() { return twoWheelerParking; }
    public void setTwoWheelerParking(Boolean twoWheelerParking) { this.twoWheelerParking = twoWheelerParking; }

    public Boolean getFourWheelerParking() { return fourWheelerParking; }
    public void setFourWheelerParking(Boolean fourWheelerParking) { this.fourWheelerParking = fourWheelerParking; }

    public Boolean getLoungeAvailable() { return loungeAvailable; }
    public void setLoungeAvailable(Boolean loungeAvailable) { this.loungeAvailable = loungeAvailable; }

    public Boolean getRecreationAreaAvailable() { return recreationAreaAvailable; }
    public void setRecreationAreaAvailable(Boolean recreationAreaAvailable) { this.recreationAreaAvailable = recreationAreaAvailable; }

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
    public void setWaterPurifierAvailable(Boolean waterPurifierAvailable) { this.waterPurifierAvailable = waterPurifierAvailable; }

    public Boolean getDispenserAvailable() { return dispenserAvailable; }
    public void setDispenserAvailable(Boolean dispenserAvailable) { this.dispenserAvailable = dispenserAvailable; }

    public String getEntryExitTimings() { return entryExitTimings; }
    public void setEntryExitTimings(String entryExitTimings) { this.entryExitTimings = entryExitTimings; }

    public Boolean getVisitorsAllowed() { return visitorsAllowed; }
    public void setVisitorsAllowed(Boolean visitorsAllowed) { this.visitorsAllowed = visitorsAllowed; }

    public Boolean getGuestsOvernightAllowed() { return guestsOvernightAllowed; }
    public void setGuestsOvernightAllowed(Boolean guestsOvernightAllowed) { this.guestsOvernightAllowed = guestsOvernightAllowed; }

    public Double getSecurityDepositAmount() { return securityDepositAmount; }
    public void setSecurityDepositAmount(Double securityDepositAmount) { this.securityDepositAmount = securityDepositAmount; }

    public Boolean getIdVerificationRequired() { return idVerificationRequired; }
    public void setIdVerificationRequired(Boolean idVerificationRequired) { this.idVerificationRequired = idVerificationRequired; }

    public Boolean getFireSafetyAvailable() { return fireSafetyAvailable; }
    public void setFireSafetyAvailable(Boolean fireSafetyAvailable) { this.fireSafetyAvailable = fireSafetyAvailable; }

    public Boolean getSmokingAllowed() { return smokingAllowed; }
    public void setSmokingAllowed(Boolean smokingAllowed) { this.smokingAllowed = smokingAllowed; }

    public Boolean getPetsAllowed() { return petsAllowed; }
    public void setPetsAllowed(Boolean petsAllowed) { this.petsAllowed = petsAllowed; }

    public Boolean getAlcoholAllowed() { return alcoholAllowed; }
    public void setAlcoholAllowed(Boolean alcoholAllowed) { this.alcoholAllowed = alcoholAllowed; }

    public BigDecimal getLowestPrice() { return lowestPrice; }
    public void setLowestPrice(BigDecimal lowestPrice) { this.lowestPrice = lowestPrice; }

    public Double getDepositAmount() { return depositAmount; }
    public void setDepositAmount(Double depositAmount) { this.depositAmount = depositAmount; }

    public Integer getNoticePeriodDays() { return noticePeriodDays; }
    public void setNoticePeriodDays(Integer noticePeriodDays) { this.noticePeriodDays = noticePeriodDays; }

    public Integer getLockInPeriodMonths() { return lockInPeriodMonths; }
    public void setLockInPeriodMonths(Integer lockInPeriodMonths) { this.lockInPeriodMonths = lockInPeriodMonths; }

    public String getAdditionalChargesInfo() { return additionalChargesInfo; }
    public void setAdditionalChargesInfo(String additionalChargesInfo) { this.additionalChargesInfo = additionalChargesInfo; }

    public String getMaintenanceChargesInfo() { return maintenanceChargesInfo; }
    public void setMaintenanceChargesInfo(String maintenanceChargesInfo) { this.maintenanceChargesInfo = maintenanceChargesInfo; }

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

    public String getAvailabilityFor() { return availabilityFor; }
    public void setAvailabilityFor(String availabilityFor) { this.availabilityFor = availabilityFor; }

    public String getAvailabilityLabel() { return availabilityLabel; }
    public void setAvailabilityLabel(String availabilityLabel) { this.availabilityLabel = availabilityLabel; }

    public String getAgreementType() { return agreementType; }
    public void setAgreementType(String agreementType) { this.agreementType = agreementType; }

    public String getAgreementLabel() { return agreementLabel; }
    public void setAgreementLabel(String agreementLabel) { this.agreementLabel = agreementLabel; }

    public Integer getMinimumStayMonths() { return minimumStayMonths; }
    public void setMinimumStayMonths(Integer minimumStayMonths) { this.minimumStayMonths = minimumStayMonths; }

    public Integer getNoticePeriodToLeaveDays() { return noticePeriodToLeaveDays; }
    public void setNoticePeriodToLeaveDays(Integer noticePeriodToLeaveDays) { this.noticePeriodToLeaveDays = noticePeriodToLeaveDays; }

    public String getRefundPolicy() { return refundPolicy; }
    public void setRefundPolicy(String refundPolicy) { this.refundPolicy = refundPolicy; }

    public String getHouseRulesDocumentUrl() { return houseRulesDocumentUrl; }
    public void setHouseRulesDocumentUrl(String houseRulesDocumentUrl) { this.houseRulesDocumentUrl = houseRulesDocumentUrl; }

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

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public Boolean getIsBrandNew() { return isBrandNew; }
    public void setIsBrandNew(Boolean isBrandNew) { this.isBrandNew = isBrandNew; }

    public Boolean getIsPartnerVerified() { return isPartnerVerified; }
    public void setIsPartnerVerified(Boolean isPartnerVerified) { this.isPartnerVerified = isPartnerVerified; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<SharingOptionResponse> getSharingOptions() { return sharingOptions; }
    public void setSharingOptions(List<SharingOptionResponse> sharingOptions) { this.sharingOptions = sharingOptions; }
}
