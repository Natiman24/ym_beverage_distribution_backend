package com.YM.Beverage.Distribution.Backend.product.controllers;

import com.YM.Beverage.Distribution.Backend.product.dtos.CreateLookupDTO;
import com.YM.Beverage.Distribution.Backend.product.dtos.UpdateLookupDTO;
import com.YM.Beverage.Distribution.Backend.product.services.VolumeUnitService;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/volume-units")
@CrossOrigin("*")
@SecurityRequirement(name = "bearerAuth")
public class VolumeUnitController {

    private final VolumeUnitService volumeUnitService;

    @PostMapping
    @PreAuthorize("hasAuthority('LOOKUP_CREATE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> createVolumeUnit(@Valid @RequestBody CreateLookupDTO dto) {
        ApiResponse response = volumeUnitService.createVolumeUnit(dto);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LOOKUP_VIEW')")
    public ResponseEntity<ApiResponse> getVolumeUnits(
            @RequestParam(value = "search-query", required = false) String searchQuery,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page-size", required = false) Integer pageSize) {
        ApiResponse response = volumeUnitService.getVolumeUnits(searchQuery, active, page, pageSize);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_VIEW')")
    public ResponseEntity<ApiResponse> getVolumeUnitById(@PathVariable UUID id) {
        ApiResponse response = volumeUnitService.getVolumeUnitById(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_UPDATE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> updateVolumeUnit(@PathVariable UUID id,
                                                        @Valid @RequestBody UpdateLookupDTO dto) {
        ApiResponse response = volumeUnitService.updateVolumeUnit(id, dto);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_DELETE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> deleteVolumeUnit(@PathVariable UUID id) {
        ApiResponse response = volumeUnitService.deleteVolumeUnit(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }
}
