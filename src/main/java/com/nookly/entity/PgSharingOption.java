package com.nookly.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "pg_sharing_options",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"pg_listing_id", "sharing_type"})
    }
)
public class PgSharingOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relation ───────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pg_listing_id", nullable = false)
    private PgListing pgListing;

    // ── Sharing Type & Price ───────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "sharing_type", nullable = false)
    private SharingType sharingType;            // ONE_SHARING, TWO_SHARING, etc.

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerMonth;           // ₹8,500 for TWO_SHARING

    // ── Bed Info ───────────────────────────────────────────────────────────
    @Column(nullable = false)
    private Integer totalBeds;                  // total beds in this category
    private Integer availableBeds;              // decrements on booking

    // ── Amenities specific to this sharing type ────────────────────────────
    @ElementCollection
    @CollectionTable(
        name = "sharing_option_amenities",
        joinColumns = @JoinColumn(name = "sharing_option_id")
    )
    @Column(name = "amenity")
    private List<String> amenities = new ArrayList<>();  // ["AC", "WiFi", "Attached Bathroom"]

    // ── Status ─────────────────────────────────────────────────────────────
    private Boolean isAvailable = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // ── Getters & Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PgListing getPgListing() { return pgListing; }
    public void setPgListing(PgListing pgListing) { this.pgListing = pgListing; }

    public SharingType getSharingType() { return sharingType; }
    public void setSharingType(SharingType sharingType) { this.sharingType = sharingType; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
}
