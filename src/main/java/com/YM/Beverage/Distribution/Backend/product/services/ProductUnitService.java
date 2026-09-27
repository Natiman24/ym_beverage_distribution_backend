package com.YM.Beverage.Distribution.Backend.product.services;

import com.YM.Beverage.Distribution.Backend.product.dtos.CreateLookupDTO;
import com.YM.Beverage.Distribution.Backend.product.dtos.UpdateLookupDTO;
import com.YM.Beverage.Distribution.Backend.product.models.ProductUnit;
import com.YM.Beverage.Distribution.Backend.product.repositories.ProductUnitRepository;
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
public class ProductUnitService {

    private final ProductUnitRepository unitRepository;

    public ApiResponse createUnit(CreateLookupDTO dto) {
        if (unitRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new DataAlreadyExistsException("Unit with this name already exists");
        }
        ProductUnit unit = ProductUnit.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
        unitRepository.save(unit);
        return new ApiResponse("Unit created successfully", HttpStatus.CREATED,
                Map.of("unit", unit.toResponseDTO()));
    }

    public ApiResponse getUnits(String searchQuery, Boolean active, Integer page, Integer pageSize) {
        Specification<ProductUnit> spec = new LookupSpecification<>(searchQuery, active);
        Sort sort = Sort.by(Sort.Direction.ASC, "name");

        if (page == null || pageSize == null) {
            List<ProductUnit> items = unitRepository.findAll(spec, sort);
            return new ApiResponse("", HttpStatus.OK,
                    Map.of("units", items.stream().map(ProductUnit::toResponseDTO).toList()));
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        Page<ProductUnit> pageResult = unitRepository.findAll(spec, pageable);

        return new ApiResponse("", HttpStatus.OK, Map.of(
                "units", pageResult.getContent().stream().map(ProductUnit::toResponseDTO).toList(),
                "pageSize", pageSize,
                "currentPage", page,
                "totalPages", pageResult.getTotalPages(),
                "totalElements", pageResult.getTotalElements()));
    }

    public ApiResponse getUnitById(UUID id) {
        ProductUnit unit = unitRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Unit not found"));
        return new ApiResponse("", HttpStatus.OK, Map.of("unit", unit.toResponseDTO()));
    }

    public ApiResponse updateUnit(UUID id, UpdateLookupDTO dto) {
        ProductUnit unit = unitRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Unit not found"));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            if (!dto.getName().equalsIgnoreCase(unit.getName()) &&
                    unitRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new DataAlreadyExistsException("Unit with this name already exists");
            }
            unit.setName(dto.getName());
        }
        if (dto.getDescription() != null) unit.setDescription(dto.getDescription());
        if (dto.getActive() != null) unit.setActive(dto.getActive());

        unitRepository.save(unit);
        return new ApiResponse("Unit updated successfully", HttpStatus.OK,
                Map.of("unit", unit.toResponseDTO()));
    }

    public ApiResponse deleteUnit(UUID id) {
        ProductUnit unit = unitRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Unit not found"));
        unitRepository.delete(unit);
        return new ApiResponse("Unit deleted successfully", HttpStatus.OK);
    }
}
