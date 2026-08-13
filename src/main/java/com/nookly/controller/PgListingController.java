package com.nookly.controller;

import org.springframework.beans.factory.annotation.Value;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.nookly.dto.CreatePgListingRequest;
import com.nookly.dto.PgImportResult;
import com.nookly.dto.PgListingResponse;
import com.nookly.entity.OccupancyType;
import com.nookly.entity.SharingType;
import com.nookly.service.PgListingService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Key;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pg-listings")
public class PgListingController {

	@Value("${app.jwt-secret}")
	private String jwtSecret;

	@Autowired
	private PgListingService pgListingService;

//    private Key getSigninKey() {
//        byte[] key = Decoders.BASE64.decode("q4JjknVjndVi5B1u6WqBl+S3rXWk4Hrp12qFZRfTcys=");
//        return Keys.hmacShaKeyFor(key);
//    }

	private Key getSigninKey() {
		byte[] key = Decoders.BASE64.decode(jwtSecret);
		return Keys.hmacShaKeyFor(key);
	}

	private Long getOwnerIdFromToken(jakarta.servlet.http.HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new RuntimeException("No token provided");
		}
		String token = authHeader.substring(7);
		Claims claims = Jwts.parserBuilder().setSigningKey(getSigninKey()).build().parseClaimsJws(token).getBody();
		Object userIdObj = claims.get("userId");
		if (userIdObj instanceof Integer) {
			return ((Integer) userIdObj).longValue();
		}
		return (Long) userIdObj;
	}

	@PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file)
			throws IOException {

		Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap("cloud_name", System.getenv("CLOUDINARY_CLOUD_NAME"),
				"api_key", System.getenv("CLOUDINARY_API_KEY"), "api_secret", System.getenv("CLOUDINARY_API_SECRET")));

		Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", "nookly-pg"));

		String url = (String) uploadResult.get("secure_url");
		return ResponseEntity.ok(Map.of("url", url));
	}

	// ownerId extracted from token — frontend sends no owner ID
	@PostMapping
	public ResponseEntity<PgListingResponse> createListing(@Valid @RequestBody CreatePgListingRequest request,
			jakarta.servlet.http.HttpServletRequest httpRequest) {
		Long ownerId = getOwnerIdFromToken(httpRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(pgListingService.createListing(request, ownerId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<PgListingResponse> getListingById(@PathVariable Long id) {
		return ResponseEntity.ok(pgListingService.getListingById(id));
	}

	// Owner's PGs — ownerId from token
	@GetMapping("/owner")
	public ResponseEntity<List<PgListingResponse>> getMyListings(jakarta.servlet.http.HttpServletRequest request) {
		Long ownerId = getOwnerIdFromToken(request);
		return ResponseEntity.ok(pgListingService.getListingsByOwner(ownerId));
	}

	@GetMapping("/city/{city}")
	public ResponseEntity<Page<PgListingResponse>> getByCity(@PathVariable String city,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
		return ResponseEntity.ok(pgListingService.getListingsByCity(city, pageable));
	}

	@GetMapping("/search")
	public ResponseEntity<Page<PgListingResponse>> searchListings(@RequestParam String city,
			@RequestParam(required = false) OccupancyType occupancyType,
			@RequestParam(required = false) SharingType sharingType,
			@RequestParam(required = false) BigDecimal minPrice, @RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(required = false) Boolean foodProvided, @RequestParam(required = false) Boolean wifiAvailable,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		Pageable pageable = PageRequest.of(page, size);
		return ResponseEntity.ok(pgListingService.filterListings(city, occupancyType, sharingType, minPrice, maxPrice,
				foodProvided, wifiAvailable, pageable));
	}

	@GetMapping("/top-rated/{city}")
	public ResponseEntity<List<PgListingResponse>> getTopRated(@PathVariable String city) {
		return ResponseEntity.ok(pgListingService.getTopRatedByCity(city));
	}

	// ownerId from token
	@PutMapping("/{id}")
	public ResponseEntity<PgListingResponse> updateListing(@PathVariable Long id,
			@Valid @RequestBody CreatePgListingRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
		Long ownerId = getOwnerIdFromToken(httpRequest);
		return ResponseEntity.ok(pgListingService.updateListing(id, request, ownerId));
	}

	// ownerId from token
	@PatchMapping("/{id}/deactivate")
	public ResponseEntity<Map<String, String>> deactivateListing(@PathVariable Long id,
			jakarta.servlet.http.HttpServletRequest httpRequest) {
		Long ownerId = getOwnerIdFromToken(httpRequest);
		pgListingService.deactivateListing(id, ownerId);
		return ResponseEntity.ok(Map.of("message", "Listing deactivated successfully"));
	}

	// ownerId from token
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, String>> deleteListing(@PathVariable Long id,
			jakarta.servlet.http.HttpServletRequest httpRequest) {
		Long ownerId = getOwnerIdFromToken(httpRequest);
		pgListingService.deleteListing(id, ownerId);
		return ResponseEntity.ok(Map.of("message", "Listing deleted successfully"));
	}

	@GetMapping
	public ResponseEntity<Page<PgListingResponse>> getAllListings(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return ResponseEntity.ok(pgListingService.getAllListings(pageable));
	}

	// Endpoint for internal service communication (Management Service)
	// Returns all PG listings as a List (not paginated)
	@GetMapping("/all")
	public ResponseEntity<List<PgListingResponse>> getAllListingsAsList() {
		return ResponseEntity.ok(pgListingService.getAllListingsAsList());
	}

	@PostMapping(value = "/import-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> importFromCsv(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		try {
			String authHeader = request.getHeader("Authorization");
			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				return ResponseEntity.status(401).body(Map.of("error", "No token provided"));
			}
			// Extract ownerId from JWT – adjust to your logic
			Claims claims = Jwts.parserBuilder().setSigningKey(getSigninKey()).build()
					.parseClaimsJws(authHeader.substring(7)).getBody();
			Object userIdObj = claims.get("userId");
			Long ownerId = (userIdObj instanceof Integer) ? ((Integer) userIdObj).longValue() : (Long) userIdObj;

			PgImportResult result = pgListingService.importFromCsv(ownerId, file);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
		}
	}
}