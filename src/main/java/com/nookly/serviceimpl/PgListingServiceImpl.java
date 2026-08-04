package com.nookly.serviceimpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.web.multipart.MultipartFile;
import com.nookly.dto.CreatePgListingRequest;
import com.nookly.dto.CreatePgListingRequest.SharingOptionRequest;
import com.nookly.dto.PgImportResult;
import com.nookly.dto.PgImportRow;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
		List<PgSharingOption> options = request.getSharingOptions().stream().map(optReq -> {
			PgSharingOption opt = new PgSharingOption();
			opt.setPgListing(pg);
			opt.setSharingType(optReq.getSharingType());
			opt.setPricePerMonth(optReq.getPricePerMonth());
			opt.setTotalBeds(optReq.getTotalBeds());
			opt.setAvailableBeds(optReq.getTotalBeds()); // all available at start
			opt.setAmenities(optReq.getAmenities() != null ? optReq.getAmenities() : new ArrayList<>());
			opt.setIsAvailable(true);
			return opt;
		}).collect(Collectors.toList());

		pg.setSharingOptions(options);

		// 3. Save — cascades to sharing options automatically
		PgListing saved = pgListingRepository.save(pg);

		return toResponse(saved);
	}

	// ── GET BY ID ──────────────────────────────────────────────────────────
	@Override
	public PgListingResponse getListingById(Long id) {
		PgListing pg = pgListingRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PG listing not found with id: " + id));
		return toResponse(pg);
	}

	// ── GET BY OWNER ───────────────────────────────────────────────────────
	@Override
	public List<PgListingResponse> getListingsByOwner(Long ownerId) {
		return pgListingRepository.findByOwnerIdAndIsActiveTrue(ownerId).stream().map(this::toResponse)
				.collect(Collectors.toList());
	}

	// ── GET BY CITY ────────────────────────────────────────────────────────
	@Override
	public Page<PgListingResponse> getListingsByCity(String city, Pageable pageable) {
		return pgListingRepository.findByCityIgnoreCaseAndIsActiveTrue(city, pageable).map(this::toResponse);
	}

	// ── FILTER ─────────────────────────────────────────────────────────────
	@Override
	public Page<PgListingResponse> filterListings(String city, OccupancyType occupancyType, SharingType sharingType,
			BigDecimal minPrice, BigDecimal maxPrice, Boolean foodProvided, Boolean wifiAvailable, Pageable pageable) {

		// Use price-range query when sharing type + price provided
		if (sharingType != null && minPrice != null && maxPrice != null) {
			return pgListingRepository.searchByPriceRange(city, sharingType, minPrice, maxPrice, pageable)
					.map(this::toResponse);
		}

		// General filter otherwise
		return pgListingRepository.filterListings(city, occupancyType, foodProvided, wifiAvailable, pageable)
				.map(this::toResponse);
	}

	// ── TOP RATED ──────────────────────────────────────────────────────────
	@Override
	public List<PgListingResponse> getTopRatedByCity(String city) {
		return pgListingRepository.findTop10ByCityIgnoreCaseAndIsActiveTrueOrderByRatingDesc(city).stream()
				.map(this::toResponse).collect(Collectors.toList());
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


	    mapRequestToEntity(request, pg);

	    Map<SharingType, PgSharingOption> existingMap = pg.getSharingOptions().stream()
	        .collect(Collectors.toMap(
	            PgSharingOption::getSharingType,
	            Function.identity()
	        ));


	    List<PgSharingOption> processedOptions = new ArrayList<>();

	    for (SharingOptionRequest optReq : request.getSharingOptions()) {
	        SharingType sharingType = optReq.getSharingType();
	        PgSharingOption existing = existingMap.remove(sharingType); // remove from map

	        if (existing != null) {

	            existing.setPricePerMonth(optReq.getPricePerMonth());
	            existing.setTotalBeds(optReq.getTotalBeds());
	            existing.setAvailableBeds(optReq.getTotalBeds());
	            existing.setAmenities(optReq.getAmenities() != null ? optReq.getAmenities() : new ArrayList<>());
	            existing.setIsAvailable(true);

	            processedOptions.add(existing);
	        } else {

	            PgSharingOption newOpt = new PgSharingOption();
	            newOpt.setPgListing(pg);
	            newOpt.setSharingType(sharingType);
	            newOpt.setPricePerMonth(optReq.getPricePerMonth());
	            newOpt.setTotalBeds(optReq.getTotalBeds());
	            newOpt.setAvailableBeds(optReq.getTotalBeds());
	            newOpt.setAmenities(optReq.getAmenities() != null ? optReq.getAmenities() : new ArrayList<>());
	            newOpt.setIsAvailable(true);
	            processedOptions.add(newOpt);
	        }
	    }
	    pg.getSharingOptions().clear();
	    pg.getSharingOptions().addAll(processedOptions);

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
		List<PgListingResponse.SharingOptionResponse> sharingRes = pg.getSharingOptions().stream().map(opt -> {
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
		}).collect(Collectors.toList());

		res.setSharingOptions(sharingRes);

		// ── Lowest price — "₹6,500 Onwards" ─────────────────────────────────
		pg.getSharingOptions().stream().map(PgSharingOption::getPricePerMonth).min(BigDecimal::compareTo)
				.ifPresent(res::setLowestPrice);

		return res;
	}

	@Override
	public Page<PgListingResponse> getAllListings(Pageable pageable) {
		return pgListingRepository.findByIsActiveTrue(pageable).map(this::toResponse);
	}

	@Override
	public List<PgListingResponse> getAllListingsAsList() {
		return pgListingRepository.findByIsActiveTrue().stream().map(this::toResponse).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public PgImportResult importFromCsv(Long ownerId, MultipartFile file) {
	    List<String> errors = new ArrayList<>();
	    int totalProcessed = 0;
	    int successCount = 0;
	    ObjectMapper objectMapper = new ObjectMapper();

	    // ── File validation ──────────────────────────────────────────────
	    if (file == null || file.isEmpty()) {
	        errors.add("File is empty or not provided.");
	        return new PgImportResult(0, 0, 1, errors);
	    }
	    if (file.getSize() > 5 * 1024 * 1024) {
	        errors.add("File size exceeds 5MB limit.");
	        return new PgImportResult(0, 0, 1, errors);
	    }

	    // ── Date formatters ──────────────────────────────────────────────
	    List<DateTimeFormatter> dateFormatters = Arrays.asList(
	        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
	        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
	        DateTimeFormatter.ofPattern("M/d/yyyy")
	    );

	    // ── Read CSV ─────────────────────────────────────────────────────
	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

	        // Read header
	        String headerLine = reader.readLine();
	        if (headerLine == null) {
	            errors.add("File is empty.");
	            return new PgImportResult(0, 0, 1, errors);
	        }
	        String[] headers = headerLine.split(",", -1);
	        Map<String, Integer> colIndex = new HashMap<>();
	        for (int i = 0; i < headers.length; i++) {
	            colIndex.put(headers[i].trim(), i);
	        }

	        String line;
	        int rowNumber = 1;

	        while ((line = reader.readLine()) != null) {
	            rowNumber++;
	            totalProcessed++;
	            if (line.trim().isEmpty()) continue;

	            String[] cols = line.split(",", -1);

	            // Helper to get a value by column name
	            java.util.function.Function<String, String> getVal = fieldName -> {
	                Integer idx = colIndex.get(fieldName);
	                return (idx != null && idx < cols.length) ? cols[idx].trim() : null;
	            };

	            try {
	                // ── Map CSV row to PgImportRow ──────────────────────────────
	                PgImportRow row = new PgImportRow();

	                // All fields – using the getVal function
	                row.setPgName(getVal.apply("pgName"));
	                row.setFullAddress(getVal.apply("fullAddress"));
	                row.setCity(getVal.apply("city"));
	                row.setLocality(getVal.apply("locality"));
	                row.setPinCode(getVal.apply("pinCode"));
	                row.setGoogleMapLink(getVal.apply("googleMapLink"));
	                row.setNearbyLandmarks(getVal.apply("nearbyLandmarks"));
	                row.setCoverImageUrl(getVal.apply("coverImageUrl"));
	                row.setGalleryImages(getVal.apply("galleryImages"));
	                row.setVirtualTourLink(getVal.apply("virtualTourLink"));
	                row.setVideoLink(getVal.apply("videoLink"));
	                row.setOccupancyType(getVal.apply("occupancyType"));
	                row.setRoomSizeSqFt(parseDouble(getVal.apply("roomSizeSqFt")));
	                row.setFurnished(parseBoolean(getVal.apply("furnished")));
	                row.setAttachedWashroom(parseBoolean(getVal.apply("attachedWashroom")));
	                row.setBalconyAvailable(parseBoolean(getVal.apply("balconyAvailable")));
	                row.setAirConditioned(parseBoolean(getVal.apply("airConditioned")));
	                row.setBedType(getVal.apply("bedType"));
	                row.setMattressProvided(parseBoolean(getVal.apply("mattressProvided")));
	                row.setStudyTableAvailable(parseBoolean(getVal.apply("studyTableAvailable")));
	                row.setFoodProvided(parseBoolean(getVal.apply("foodProvided")));
	                row.setMealTypes(getVal.apply("mealTypes"));
	                row.setFoodOptions(getVal.apply("foodOptions"));
	                row.setCookingAllowed(parseBoolean(getVal.apply("cookingAllowed")));
	                row.setCommonKitchenAccess(parseBoolean(getVal.apply("commonKitchenAccess")));
	                row.setFridgeAvailable(parseBoolean(getVal.apply("fridgeAvailable")));
	                row.setMicrowaveAvailable(parseBoolean(getVal.apply("microwaveAvailable")));
	                row.setWifiAvailable(parseBoolean(getVal.apply("wifiAvailable")));
	                row.setPowerBackupAvailable(parseBoolean(getVal.apply("powerBackupAvailable")));
	                row.setGeyserAvailable(parseBoolean(getVal.apply("geyserAvailable")));
	                row.setWashingMachineAvailable(parseBoolean(getVal.apply("washingMachineAvailable")));
	                row.setHousekeepingFrequency(getVal.apply("housekeepingFrequency"));
	                row.setCctvSurveillance(parseBoolean(getVal.apply("cctvSurveillance")));
	                row.setSecurityGuardAvailable(parseBoolean(getVal.apply("securityGuardAvailable")));
	                row.setLiftAvailable(parseBoolean(getVal.apply("liftAvailable")));
	                row.setTwoWheelerParking(parseBoolean(getVal.apply("twoWheelerParking")));
	                row.setFourWheelerParking(parseBoolean(getVal.apply("fourWheelerParking")));
	                row.setLoungeAvailable(parseBoolean(getVal.apply("loungeAvailable")));
	                row.setRecreationAreaAvailable(parseBoolean(getVal.apply("recreationAreaAvailable")));
	                row.setGymAvailable(parseBoolean(getVal.apply("gymAvailable")));
	                row.setRooftopAccess(parseBoolean(getVal.apply("rooftopAccess")));
	                row.setDailyCleaning(parseBoolean(getVal.apply("dailyCleaning")));
	                row.setLaundryService(parseBoolean(getVal.apply("laundryService")));
	                row.setMaintenanceOnCall(parseBoolean(getVal.apply("maintenanceOnCall")));
	                row.setWaterPurifierAvailable(parseBoolean(getVal.apply("waterPurifierAvailable")));
	                row.setDispenserAvailable(parseBoolean(getVal.apply("dispenserAvailable")));
	                row.setEntryExitTimings(getVal.apply("entryExitTimings"));
	                row.setVisitorsAllowed(parseBoolean(getVal.apply("visitorsAllowed")));
	                row.setGuestsOvernightAllowed(parseBoolean(getVal.apply("guestsOvernightAllowed")));
	                row.setSecurityDepositAmount(parseDouble(getVal.apply("securityDepositAmount")));
	                row.setIdVerificationRequired(parseBoolean(getVal.apply("idVerificationRequired")));
	                row.setFireSafetyAvailable(parseBoolean(getVal.apply("fireSafetyAvailable")));
	                row.setSmokingAllowed(parseBoolean(getVal.apply("smokingAllowed")));
	                row.setPetsAllowed(parseBoolean(getVal.apply("petsAllowed")));
	                row.setAlcoholAllowed(parseBoolean(getVal.apply("alcoholAllowed")));
	                row.setDepositAmount(parseDouble(getVal.apply("depositAmount")));
	                row.setNoticePeriodDays(parseInteger(getVal.apply("noticePeriodDays")));
	                row.setLockInPeriodMonths(parseInteger(getVal.apply("lockInPeriodMonths")));
	                row.setAdditionalChargesInfo(getVal.apply("additionalChargesInfo"));
	                row.setMaintenanceChargesInfo(getVal.apply("maintenanceChargesInfo"));
	                row.setOwnerName(getVal.apply("ownerName"));
	                row.setContactNumber(getVal.apply("contactNumber"));
	                row.setWhatsappNumber(getVal.apply("whatsappNumber"));
	                row.setEmail(getVal.apply("email"));
	                row.setVisitingHours(getVal.apply("visitingHours"));
	                row.setAvailabilityFor(getVal.apply("availabilityFor"));
	                row.setAgreementType(getVal.apply("agreementType"));
	                row.setMinimumStayMonths(parseInteger(getVal.apply("minimumStayMonths")));
	                row.setNoticePeriodToLeaveDays(parseInteger(getVal.apply("noticePeriodToLeaveDays")));
	                row.setRefundPolicy(getVal.apply("refundPolicy"));
	                row.setHouseRulesDocumentUrl(getVal.apply("houseRulesDocumentUrl"));
	                row.setSpecialOffers(getVal.apply("specialOffers"));
	                row.setEarlyBirdDiscounts(getVal.apply("earlyBirdDiscounts"));
	                row.setReferralBonuses(getVal.apply("referralBonuses"));
	                row.setImmediatePossession(parseBoolean(getVal.apply("immediatePossession")));
	                String dateStr = getVal.apply("availableFromDate");
	                if (dateStr != null && !dateStr.isEmpty()) {
	                    LocalDate parsed = null;
	                    for (DateTimeFormatter fmt : dateFormatters) {
	                        try { parsed = LocalDate.parse(dateStr, fmt); break; } catch (Exception ignored) {}
	                    }
	                    row.setAvailableFromDate(parsed != null ? parsed : LocalDate.now());
	                } else {
	                    row.setAvailableFromDate(LocalDate.now());
	                }
	                row.setWaitingList(parseBoolean(getVal.apply("waitingList")));
	                row.setTotalRooms(parseInteger(getVal.apply("totalRooms")));
	                row.setAvailableRooms(parseInteger(getVal.apply("availableRooms")));
	                row.setSharingOptionsJson(getVal.apply("sharingOptionsJson"));

	                // ── Required fields validation ──────────────────────────────
	                if (isEmpty(row.getPgName())) {
	                    errors.add("Row " + rowNumber + ": pgName is required.");
	                    continue;
	                }
	                if (isEmpty(row.getCity())) {
	                    errors.add("Row " + rowNumber + ": city is required.");
	                    continue;
	                }
	                if (isEmpty(row.getOccupancyType())) {
	                    errors.add("Row " + rowNumber + ": occupancyType is required.");
	                    continue;
	                }

	                // Validate occupancyType enum
	                try {
	                    if (row.getOccupancyType() != null)
	                        OccupancyType.valueOf(row.getOccupancyType().toUpperCase());
	                } catch (IllegalArgumentException e) {
	                    errors.add("Row " + rowNumber + ": invalid occupancyType.");
	                    continue;
	                }

	                // ── Build PgListing entity ──────────────────────────────────
	                PgListing pg = new PgListing();
	                pg.setOwnerId(ownerId);
	                pg.setPgName(row.getPgName());
	                pg.setFullAddress(row.getFullAddress());
	                pg.setCity(row.getCity());
	                pg.setLocality(row.getLocality());
	                pg.setPinCode(row.getPinCode());
	                pg.setGoogleMapLink(row.getGoogleMapLink());
	                pg.setNearbyLandmarks(row.getNearbyLandmarks());
	                pg.setCoverImageUrl(row.getCoverImageUrl());
	                if (row.getGalleryImages() != null && !row.getGalleryImages().isEmpty()) {
	                    pg.setGalleryImages(Arrays.asList(row.getGalleryImages().split(",")));
	                }
	                pg.setVirtualTourLink(row.getVirtualTourLink());
	                pg.setVideoLink(row.getVideoLink());
	                pg.setOccupancyType(row.getOccupancyType() != null ? OccupancyType.valueOf(row.getOccupancyType().toUpperCase()) : null);
	                pg.setRoomSizeSqFt(row.getRoomSizeSqFt());
	                pg.setFurnished(row.getFurnished() != null ? row.getFurnished() : false);
	                pg.setAttachedWashroom(row.getAttachedWashroom() != null ? row.getAttachedWashroom() : false);
	                pg.setBalconyAvailable(row.getBalconyAvailable() != null ? row.getBalconyAvailable() : false);
	                pg.setAirConditioned(row.getAirConditioned() != null ? row.getAirConditioned() : false);
	                pg.setBedType(row.getBedType() != null ? BedType.valueOf(row.getBedType().toUpperCase()) : null);
	                pg.setMattressProvided(row.getMattressProvided() != null ? row.getMattressProvided() : false);
	                pg.setStudyTableAvailable(row.getStudyTableAvailable() != null ? row.getStudyTableAvailable() : false);
	                pg.setFoodProvided(row.getFoodProvided() != null ? row.getFoodProvided() : false);
	                pg.setMealTypes(row.getMealTypes());
	                pg.setFoodOptions(row.getFoodOptions());
	                pg.setCookingAllowed(row.getCookingAllowed() != null ? row.getCookingAllowed() : false);
	                pg.setCommonKitchenAccess(row.getCommonKitchenAccess() != null ? row.getCommonKitchenAccess() : false);
	                pg.setFridgeAvailable(row.getFridgeAvailable() != null ? row.getFridgeAvailable() : false);
	                pg.setMicrowaveAvailable(row.getMicrowaveAvailable() != null ? row.getMicrowaveAvailable() : false);
	                pg.setWifiAvailable(row.getWifiAvailable() != null ? row.getWifiAvailable() : false);
	                pg.setPowerBackupAvailable(row.getPowerBackupAvailable() != null ? row.getPowerBackupAvailable() : false);
	                pg.setGeyserAvailable(row.getGeyserAvailable() != null ? row.getGeyserAvailable() : false);
	                pg.setWashingMachineAvailable(row.getWashingMachineAvailable() != null ? row.getWashingMachineAvailable() : false);
	                pg.setHousekeepingFrequency(row.getHousekeepingFrequency() != null ? HousekeepingFrequency.valueOf(row.getHousekeepingFrequency().toUpperCase()) : null);
	                pg.setCctvSurveillance(row.getCctvSurveillance() != null ? row.getCctvSurveillance() : false);
	                pg.setSecurityGuardAvailable(row.getSecurityGuardAvailable() != null ? row.getSecurityGuardAvailable() : false);
	                pg.setLiftAvailable(row.getLiftAvailable() != null ? row.getLiftAvailable() : false);
	                pg.setTwoWheelerParking(row.getTwoWheelerParking() != null ? row.getTwoWheelerParking() : false);
	                pg.setFourWheelerParking(row.getFourWheelerParking() != null ? row.getFourWheelerParking() : false);
	                pg.setLoungeAvailable(row.getLoungeAvailable() != null ? row.getLoungeAvailable() : false);
	                pg.setRecreationAreaAvailable(row.getRecreationAreaAvailable() != null ? row.getRecreationAreaAvailable() : false);
	                pg.setGymAvailable(row.getGymAvailable() != null ? row.getGymAvailable() : false);
	                pg.setRooftopAccess(row.getRooftopAccess() != null ? row.getRooftopAccess() : false);
	                pg.setDailyCleaning(row.getDailyCleaning() != null ? row.getDailyCleaning() : false);
	                pg.setLaundryService(row.getLaundryService() != null ? row.getLaundryService() : false);
	                pg.setMaintenanceOnCall(row.getMaintenanceOnCall() != null ? row.getMaintenanceOnCall() : false);
	                pg.setWaterPurifierAvailable(row.getWaterPurifierAvailable() != null ? row.getWaterPurifierAvailable() : false);
	                pg.setDispenserAvailable(row.getDispenserAvailable() != null ? row.getDispenserAvailable() : false);
	                pg.setEntryExitTimings(row.getEntryExitTimings());
	                pg.setVisitorsAllowed(row.getVisitorsAllowed() != null ? row.getVisitorsAllowed() : false);
	                pg.setGuestsOvernightAllowed(row.getGuestsOvernightAllowed() != null ? row.getGuestsOvernightAllowed() : false);
	                pg.setSecurityDepositAmount(row.getSecurityDepositAmount() != null ? row.getSecurityDepositAmount() : 0.0);
	                pg.setIdVerificationRequired(row.getIdVerificationRequired() != null ? row.getIdVerificationRequired() : false);
	                pg.setFireSafetyAvailable(row.getFireSafetyAvailable() != null ? row.getFireSafetyAvailable() : false);
	                pg.setSmokingAllowed(row.getSmokingAllowed() != null ? row.getSmokingAllowed() : false);
	                pg.setPetsAllowed(row.getPetsAllowed() != null ? row.getPetsAllowed() : false);
	                pg.setAlcoholAllowed(row.getAlcoholAllowed() != null ? row.getAlcoholAllowed() : false);
	                pg.setDepositAmount(row.getDepositAmount() != null ? row.getDepositAmount() : 0.0);
	                pg.setNoticePeriodDays(row.getNoticePeriodDays() != null ? row.getNoticePeriodDays() : 0);
	                pg.setLockInPeriodMonths(row.getLockInPeriodMonths() != null ? row.getLockInPeriodMonths() : 0);
	                pg.setAdditionalChargesInfo(row.getAdditionalChargesInfo());
	                pg.setMaintenanceChargesInfo(row.getMaintenanceChargesInfo());
	                pg.setOwnerName(row.getOwnerName());
	                pg.setContactNumber(row.getContactNumber());
	                pg.setWhatsappNumber(row.getWhatsappNumber());
	                pg.setEmail(row.getEmail());
	                pg.setVisitingHours(row.getVisitingHours());
	                pg.setAvailabilityFor(row.getAvailabilityFor() != null ? AvailabilityFor.valueOf(row.getAvailabilityFor().toUpperCase()) : null);
	                pg.setAgreementType(row.getAgreementType() != null ? AgreementType.valueOf(row.getAgreementType().toUpperCase()) : null);
	                pg.setMinimumStayMonths(row.getMinimumStayMonths() != null ? row.getMinimumStayMonths() : 0);
	                pg.setNoticePeriodToLeaveDays(row.getNoticePeriodToLeaveDays() != null ? row.getNoticePeriodToLeaveDays() : 0);
	                pg.setRefundPolicy(row.getRefundPolicy());
	                pg.setHouseRulesDocumentUrl(row.getHouseRulesDocumentUrl());
	                pg.setSpecialOffers(row.getSpecialOffers());
	                pg.setEarlyBirdDiscounts(row.getEarlyBirdDiscounts());
	                pg.setReferralBonuses(row.getReferralBonuses());
	                pg.setImmediatePossession(row.getImmediatePossession() != null ? row.getImmediatePossession() : false);
	                pg.setAvailableFromDate(row.getAvailableFromDate() != null ? row.getAvailableFromDate() : LocalDate.now());
	                pg.setWaitingList(row.getWaitingList() != null ? row.getWaitingList() : false);
	                pg.setTotalRooms(row.getTotalRooms() != null ? row.getTotalRooms() : 0);
	                pg.setAvailableRooms(row.getAvailableRooms() != null ? row.getAvailableRooms() : pg.getTotalRooms());
	                pg.setIsActive(true);
	                pg.setIsVerified(false);
	                pg.setCreatedAt(LocalDateTime.now());

	                // ─── Parse sharing options ──────────────────────────────────
	                if (row.getSharingOptionsJson() != null && !row.getSharingOptionsJson().isEmpty()) {
	                    try {
	                        // Use PgSharingOption (the entity)
	                        List<PgSharingOption> options = objectMapper.readValue(
	                                row.getSharingOptionsJson(),
	                                new TypeReference<List<PgSharingOption>>() {}
	                        );
	                        for (PgSharingOption opt : options) {
	                            opt.setPgListing(pg);
	                        }
	                        pg.setSharingOptions(options);
	                    } catch (Exception e) {
	                        errors.add("Row " + rowNumber + ": invalid sharingOptionsJson: " + e.getMessage());
	                        continue;
	                    }
	                } else {
	                    // Default sharing option
	                    PgSharingOption defaultOpt = new PgSharingOption();
	                    defaultOpt.setSharingType(SharingType.ONE_SHARING);
	                    defaultOpt.setPricePerMonth(BigDecimal.ZERO);
	                    defaultOpt.setTotalBeds(1);
	                    defaultOpt.setAvailableBeds(1);
	                    defaultOpt.setAmenities(new ArrayList<>());
	                    defaultOpt.setPgListing(pg);
	                    pg.setSharingOptions(List.of(defaultOpt));
	                }

	                // ─── Compute lowest price ──────────────────────────────────
	                // Note: you have a "lowestPrice" field in PgListingResponse, not in entity.
	                // The entity doesn't need a lowestPrice field; it's computed on the fly.
	                // So we don't set any entity field; it will be computed in toResponse().

	                pgListingRepository.save(pg);
	                successCount++;

	            } catch (Exception e) {
	                errors.add("Row " + rowNumber + ": " + e.getMessage());
	            }
	        }

	    } catch (Exception e) {
	        errors.add("File processing error: " + e.getMessage());
	    }

	    return new PgImportResult(totalProcessed, successCount, totalProcessed - successCount, errors);
	}

	// ─── Helper methods (keep these in the class) ──────────────────────────

	private Double parseDouble(String val) {
	    if (val == null || val.isEmpty()) return null;
	    try { return Double.parseDouble(val); } catch (NumberFormatException e) { return null; }
	}

	private Integer parseInteger(String val) {
	    if (val == null || val.isEmpty()) return null;
	    try { return Integer.parseInt(val); } catch (NumberFormatException e) { return null; }
	}

	private Boolean parseBoolean(String val) {
	    if (val == null || val.isEmpty()) return null;
	    return "true".equalsIgnoreCase(val) || "1".equals(val);
	}

	private boolean isEmpty(String s) {
	    return s == null || s.trim().isEmpty();
	}
	
	
}
