package com.YM.Beverage.Distribution.Backend.product.services;

import com.YM.Beverage.Distribution.Backend.product.dtos.CreateLookupDTO;
import com.YM.Beverage.Distribution.Backend.product.dtos.UpdateLookupDTO;
import com.YM.Beverage.Distribution.Backend.product.models.VolumeUnit;
import com.YM.Beverage.Distribution.Backend.product.repositories.VolumeUnitRepository;
import com.YM.Beverage.Distribution.Backend.product.specification.LookupSpecification;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataAlreadyExistsException;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataNotFoundException;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VolumeUnitService {

    private final VolumeUnitRepository volumeUnitRepository;

    public ApiResponse createVolumeUnit(CreateLookupDTO dto) {
        if (volumeUnitRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new DataAlreadyExistsException("Volume unit with this name already exists");
        }
        VolumeUnit volumeUnit = VolumeUnit.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
        volumeUnitRepository.save(volumeUnit);
        return new ApiResponse("Volume unit created successfully", HttpStatus.CREATED,
                Map.of("volumeUnit", volumeUnit.toResponseDTO()));
    }

    public ApiResponse getVolumeUnits(String searchQuery, Boolean active, Integer page, Integer pageSize) {
        Specification<VolumeUnit> spec = new LookupSpecification<>(searchQuery, active);
        Sort sort = Sort.by(Sort.Direction.ASC, "name");

        if (page == null || pageSize == null) {
            List<VolumeUnit> items = volumeUnitRepository.findAll(spec, sort);
            return new ApiResponse("", HttpStatus.OK,
                    Map.of("volumeUnits", items.stream().map(VolumeUnit::toResponseDTO).toList()));
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        Page<VolumeUnit> pageResult = volumeUnitRepository.findAll(spec, pageable);

        return new ApiResponse("", HttpStatus.OK, Map.of(
                "volumeUnits", pageResult.getContent().stream().map(VolumeUnit::toResponseDTO).toList(),
                "pageSize", pageSize,
                "currentPage", page,
                "totalPages", pageResult.getTotalPages(),
                "totalElements", pageResult.getTotalElements()));
    }

    public ApiResponse getVolumeUnitById(UUID id) {
        VolumeUnit volumeUnit = volumeUnitRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Volume unit not found"));
        return new ApiResponse("", HttpStatus.OK, Map.of("volumeUnit", volumeUnit.toResponseDTO()));
    }

    public ApiResponse updateVolumeUnit(UUID id, UpdateLookupDTO dto) {
        VolumeUnit volumeUnit = volumeUnitRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Volume unit not found"));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            if (!dto.getName().equalsIgnoreCase(volumeUnit.getName()) &&
                    volumeUnitRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new DataAlreadyExistsException("Volume unit with this name already exists");
            }
            volumeUnit.setName(dto.getName());
        }
        if (dto.getDescription() != null) volumeUnit.setDescription(dto.getDescription());
        if (dto.getActive() != null) volumeUnit.setActive(dto.getActive());

        volumeUnitRepository.save(volumeUnit);
        return new ApiResponse("Volume unit updated successfully", HttpStatus.OK,
                Map.of("volumeUnit", volumeUnit.toResponseDTO()));
    }

    public ApiResponse deleteVolumeUnit(UUID id) {
        VolumeUnit volumeUnit = volumeUnitRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Volume unit not found"));
        volumeUnitRepository.delete(volumeUnit);
        return new ApiResponse("Volume unit deleted successfully", HttpStatus.OK);
    }
}
