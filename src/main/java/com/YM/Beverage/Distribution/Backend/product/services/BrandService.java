package com.YM.Beverage.Distribution.Backend.product.services;

import com.YM.Beverage.Distribution.Backend.product.dtos.CreateLookupDTO;
import com.YM.Beverage.Distribution.Backend.product.dtos.UpdateLookupDTO;
import com.YM.Beverage.Distribution.Backend.product.models.Brand;
import com.YM.Beverage.Distribution.Backend.product.repositories.BrandRepository;
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
public class BrandService {

    private final BrandRepository brandRepository;

    public ApiResponse createBrand(CreateLookupDTO dto) {
        if (brandRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new DataAlreadyExistsException("Brand with this name already exists");
        }
        Brand brand = Brand.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
        brandRepository.save(brand);
        return new ApiResponse("Brand created successfully", HttpStatus.CREATED,
                Map.of("brand", brand.toResponseDTO()));
    }

    public ApiResponse getBrands(String searchQuery, Boolean active, Integer page, Integer pageSize) {
        Specification<Brand> spec = new LookupSpecification<>(searchQuery, active);
        Sort sort = Sort.by(Sort.Direction.ASC, "name");

        if (page == null || pageSize == null) {
            List<Brand> items = brandRepository.findAll(spec, sort);
            return new ApiResponse("", HttpStatus.OK,
                    Map.of("brands", items.stream().map(Brand::toResponseDTO).toList()));
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        Page<Brand> pageResult = brandRepository.findAll(spec, pageable);

        return new ApiResponse("", HttpStatus.OK, Map.of(
                "brands", pageResult.getContent().stream().map(Brand::toResponseDTO).toList(),
                "pageSize", pageSize,
                "currentPage", page,
                "totalPages", pageResult.getTotalPages(),
                "totalElements", pageResult.getTotalElements()));
    }

    public ApiResponse getBrandById(UUID id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Brand not found"));
        return new ApiResponse("", HttpStatus.OK, Map.of("brand", brand.toResponseDTO()));
    }

    public ApiResponse updateBrand(UUID id, UpdateLookupDTO dto) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Brand not found"));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            if (!dto.getName().equalsIgnoreCase(brand.getName()) &&
                    brandRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new DataAlreadyExistsException("Brand with this name already exists");
            }
            brand.setName(dto.getName());
        }
        if (dto.getDescription() != null) brand.setDescription(dto.getDescription());
        if (dto.getActive() != null) brand.setActive(dto.getActive());

        brandRepository.save(brand);
        return new ApiResponse("Brand updated successfully", HttpStatus.OK,
                Map.of("brand", brand.toResponseDTO()));
    }

    public ApiResponse deleteBrand(UUID id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Brand not found"));
        brandRepository.delete(brand);
        return new ApiResponse("Brand deleted successfully", HttpStatus.OK);
    }
}
