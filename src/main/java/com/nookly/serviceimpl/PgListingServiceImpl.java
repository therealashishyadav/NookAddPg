package com.nookly.serviceimpl;

import com.nookly.dto.CreatePgListingRequest;
import com.nookly.dto.PgListingResponse;
import com.nookly.entity.*;
import com.nookly.repository.PgListingRepository;
import com.nookly.repository.PgSharingOptionRepository;
import com.nookly.service.PgListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PgListingServiceImpl implements PgListingService {

    @Autowired
    private PgListingRepository pgListingRepository;

    @Autowired
    private PgSharingOptionRepository sharingOptionRepository;
    

    // ── CREATE ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public PgListingResponse createListing(CreatePgListingRequest request, Long ownerId) {

        // 1. Map request → PgListing entity
        PgListing pg = mapRequestToEntity(request);
        pg.setOwnerId(ownerId);
        pg.setIsActive(true);
        pg.setIsVerified(false);
        pg.setRating(0.0);
        pg.setTotalReviews(0);

        // 2. Build sharing options — each gets price + sharingType
        List<PgSharingOption> options = request.getSharingOptions().stream()
            .map(optReq -> {
                PgSharingOption opt = new PgSharingOption();
                opt.setPgListing(pg);
                opt.setSharingType(optReq.getSharingType());
                opt.setPricePerMonth(optReq.getPricePerMonth());
                opt.setTotalBeds(optReq.getTotalBeds());
                opt.setAvailableBeds(optReq.getTotalBeds()); // all available at start
                opt.setAmenities(optReq.getAmenities() != null ? optReq.getAmenities() : new ArrayList<>());
                opt.setIsAvailable(true);
                return opt;
            })
            .collect(Collectors.toList());

        pg.setSharingOptions(options);

        // 3. Save — cascades to sharing options automatically
        PgListing saved = pgListingRepository.save(pg);

        return toResponse(saved);
    }

    // ── GET BY ID ──────────────────────────────────────────────────────────
    @Override
    public PgListingResponse getListingById(Long id) {
        PgListing pg = pgListingRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "PG listing not found with id: " + id));
        return toResponse(pg);
    }

    // ── GET BY OWNER ───────────────────────────────────────────────────────
    @Override
    public List<PgListingResponse> getListingsByOwner(Long ownerId) {
        return pgListingRepository.findByOwnerIdAndIsActiveTrue(ownerId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ── GET BY CITY ────────────────────────────────────────────────────────
    @Override
    public Page<PgListingResponse> getListingsByCity(String city, Pageable pageable) {
        return pgListingRepository
            .findByCityIgnoreCaseAndIsActiveTrue(city, pageable)
            .map(this::toResponse);
    }

    // ── FILTER ─────────────────────────────────────────────────────────────
    @Override
    public Page<PgListingResponse> filterListings(
            String city, OccupancyType occupancyType,
            SharingType sharingType, BigDecimal minPrice, BigDecimal maxPrice,
            Boolean foodProvided, Boolean wifiAvailable, Pageable pageable) {

        // Use price-range query when sharing type + price provided
        if (sharingType != null && minPrice != null && maxPrice != null) {
            return pgListingRepository
                .searchByPriceRange(city, sharingType, minPrice, maxPrice, pageable)
                .map(this::toResponse);
        }

        // General filter otherwise
        return pgListingRepository
            .filterListings(city, occupancyType, foodProvided, wifiAvailable, pageable)
            .map(this::toResponse);
    }

    // ── TOP RATED ──────────────────────────────────────────────────────────
    @Override
    public List<PgListingResponse> getTopRatedByCity(String city) {
        return pgListingRepository
            .findTop10ByCityIgnoreCaseAndIsActiveTrueOrderByRatingDesc(city)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public PgListingResponse updateListing(Long id, CreatePgListingRequest request, Long ownerId) {
        PgListing pg = pgListingRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PG not found"));

        if (!pg.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this listing");
        }

        // 🔥 FIX: Delete all existing sharing options for this PG
        sharingOptionRepository.deleteAllByPgListingId(id);

        // Update all other fields
        mapRequestToEntity(request, pg);

        // Create new sharing options
        List<PgSharingOption> updatedOptions = request.getSharingOptions().stream()
            .map(optReq -> {
                PgSharingOption opt = new PgSharingOption();
                opt.setPgListing(pg);
                opt.setSharingType(optReq.getSharingType());
                opt.setPricePerMonth(optReq.getPricePerMonth());
                opt.setTotalBeds(optReq.getTotalBeds());
                opt.setAvailableBeds(optReq.getTotalBeds());
                opt.setAmenities(optReq.getAmenities() != null ? optReq.getAmenities() : new ArrayList<>());
                opt.setIsAvailable(true);
                return opt;
            })
            .collect(Collectors.toList());

        pg.setSharingOptions(updatedOptions);

        // Save – now only the new options exist
        PgListing saved = pgListingRepository.save(pg);
        return toResponse(saved);
    }

    // ── DEACTIVATE ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void deactivateListing(Long id, Long ownerId) {
        PgListing pg = pgListingRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PG not found"));

        if (!pg.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this listing");
        }

        pg.setIsActive(false);
        pgListingRepository.save(pg);
    }

    // ── DELETE ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void deleteListing(Long id, Long ownerId) {
        PgListing pg = pgListingRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PG not found"));

        if (!pg.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this listing");
        }

        pgListingRepository.delete(pg);
    }

    // ══════════════════════════════════════════════════════════════════════
    // MAPPER — Request → Entity (new)
    // ══════════════════════════════════════════════════════════════════════
    private PgListing mapRequestToEntity(CreatePgListingRequest req) {
        PgListing pg = new PgListing();
        return mapRequestToEntity(req, pg);
    }

    private PgListing mapRequestToEntity(CreatePgListingRequest req, PgListing pg) {
        pg.setPgName(req.getPgName());
        pg.setFullAddress(req.getFullAddress());
        pg.setCity(req.getCity());
        pg.setLocality(req.getLocality());
        pg.setPinCode(req.getPinCode());
        pg.setGoogleMapLink(req.getGoogleMapLink());
        pg.setNearbyLandmarks(req.getNearbyLandmarks());
        pg.setCoverImageUrl(req.getCoverImageUrl());
        pg.setGalleryImages(req.getGalleryImages());
        pg.setVirtualTourLink(req.getVirtualTourLink());
        pg.setVideoLink(req.getVideoLink());
        pg.setOccupancyType(req.getOccupancyType());
        pg.setRoomSizeSqFt(req.getRoomSizeSqFt());
        pg.setFurnished(req.getFurnished());
        pg.setAttachedWashroom(req.getAttachedWashroom());
        pg.setBalconyAvailable(req.getBalconyAvailable());
        pg.setAirConditioned(req.getAirConditioned());
        pg.setBedType(req.getBedType());
        pg.setMattressProvided(req.getMattressProvided());
        pg.setStudyTableAvailable(req.getStudyTableAvailable());
        pg.setFoodProvided(req.getFoodProvided());
        pg.setMealTypes(req.getMealTypes());
        pg.setFoodOptions(req.getFoodOptions());
        pg.setCookingAllowed(req.getCookingAllowed());
        pg.setCommonKitchenAccess(req.getCommonKitchenAccess());
        pg.setFridgeAvailable(req.getFridgeAvailable());
        pg.setMicrowaveAvailable(req.getMicrowaveAvailable());
        pg.setWifiAvailable(req.getWifiAvailable());
        pg.setPowerBackupAvailable(req.getPowerBackupAvailable());
        pg.setGeyserAvailable(req.getGeyserAvailable());
        pg.setWashingMachineAvailable(req.getWashingMachineAvailable());
        pg.setHousekeepingFrequency(req.getHousekeepingFrequency());
        pg.setCctvSurveillance(req.getCctvSurveillance());
        pg.setSecurityGuardAvailable(req.getSecurityGuardAvailable());
        pg.setLiftAvailable(req.getLiftAvailable());
        pg.setTwoWheelerParking(req.getTwoWheelerParking());
        pg.setFourWheelerParking(req.getFourWheelerParking());
        pg.setLoungeAvailable(req.getLoungeAvailable());
        pg.setRecreationAreaAvailable(req.getRecreationAreaAvailable());
        pg.setGymAvailable(req.getGymAvailable());
        pg.setRooftopAccess(req.getRooftopAccess());
        pg.setDailyCleaning(req.getDailyCleaning());
        pg.setLaundryService(req.getLaundryService());
        pg.setMaintenanceOnCall(req.getMaintenanceOnCall());
        pg.setWaterPurifierAvailable(req.getWaterPurifierAvailable());
        pg.setDispenserAvailable(req.getDispenserAvailable());
        pg.setEntryExitTimings(req.getEntryExitTimings());
        pg.setVisitorsAllowed(req.getVisitorsAllowed());
        pg.setGuestsOvernightAllowed(req.getGuestsOvernightAllowed());
        pg.setSecurityDepositAmount(req.getSecurityDepositAmount());
        pg.setIdVerificationRequired(req.getIdVerificationRequired());
        pg.setFireSafetyAvailable(req.getFireSafetyAvailable());
        pg.setSmokingAllowed(req.getSmokingAllowed());
        pg.setPetsAllowed(req.getPetsAllowed());
        pg.setAlcoholAllowed(req.getAlcoholAllowed());
        pg.setDepositAmount(req.getDepositAmount());
        pg.setNoticePeriodDays(req.getNoticePeriodDays());
        pg.setLockInPeriodMonths(req.getLockInPeriodMonths());
        pg.setAdditionalChargesInfo(req.getAdditionalChargesInfo());
        pg.setMaintenanceChargesInfo(req.getMaintenanceChargesInfo());
        pg.setOwnerName(req.getOwnerName());
        pg.setContactNumber(req.getContactNumber());
        pg.setWhatsappNumber(req.getWhatsappNumber());
        pg.setEmail(req.getEmail());
        pg.setVisitingHours(req.getVisitingHours());
        pg.setAvailabilityFor(req.getAvailabilityFor());
        pg.setAgreementType(req.getAgreementType());
        pg.setMinimumStayMonths(req.getMinimumStayMonths());
        pg.setNoticePeriodToLeaveDays(req.getNoticePeriodToLeaveDays());
        pg.setRefundPolicy(req.getRefundPolicy());
        pg.setHouseRulesDocumentUrl(req.getHouseRulesDocumentUrl());
        pg.setSpecialOffers(req.getSpecialOffers());
        pg.setEarlyBirdDiscounts(req.getEarlyBirdDiscounts());
        pg.setReferralBonuses(req.getReferralBonuses());
        pg.setImmediatePossession(req.getImmediatePossession());
        pg.setAvailableFromDate(req.getAvailableFromDate());
        pg.setWaitingList(req.getWaitingList());
        pg.setTotalRooms(req.getTotalRooms());
        pg.setAvailableRooms(req.getAvailableRooms());
        return pg;
    }

    // ══════════════════════════════════════════════════════════════════════
    // MAPPER — Entity → Response DTO
    // ══════════════════════════════════════════════════════════════════════
    private PgListingResponse toResponse(PgListing pg) {
        PgListingResponse res = new PgListingResponse();

        res.setId(pg.getId());
        res.setOwnerId(pg.getOwnerId());
        res.setPgName(pg.getPgName());
        res.setFullAddress(pg.getFullAddress());
        res.setCity(pg.getCity());
        res.setLocality(pg.getLocality());
        res.setPinCode(pg.getPinCode());
        res.setGoogleMapLink(pg.getGoogleMapLink());
        res.setNearbyLandmarks(pg.getNearbyLandmarks());
        res.setCoverImageUrl(pg.getCoverImageUrl());
        res.setGalleryImages(pg.getGalleryImages());
        res.setVirtualTourLink(pg.getVirtualTourLink());
        res.setVideoLink(pg.getVideoLink());

        if (pg.getOccupancyType() != null) {
            res.setOccupancyType(pg.getOccupancyType().name());
            res.setOccupancyLabel(pg.getOccupancyType().displayLabel());
        }

        res.setRoomSizeSqFt(pg.getRoomSizeSqFt());
        res.setFurnished(pg.getFurnished());
        res.setAttachedWashroom(pg.getAttachedWashroom());
        res.setBalconyAvailable(pg.getBalconyAvailable());
        res.setAirConditioned(pg.getAirConditioned());

        if (pg.getBedType() != null) {
            res.setBedType(pg.getBedType().name());
            res.setBedTypeLabel(pg.getBedType().displayLabel());
        }

        res.setMattressProvided(pg.getMattressProvided());
        res.setStudyTableAvailable(pg.getStudyTableAvailable());
        res.setFoodProvided(pg.getFoodProvided());
        res.setMealTypes(pg.getMealTypes());
        res.setFoodOptions(pg.getFoodOptions());
        res.setCookingAllowed(pg.getCookingAllowed());
        res.setCommonKitchenAccess(pg.getCommonKitchenAccess());
        res.setFridgeAvailable(pg.getFridgeAvailable());
        res.setMicrowaveAvailable(pg.getMicrowaveAvailable());
        res.setWifiAvailable(pg.getWifiAvailable());
        res.setPowerBackupAvailable(pg.getPowerBackupAvailable());
        res.setGeyserAvailable(pg.getGeyserAvailable());
        res.setWashingMachineAvailable(pg.getWashingMachineAvailable());

        if (pg.getHousekeepingFrequency() != null) {
            res.setHousekeepingFrequency(pg.getHousekeepingFrequency().name());
            res.setHousekeepingLabel(pg.getHousekeepingFrequency().displayLabel());
        }

        res.setCctvSurveillance(pg.getCctvSurveillance());
        res.setSecurityGuardAvailable(pg.getSecurityGuardAvailable());
        res.setLiftAvailable(pg.getLiftAvailable());
        res.setTwoWheelerParking(pg.getTwoWheelerParking());
        res.setFourWheelerParking(pg.getFourWheelerParking());
        res.setLoungeAvailable(pg.getLoungeAvailable());
        res.setRecreationAreaAvailable(pg.getRecreationAreaAvailable());
        res.setGymAvailable(pg.getGymAvailable());
        res.setRooftopAccess(pg.getRooftopAccess());
        res.setDailyCleaning(pg.getDailyCleaning());
        res.setLaundryService(pg.getLaundryService());
        res.setMaintenanceOnCall(pg.getMaintenanceOnCall());
        res.setWaterPurifierAvailable(pg.getWaterPurifierAvailable());
        res.setDispenserAvailable(pg.getDispenserAvailable());
        res.setEntryExitTimings(pg.getEntryExitTimings());
        res.setVisitorsAllowed(pg.getVisitorsAllowed());
        res.setGuestsOvernightAllowed(pg.getGuestsOvernightAllowed());
        res.setSecurityDepositAmount(pg.getSecurityDepositAmount());
        res.setIdVerificationRequired(pg.getIdVerificationRequired());
        res.setFireSafetyAvailable(pg.getFireSafetyAvailable());
        res.setSmokingAllowed(pg.getSmokingAllowed());
        res.setPetsAllowed(pg.getPetsAllowed());
        res.setAlcoholAllowed(pg.getAlcoholAllowed());
        res.setDepositAmount(pg.getDepositAmount());
        res.setNoticePeriodDays(pg.getNoticePeriodDays());
        res.setLockInPeriodMonths(pg.getLockInPeriodMonths());
        res.setAdditionalChargesInfo(pg.getAdditionalChargesInfo());
        res.setMaintenanceChargesInfo(pg.getMaintenanceChargesInfo());
        res.setOwnerName(pg.getOwnerName());
        res.setContactNumber(pg.getContactNumber());
        res.setWhatsappNumber(pg.getWhatsappNumber());
        res.setEmail(pg.getEmail());
        res.setVisitingHours(pg.getVisitingHours());

        if (pg.getAvailabilityFor() != null) {
            res.setAvailabilityFor(pg.getAvailabilityFor().name());
            res.setAvailabilityLabel(pg.getAvailabilityFor().displayLabel());
        }

        if (pg.getAgreementType() != null) {
            res.setAgreementType(pg.getAgreementType().name());
            res.setAgreementLabel(pg.getAgreementType().displayLabel());
        }

        res.setMinimumStayMonths(pg.getMinimumStayMonths());
        res.setNoticePeriodToLeaveDays(pg.getNoticePeriodToLeaveDays());
        res.setRefundPolicy(pg.getRefundPolicy());
        res.setHouseRulesDocumentUrl(pg.getHouseRulesDocumentUrl());
        res.setSpecialOffers(pg.getSpecialOffers());
        res.setEarlyBirdDiscounts(pg.getEarlyBirdDiscounts());
        res.setReferralBonuses(pg.getReferralBonuses());
        res.setImmediatePossession(pg.getImmediatePossession());
        res.setAvailableFromDate(pg.getAvailableFromDate());
        res.setWaitingList(pg.getWaitingList());
        res.setTotalRooms(pg.getTotalRooms());
        res.setAvailableRooms(pg.getAvailableRooms());
        res.setIsActive(pg.getIsActive());
        res.setIsVerified(pg.getIsVerified());
        res.setIsBrandNew(pg.getIsBrandNew());
        res.setIsPartnerVerified(pg.getIsPartnerVerified());
        res.setRating(pg.getRating());
        res.setTotalReviews(pg.getTotalReviews());
        res.setCreatedAt(pg.getCreatedAt());
        res.setUpdatedAt(pg.getUpdatedAt());

        // ── Sharing options → response ─────────────────────────────────────
        List<PgListingResponse.SharingOptionResponse> sharingRes = pg.getSharingOptions()
            .stream()
            .map(opt -> {
                PgListingResponse.SharingOptionResponse o = new PgListingResponse.SharingOptionResponse();
                o.setId(opt.getId());
                o.setSharingType(opt.getSharingType().name());
                o.setLabel(opt.getSharingType().displayLabel());
                o.setPersons(opt.getSharingType().persons());
                o.setPricePerMonth(opt.getPricePerMonth());
                o.setTotalBeds(opt.getTotalBeds());
                o.setAvailableBeds(opt.getAvailableBeds());
                o.setAmenities(opt.getAmenities());
                o.setIsAvailable(opt.getIsAvailable());
                return o;
            })
            .collect(Collectors.toList());

        res.setSharingOptions(sharingRes);

        // ── Lowest price — "₹6,500 Onwards" ─────────────────────────────────
        pg.getSharingOptions().stream()
            .map(PgSharingOption::getPricePerMonth)
            .min(BigDecimal::compareTo)
            .ifPresent(res::setLowestPrice);

        return res;
    }

    @Override
    public Page<PgListingResponse> getAllListings(Pageable pageable) {
        return pgListingRepository
            .findByIsActiveTrue(pageable)
            .map(this::toResponse);
    }

    @Override
    public List<PgListingResponse> getAllListingsAsList() {
        return pgListingRepository
            .findByIsActiveTrue()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}
