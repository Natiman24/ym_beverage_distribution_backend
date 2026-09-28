package com.YM.Beverage.Distribution.Backend.store.services.Implementations;

import com.YM.Beverage.Distribution.Backend.configs.security.DataScopeService;
import com.YM.Beverage.Distribution.Backend.store.dtos.CreateStoreDTO;
import com.YM.Beverage.Distribution.Backend.store.dtos.UpdateStoreDTO;
import com.YM.Beverage.Distribution.Backend.store.models.Store;
import com.YM.Beverage.Distribution.Backend.store.repositories.StoreRepository;
import com.YM.Beverage.Distribution.Backend.store.services.Interfaces.StoreService;
import com.YM.Beverage.Distribution.Backend.store.specification.StoreSpecification;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.CustomException;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataAlreadyExistsException;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataNotFoundException;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;
    private final DataScopeService dataScopeService;

    @Override
    public ApiResponse createStore(CreateStoreDTO createStoreDTO) {
        if (createStoreDTO.getPhoneNumber() != null && !createStoreDTO.getPhoneNumber().isBlank()) {
            if (!createStoreDTO.getPhoneNumber().matches("^\\+251[0-9]{9}$")) {
                throw new CustomException("Phone number must start with +251 and contain exactly 9 digits after that.", HttpStatus.BAD_REQUEST, "");
            }
        }

        Optional<Store> optionalStoreByName = storeRepository.findByNameIgnoreCase(createStoreDTO.getName());
        if (optionalStoreByName.isPresent()) {
            throw new DataAlreadyExistsException("Store with this name already exists");
        }

        if (createStoreDTO.getPhoneNumber() != null) {
            verifyPhoneNumber(createStoreDTO.getPhoneNumber());
        }

        if (createStoreDTO.getEmail() != null) {
            verifyEmail(createStoreDTO.getEmail());
        }

        Store store = Store.builder()
                .name(createStoreDTO.getName())
                .description(createStoreDTO.getDescription())
                .tinNumber(createStoreDTO.getTinNumber())
                .phoneNumber(createStoreDTO.getPhoneNumber())
                .email(createStoreDTO.getEmail())
                .city(createStoreDTO.getCity())
                .subCity(createStoreDTO.getSubCity())
                .address(createStoreDTO.getAddress())
                .build();

        storeRepository.save(store);

        return new ApiResponse("Store created successfully", HttpStatus.CREATED,
                Map.of("store", store.toSingleResponseDTO()));
    }

    @Override
    public ApiResponse getStores(String searchQuery, Boolean isActive, Integer page, Integer pageSize) {
        Specification<Store> specification = new StoreSpecification(
                searchQuery, isActive, dataScopeService.currentStoreId());
        Sort sort = Sort.by(Sort.Direction.ASC, "name");

        if (page == null || pageSize == null) {
            List<Store> stores = storeRepository.findAll(specification, sort);
            return new ApiResponse("", HttpStatus.OK,
                    Map.of("stores", stores.stream().map(Store::toListResponseDTO).toList()));
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        Page<Store> storesPage = storeRepository.findAll(specification, pageable);

        return new ApiResponse("", HttpStatus.OK,
                Map.of("stores", storesPage.getContent().stream().map(Store::toListResponseDTO).toList(),
                        "pageSize", pageSize,
                        "currentPage", page,
                        "totalPages", storesPage.getTotalPages(),
                        "totalElements", storesPage.getTotalElements()));
    }

    @Override
    public ApiResponse getStoreById(UUID id) {
        dataScopeService.assertCanAccessStore(id);
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Store with this id is not found"));

        return new ApiResponse("", HttpStatus.OK, Map.of("store", store.toSingleResponseDTO()));
    }

    @Override
    public ApiResponse editStore(UUID id, UpdateStoreDTO updateStoreDTO) {
        dataScopeService.assertCanAccessStore(id);
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Store not found"));

        if (updateStoreDTO.getName() != null && !updateStoreDTO.getName().isBlank()) {
            Optional<Store> existingStoreByName = storeRepository.findByNameIgnoreCase(updateStoreDTO.getName());
            if (existingStoreByName.isPresent() && !existingStoreByName.get().getId().equals(store.getId())) {
                throw new DataAlreadyExistsException("Store with this name already exists");
            }
            store.setName(updateStoreDTO.getName());
        }

        store.setDescription(updateStoreDTO.getDescription() != null ? updateStoreDTO.getDescription() : store.getDescription());

        if (updateStoreDTO.getTinNumber() != null) {
            store.setTinNumber(updateStoreDTO.getTinNumber());
        }

        if (updateStoreDTO.getPhoneNumber() != null && !updateStoreDTO.getPhoneNumber().isBlank()) {
            verifyPhoneNumber(updateStoreDTO.getPhoneNumber());
            store.setPhoneNumber(updateStoreDTO.getPhoneNumber());
        }

        if (updateStoreDTO.getEmail() != null) {
            verifyEmail(updateStoreDTO.getEmail());
            store.setEmail(updateStoreDTO.getEmail());
        }

        if (updateStoreDTO.getCity() != null) {
            store.setCity(updateStoreDTO.getCity());
        }

        if (updateStoreDTO.getSubCity() != null) {
            store.setSubCity(updateStoreDTO.getSubCity());
        }

        if (updateStoreDTO.getAddress() != null) {
            store.setAddress(updateStoreDTO.getAddress());
        }

        storeRepository.save(store);

        return new ApiResponse("Store updated successfully", HttpStatus.OK,
                Map.of("store", store.toSingleResponseDTO()));
    }

    @Override
    public ApiResponse changeActivationStatus(UUID id) {
        dataScopeService.requireCompanyUser();
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Store not found"));

        String message;
        if (store.isActive()) {
            store.setActive(false);
            message = "Store deactivated successfully";
        } else {
            store.setActive(true);
            message = "Store activated successfully";
        }

        storeRepository.save(store);

        return new ApiResponse(message, HttpStatus.OK,
                Map.of("store", store.toSingleResponseDTO()));
    }

    @Override
    public ApiResponse deleteStore(UUID id) {
        dataScopeService.requireCompanyUser();
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Store not found"));

        storeRepository.delete(store);

        return new ApiResponse("Store deleted successfully", HttpStatus.OK);
    }

    private void verifyPhoneNumber(String phoneNumber) {
        if (!phoneNumber.matches("^\\+251[0-9]{9}$")) {
            throw new CustomException("Invalid phone number format", HttpStatus.BAD_REQUEST, "invalid_phone_number");
        }
    }

    private void verifyEmail(String email) {
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new CustomException("Invalid email format", HttpStatus.BAD_REQUEST, "invalid_email");
        }
    }
}
