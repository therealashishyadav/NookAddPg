package com.nookly.repository;

import com.nookly.entity.PgListing;
import com.nookly.dto.CreatePgListingRequest.SharingOptionRequest;
import com.nookly.entity.OccupancyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
public interface PgListingRepository extends JpaRepository<PgListing, Long> {

    // ── Owner queries ──────────────────────────────────────────────────────
    List<PgListing> findByOwnerIdAndIsActiveTrue(Long ownerId);

    // ── City / locality queries ────────────────────────────────────────────
    Page<PgListing> findByCityIgnoreCaseAndIsActiveTrue(String city, Pageable pageable);

    Page<PgListing> findByCityIgnoreCaseAndLocalityIgnoreCaseAndIsActiveTrue(
        String city, String locality, Pageable pageable);

    // ── Occupancy filter ───────────────────────────────────────────────────
    Page<PgListing> findByCityIgnoreCaseAndOccupancyTypeAndIsActiveTrue(
        String city, OccupancyType occupancyType, Pageable pageable);

    // ── Search by price range across sharing options ───────────────────────
    @Query("SELECT DISTINCT p FROM PgListing p " +
           "JOIN p.sharingOptions s " +
           "WHERE LOWER(p.city) = LOWER(:city) " +
           "AND s.sharingType = :sharingType " +
           "AND s.pricePerMonth BETWEEN :minPrice AND :maxPrice " +
           "AND s.isAvailable = true " +
           "AND p.isActive = true")
    Page<PgListing> searchByPriceRange(
        @Param("city") String city,
        @Param("sharingType") com.nookly.entity.SharingType sharingType,
        @Param("minPrice") java.math.BigDecimal minPrice,
        @Param("maxPrice") java.math.BigDecimal maxPrice,
        Pageable pageable
    );

    // ── Combined filter — city + occupancy + immediate possession ──────────
    @Query("SELECT p FROM PgListing p " +
           "WHERE LOWER(p.city) = LOWER(:city) " +
           "AND (:occupancyType IS NULL OR p.occupancyType = :occupancyType) " +
           "AND (:foodProvided IS NULL OR p.foodProvided = :foodProvided) " +
           "AND (:wifiAvailable IS NULL OR p.wifiAvailable = :wifiAvailable) " +
           "AND p.isActive = true")
    Page<PgListing> filterListings(
        @Param("city") String city,
        @Param("occupancyType") OccupancyType occupancyType,
        @Param("foodProvided") Boolean foodProvided,
        @Param("wifiAvailable") Boolean wifiAvailable,
        Pageable pageable
    );

    // ── Top rated ─────────────────────────────────────────────────────────
    List<PgListing> findTop10ByCityIgnoreCaseAndIsActiveTrueOrderByRatingDesc(String city);

    // ── Verified only ──────────────────────────────────────────────────────
    Page<PgListing> findByCityIgnoreCaseAndIsVerifiedTrueAndIsActiveTrue(
        String city, Pageable pageable);

    // ── Pagination ─────────────────────────────────────────────────────────
    Page<PgListing> findByIsActiveTrue(Pageable pageable);
    
    // ── All listings as List (for internal service communication) ─────────
    List<PgListing> findByIsActiveTrue();
	
}
