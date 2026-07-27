package com.nookly.repository;

import com.nookly.entity.PgSharingOption;
import com.nookly.entity.SharingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PgSharingOptionRepository extends JpaRepository<PgSharingOption, Long> {

    // ── Get all options for a PG ───────────────────────────────────────────
    List<PgSharingOption> findByPgListingId(Long pgListingId);

    // ── Get available options only ─────────────────────────────────────────
    List<PgSharingOption> findByPgListingIdAndIsAvailableTrue(Long pgListingId);

    // ── Get one specific sharing type for a PG ─────────────────────────────
    Optional<PgSharingOption> findByPgListingIdAndSharingType(Long pgListingId, SharingType sharingType);

    // ── Delete all options for a PG (used when PG is removed) ─────────────
    void deleteByPgListingId(Long pgListingId);
    
    @Modifying
    @Query("DELETE FROM PgSharingOption o WHERE o.pgListing.id = :pgId")
    void deleteAllByPgListingId(@Param("pgId") Long pgId);
}
