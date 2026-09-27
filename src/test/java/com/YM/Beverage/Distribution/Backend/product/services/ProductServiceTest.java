package com.YM.Beverage.Distribution.Backend.product.services;

import com.YM.Beverage.Distribution.Backend.order.repositories.OrderItemRepository;
import com.YM.Beverage.Distribution.Backend.product.enums.HistoryMode;
import com.YM.Beverage.Distribution.Backend.product.models.Product;
import com.YM.Beverage.Distribution.Backend.product.models.ProductHistory;
import com.YM.Beverage.Distribution.Backend.product.repositories.*;
import com.YM.Beverage.Distribution.Backend.product.specification.ProductSpecification;
import com.YM.Beverage.Distribution.Backend.supplier.repositories.SupplierRepository;
import com.YM.Beverage.Distribution.Backend.user.models.Role;
import com.YM.Beverage.Distribution.Backend.user.models.User;
import com.YM.Beverage.Distribution.Backend.user.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductCategoryRepository categoryRepository;
    @Mock private ProductUnitRepository unitRepository;
    @Mock private VolumeUnitRepository volumeUnitRepository;
    @Mock private BrandRepository brandRepository;
    @Mock private ProductHistoryRepository productHistoryRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private UserRepository userRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, categoryRepository, unitRepository,
                volumeUnitRepository, brandRepository, productHistoryRepository, supplierRepository,
                orderItemRepository, userRepository);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void regularUsersAlwaysListOnlyActiveProducts() {
        when(productRepository.findAll(any(ProductSpecification.class), any(Sort.class)))
                .thenReturn(List.of());

        productService.getProducts(null, null, null, null, false, null, null);

        ArgumentCaptor<ProductSpecification> specification =
                ArgumentCaptor.forClass(ProductSpecification.class);
        verify(productRepository).findAll(specification.capture(), any(Sort.class));
        assertEquals(Boolean.TRUE, ReflectionTestUtils.getField(specification.getValue(), "active"));
    }

    @Test
    void superAdminCanListActiveAndInactiveProductsTogether() {
        String email = "admin@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of()));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(User.builder()
                .email(email)
                .roles(Set.of(Role.builder().name("Super Admin").build()))
                .build()));
        when(productRepository.findAll(any(ProductSpecification.class), any(Sort.class)))
                .thenReturn(List.of());

        productService.getProducts(null, null, null, null, null, null, null);

        ArgumentCaptor<ProductSpecification> specification =
                ArgumentCaptor.forClass(ProductSpecification.class);
        verify(productRepository).findAll(specification.capture(), any(Sort.class));
        assertEquals(null, ReflectionTestUtils.getField(specification.getValue(), "active"));
    }

    @Test
    void unusedProductIsPermanentlyDeleted() {
        Product product = product();
        when(productRepository.findByIdAndActiveTrue(product.getId())).thenReturn(Optional.of(product));
        when(orderItemRepository.existsByProductId(product.getId())).thenReturn(false);
        when(productHistoryRepository.existsByProductIdAndHistoryModeIn(
                any(UUID.class), any())).thenReturn(false);

        productService.deleteProduct(product.getId());

        verify(productHistoryRepository).deleteByProductId(product.getId());
        verify(productRepository).delete(product);
        verify(productRepository, never()).save(product);
    }

    @Test
    void usedProductIsDeactivatedInsteadOfDeleted() {
        Product product = product();
        when(productRepository.findByIdAndActiveTrue(product.getId())).thenReturn(Optional.of(product));
        when(orderItemRepository.existsByProductId(product.getId())).thenReturn(true);

        productService.deleteProduct(product.getId());

        assertFalse(product.getActive());
        verify(productRepository).save(product);
        verify(productRepository, never()).delete(product);

        ArgumentCaptor<ProductHistory> history = ArgumentCaptor.forClass(ProductHistory.class);
        verify(productHistoryRepository).save(history.capture());
        assertEquals(HistoryMode.DELETION, history.getValue().getHistoryMode());
        assertEquals("Product deactivated", history.getValue().getNote());
    }

    private Product product() {
        return Product.builder()
                .id(UUID.randomUUID())
                .name("Water")
                .quantity(10)
                .thresholdQuantity(5)
                .sellingPrice(BigDecimal.TEN)
                .active(true)
                .build();
    }
}
