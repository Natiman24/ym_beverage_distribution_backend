package com.YM.Beverage.Distribution.Backend.product.services;

import com.YM.Beverage.Distribution.Backend.order.repositories.OrderItemRepository;
import com.YM.Beverage.Distribution.Backend.product.dtos.AdjustQuantityDTO;
import com.YM.Beverage.Distribution.Backend.product.dtos.CreateProductDTO;
import com.YM.Beverage.Distribution.Backend.product.dtos.UpdateProductDTO;
import com.YM.Beverage.Distribution.Backend.product.enums.HistoryMode;
import com.YM.Beverage.Distribution.Backend.product.models.*;
import com.YM.Beverage.Distribution.Backend.product.repositories.*;
import com.YM.Beverage.Distribution.Backend.product.specification.ProductSpecification;
import com.YM.Beverage.Distribution.Backend.supplier.models.Supplier;
import com.YM.Beverage.Distribution.Backend.supplier.repositories.SupplierRepository;
import com.YM.Beverage.Distribution.Backend.user.repositories.UserRepository;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.CustomException;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataNotFoundException;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final String SUPER_ADMIN_ROLE = "Super Admin";
    private static final List<HistoryMode> OPERATIONAL_HISTORY_MODES = List.of(
            HistoryMode.RESTOCK,
            HistoryMode.REDUCTION,
            HistoryMode.SALE,
            HistoryMode.PURCHASE,
            HistoryMode.RETURN,
            HistoryMode.QUANTITY_ADJUSTMENT,
            HistoryMode.RESTORE,
            HistoryMode.OTHER
    );

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductUnitRepository unitRepository;
    private final VolumeUnitRepository volumeUnitRepository;
    private final BrandRepository brandRepository;
    private final ProductHistoryRepository productHistoryRepository;
    private final SupplierRepository supplierRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    private void saveHistory(Product product, HistoryMode mode, int quantityChanged, Supplier supplier, String note) {
        ProductHistory history = ProductHistory.builder()
                .product(product)
                .historyMode(mode)
                .quantityChanged(quantityChanged)
                .supplier(supplier)
                .note(note != null ? note : "")
                .build();
        productHistoryRepository.save(history);
    }

    @Transactional
    public ApiResponse createProduct(CreateProductDTO dto) {
        ProductCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new DataNotFoundException("Category not found"));

        ProductUnit unit = unitRepository.findById(dto.getUnitId())
                .orElseThrow(() -> new DataNotFoundException("Unit not found"));

        Brand brand = null;
        if (dto.getBrandId() != null) {
            brand = brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new DataNotFoundException("Brand not found"));
        }

        VolumeUnit volumeUnit = null;
        if (dto.getVolumeUnitId() != null) {
            volumeUnit = volumeUnitRepository.findById(dto.getVolumeUnitId())
                    .orElseThrow(() -> new DataNotFoundException("Volume unit not found"));
        }

        Supplier supplier  = null;
        if(dto.getSupplierId() != null){
            supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new DataNotFoundException("Supplier not found"));
        }

        int initialQuantity = dto.getQuantity() != null ? dto.getQuantity() : 0;

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .brand(brand)
                .category(category)
                .unit(unit)
                .volume(dto.getVolume())
                .volumeUnit(volumeUnit)
                .unitsPerPack(dto.getUnitsPerPack())
                .quantity(initialQuantity)
                .thresholdQuantity(dto.getThresholdQuantity() != null ? dto.getThresholdQuantity() : 10)
                .sellingPrice(dto.getSellingPrice())
                .purchasePrice(dto.getPurchasePrice())
                .build();

        productRepository.save(product);
        saveHistory(product, HistoryMode.CREATION, initialQuantity, supplier, "Product created");

        return new ApiResponse("Product created successfully", HttpStatus.CREATED,
                Map.of("product", product.toSingleResponseDTO()));
    }

    public ApiResponse getProducts(String searchQuery, List<UUID> categoryIds, List<UUID> unitIds,
                                   List<UUID> brandIds, Boolean active, Integer page, Integer pageSize) {
        Boolean effectiveActive = isSuperAdmin() ? active : Boolean.TRUE;
        ProductSpecification spec = new ProductSpecification(
                searchQuery, categoryIds, unitIds, brandIds, effectiveActive);
        Sort sort = Sort.by(Sort.Direction.ASC, "name");

        if (page == null || pageSize == null) {
            List<Product> products = productRepository.findAll(spec, sort);
            return new ApiResponse("", HttpStatus.OK,
                    Map.of("products", products.stream().map(Product::toListResponseDTO).toList()));
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        Page<Product> pageResult = productRepository.findAll(spec, pageable);

        return new ApiResponse("", HttpStatus.OK, Map.of(
                "products", pageResult.getContent().stream().map(Product::toListResponseDTO).toList(),
                "pageSize", pageSize,
                "currentPage", page,
                "totalPages", pageResult.getTotalPages(),
                "totalElements", pageResult.getTotalElements()));
    }

    public ApiResponse getProductById(UUID id) {
        Product product = findActiveProduct(id);
        return new ApiResponse("", HttpStatus.OK, Map.of("product", product.toSingleResponseDTO()));
    }

    @Transactional
    public ApiResponse updateProduct(UUID id, UpdateProductDTO dto) {
        Product product = findActiveProduct(id);

        if (dto.getName() != null && !dto.getName().isBlank()) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getVolume() != null) product.setVolume(dto.getVolume());
        if (dto.getUnitsPerPack() != null) product.setUnitsPerPack(dto.getUnitsPerPack());
        if (dto.getThresholdQuantity() != null) product.setThresholdQuantity(dto.getThresholdQuantity());
        if (dto.getSellingPrice() != null) product.setSellingPrice(dto.getSellingPrice());
        if (dto.getPurchasePrice() != null) product.setPurchasePrice(dto.getPurchasePrice());
        if (dto.getBrandId() != null) {
            product.setBrand(brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new DataNotFoundException("Brand not found")));
        }
        if (dto.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new DataNotFoundException("Category not found")));
        }
        if (dto.getUnitId() != null) {
            product.setUnit(unitRepository.findById(dto.getUnitId())
                    .orElseThrow(() -> new DataNotFoundException("Unit not found")));
        }
        if (dto.getVolumeUnitId() != null) {
            product.setVolumeUnit(volumeUnitRepository.findById(dto.getVolumeUnitId())
                    .orElseThrow(() -> new DataNotFoundException("Volume unit not found")));
        }

        productRepository.save(product);
        saveHistory(product, HistoryMode.UPDATE, 0, null, "Product updated");
        return new ApiResponse("Product updated successfully", HttpStatus.OK,
                Map.of("product", product.toSingleResponseDTO()));
    }

    @Transactional
    public ApiResponse deleteProduct(UUID id) {
        Product product = findActiveProduct(id);

        boolean usedInOrder = orderItemRepository.existsByProductId(id);
        boolean hasOperationalHistory = productHistoryRepository
                .existsByProductIdAndHistoryModeIn(id, OPERATIONAL_HISTORY_MODES);

        if (!usedInOrder && !hasOperationalHistory) {
            productHistoryRepository.deleteByProductId(id);
            productRepository.delete(product);
            return new ApiResponse("Unused product permanently deleted", HttpStatus.OK);
        }

        product.setActive(false);
        productRepository.save(product);
        saveHistory(product, HistoryMode.DELETION, 0, null, "Product deactivated");
        return new ApiResponse("Product has existing usage and was deactivated", HttpStatus.OK,
                Map.of("product", product.toSingleResponseDTO()));
    }

    @Transactional
    public ApiResponse adjustQuantity(UUID id, AdjustQuantityDTO dto) {
        if( dto.getAmount() == 0) {
            throw new CustomException("Amount must be non-zero", HttpStatus.BAD_REQUEST, "");
        }
        Product product = findActiveProduct(id);

        int newQuantity = product.getQuantity() + dto.getAmount();
        if (newQuantity < 0) {
            throw new CustomException("Insufficient stock. Cannot reduce quantity below zero", HttpStatus.BAD_REQUEST, "");
        }

        Supplier supplier = null;
        if (dto.getSupplierId() != null) {
            supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new DataNotFoundException("Supplier not found"));
        }

        HistoryMode mode = dto.getAmount() >= 0 ? HistoryMode.RESTOCK : HistoryMode.REDUCTION;

        product.setQuantity(newQuantity);
        productRepository.save(product);
        saveHistory(product, mode, dto.getAmount(), supplier, dto.getNote());

        return new ApiResponse("Product quantity adjusted successfully", HttpStatus.OK,
                Map.of("product", product.toSingleResponseDTO()));
    }

    public ApiResponse getProductHistory(UUID id, Integer page, Integer pageSize) {
        if (!productRepository.existsByIdAndActiveTrue(id)) {
            throw new DataNotFoundException("Product not found");
        }

        if (page == null || pageSize == null) {
            List<ProductHistory> history = productHistoryRepository.findByProductIdOrderByCreatedAtDesc(id);
            return new ApiResponse("", HttpStatus.OK,
                    Map.of("history", history.stream().map(ProductHistory::toResponseDTO).toList()));
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<ProductHistory> pageResult = productHistoryRepository.findByProductIdOrderByCreatedAtDesc(id, pageable);

        return new ApiResponse("", HttpStatus.OK, Map.of(
                "history", pageResult.getContent().stream().map(ProductHistory::toResponseDTO).toList(),
                "pageSize", pageSize,
                "currentPage", page,
                "totalPages", pageResult.getTotalPages(),
                "totalElements", pageResult.getTotalElements()));
    }

    private Product findActiveProduct(UUID id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new DataNotFoundException("Active product not found"));
    }

    private boolean isSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return userRepository.findByEmail(authentication.getName())
                .map(user -> user.getRoles().stream()
                        .anyMatch(role -> SUPER_ADMIN_ROLE.equalsIgnoreCase(role.getName())))
                .orElse(false);
    }
}
