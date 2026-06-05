package com.nookly.service;

import com.nookly.dto.CreatePgListingRequest;
import com.nookly.dto.PgListingResponse;
import com.nookly.entity.OccupancyType;
import com.nookly.entity.SharingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface PgListingService {

    // ── Create ─────────────────────────────────────────────────────────────
    PgListingResponse createListing(CreatePgListingRequest request, Long ownerId);

    // ── Read ───────────────────────────────────────────────────────────────
    PgListingResponse getListingById(Long id);

    List<PgListingResponse> getListingsByOwner(Long ownerId);

    Page<PgListingResponse> getListingsByCity(String city, Pageable pageable);

    Page<PgListingResponse> filterListings(
        String city,
        OccupancyType occupancyType,
        SharingType sharingType,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean foodProvided,
        Boolean wifiAvailable,
        Pageable pageable
    );

    List<PgListingResponse> getTopRatedByCity(String city);

    // ── Update ─────────────────────────────────────────────────────────────
    PgListingResponse updateListing(Long id, CreatePgListingRequest request, Long ownerId);

    // ── Delete / Deactivate ────────────────────────────────────────────────
    void deactivateListing(Long id, Long ownerId);

    void deleteListing(Long id, Long ownerId);
    
    Page<PgListingResponse> getAllListings(Pageable pageable);
    
    List<PgListingResponse> getAllListingsAsList();
}
