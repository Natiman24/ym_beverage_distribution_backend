package com.YM.Beverage.Distribution.Backend.product.services;

import com.YM.Beverage.Distribution.Backend.product.dtos.CreateLookupDTO;
import com.YM.Beverage.Distribution.Backend.product.dtos.UpdateLookupDTO;
import com.YM.Beverage.Distribution.Backend.product.models.ProductCategory;
import com.YM.Beverage.Distribution.Backend.product.repositories.ProductCategoryRepository;
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
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    public ApiResponse createCategory(CreateLookupDTO dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new DataAlreadyExistsException("Category with this name already exists");
        }
        ProductCategory category = ProductCategory.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
        categoryRepository.save(category);
        return new ApiResponse("Category created successfully", HttpStatus.CREATED,
                Map.of("category", category.toResponseDTO()));
    }

    public ApiResponse getCategories(String searchQuery, Boolean active, Integer page, Integer pageSize) {
        Specification<ProductCategory> spec = new LookupSpecification<>(searchQuery, active);
        Sort sort = Sort.by(Sort.Direction.ASC, "name");

        if (page == null || pageSize == null) {
            List<ProductCategory> items = categoryRepository.findAll(spec, sort);
            return new ApiResponse("", HttpStatus.OK,
                    Map.of("categories", items.stream().map(ProductCategory::toResponseDTO).toList()));
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        Page<ProductCategory> pageResult = categoryRepository.findAll(spec, pageable);

        return new ApiResponse("", HttpStatus.OK, Map.of(
                "categories", pageResult.getContent().stream().map(ProductCategory::toResponseDTO).toList(),
                "pageSize", pageSize,
                "currentPage", page,
                "totalPages", pageResult.getTotalPages(),
                "totalElements", pageResult.getTotalElements()));
    }

    public ApiResponse getCategoryById(UUID id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Category not found"));
        return new ApiResponse("", HttpStatus.OK, Map.of("category", category.toResponseDTO()));
    }

    public ApiResponse updateCategory(UUID id, UpdateLookupDTO dto) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Category not found"));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            if (!dto.getName().equalsIgnoreCase(category.getName()) &&
                    categoryRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new DataAlreadyExistsException("Category with this name already exists");
            }
            category.setName(dto.getName());
        }
        if (dto.getDescription() != null) category.setDescription(dto.getDescription());
        if (dto.getActive() != null) category.setActive(dto.getActive());

        categoryRepository.save(category);
        return new ApiResponse("Category updated successfully", HttpStatus.OK,
                Map.of("category", category.toResponseDTO()));
    }

    public ApiResponse deleteCategory(UUID id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Category not found"));
        categoryRepository.delete(category);
        return new ApiResponse("Category deleted successfully", HttpStatus.OK);
    }
}
