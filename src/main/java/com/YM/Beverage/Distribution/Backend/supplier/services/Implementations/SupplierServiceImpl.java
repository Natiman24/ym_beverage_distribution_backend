package com.YM.Beverage.Distribution.Backend.supplier.services.Implementations;

import com.YM.Beverage.Distribution.Backend.supplier.dtos.CreateSupplierDTO;
import com.YM.Beverage.Distribution.Backend.supplier.dtos.UpdateSupplierDTO;
import com.YM.Beverage.Distribution.Backend.supplier.models.Supplier;
import com.YM.Beverage.Distribution.Backend.supplier.repositories.SupplierRepository;
import com.YM.Beverage.Distribution.Backend.supplier.services.Interfaces.SupplierService;
import com.YM.Beverage.Distribution.Backend.supplier.specification.SupplierSpecification;
import com.YM.Beverage.Distribution.Backend.user.repositories.UserRepository;
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
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;

    @Override
    public ApiResponse createSupplier(CreateSupplierDTO createSupplierDTO) {
        if(createSupplierDTO.getPhoneNumber() != null && !createSupplierDTO.getPhoneNumber().isBlank()){
            if(!createSupplierDTO.getPhoneNumber().matches("^\\+251[0-9]{9}$")){
                throw new CustomException("Phone number must start with +251 and contain exactly 9 digits after that.", HttpStatus.BAD_REQUEST, "");
            }

            Optional<Supplier> existingSupplier = supplierRepository.findByPhoneNumber(createSupplierDTO.getPhoneNumber());

            if(existingSupplier.isPresent()) {
                throw new DataAlreadyExistsException("Supplier by this phone number already exists");
            }
        }

        Optional<Supplier> existingSupplier = supplierRepository.findByNameIgnoreCase(createSupplierDTO.getName());

        if(existingSupplier.isPresent()) {
            throw new DataAlreadyExistsException("Supplier by this name already exists");
        }

        Supplier supplier = Supplier.builder()
                .name(createSupplierDTO.getName())
                .description(createSupplierDTO.getDescription())
                .phoneNumber(createSupplierDTO.getPhoneNumber())
                .build();

        supplierRepository.save(supplier);

        return new ApiResponse(" Supplier created successfully", HttpStatus.CREATED,
                        Map.of("supplier", supplier.toSingleResponseDTO()));
        }

    @Override
    public ApiResponse getSuppliers(Integer page, Integer pageSize, String searchQuery) {
        Specification<Supplier> specification = new SupplierSpecification(searchQuery);
        Sort sort = Sort.by(Sort.Direction.ASC, "name");

        if (page == null || pageSize == null) {
            List<Supplier> suppliers = supplierRepository.findAll(specification, sort);
            return new ApiResponse("", HttpStatus.OK,
                    Map.of("suppliers", suppliers.stream().map(Supplier::toListResponseDTO).toList()));
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        Page<Supplier> suppliersPage = supplierRepository.findAll(specification, pageable);

        return new ApiResponse("", HttpStatus.OK,
                Map.of("suppliers", suppliersPage.getContent().stream().map(Supplier::toListResponseDTO).toList(),
                        "pageSize", pageSize,
                        "currentPage", page,
                        "totalPages", suppliersPage.getTotalPages(),
                        "totalElements", suppliersPage.getTotalElements()));
    }

    @Override
    public ApiResponse getSupplierById(UUID id) {

        Supplier supplier = supplierRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException("Supplier not found")
        );

        return new ApiResponse("",HttpStatus.OK,Map.of("supplier",supplier.toSingleResponseDTO()));
    }

    @Override
    public ApiResponse editSupplier(UUID id, UpdateSupplierDTO updateSupplierDTO) {
        Supplier supplier = supplierRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException("Supplier not found")
        );

        if(updateSupplierDTO.getPhoneNumber() != null && !updateSupplierDTO.getPhoneNumber().isBlank()){
            if(!updateSupplierDTO.getPhoneNumber().matches("^\\+251[0-9]{9}$")){
                throw new CustomException("Phone number must start with +251 and contain exactly 9 digits after that.", HttpStatus.BAD_REQUEST, "");
            }

            Optional<Supplier> existingSupplierByPhone = supplierRepository.findByPhoneNumber(updateSupplierDTO.getPhoneNumber());

            if (existingSupplierByPhone.isPresent()
                    && !existingSupplierByPhone.get().getId().equals(supplier.getId())) {
                throw new DataAlreadyExistsException("Supplier by this phone number already exists");
            }

            supplier.setPhoneNumber(updateSupplierDTO.getPhoneNumber());
        }

        if(updateSupplierDTO.getName() != null && !updateSupplierDTO.getName().isBlank()) {

            Optional<Supplier> existingSupplierByName = supplierRepository.findByNameIgnoreCase(updateSupplierDTO.getName());

            if (existingSupplierByName.isPresent() && !existingSupplierByName.get().getId().equals(supplier.getId())
            ) {
                throw new DataAlreadyExistsException("Supplier by this name already exists");
            }
            supplier.setName(updateSupplierDTO.getName());
        }

        supplier.setDescription(updateSupplierDTO.getDescription() != null ? updateSupplierDTO.getDescription() : supplier.getDescription());

        supplierRepository.save(supplier);



        return new ApiResponse("Supplier updated successfully", HttpStatus.OK,
                Map.of("supplier", supplier.toSingleResponseDTO()));
        }

    @Override
    public ApiResponse deleteSupplier(UUID id) {
        Supplier supplier = supplierRepository.findById(id).orElseThrow(
                        () -> new DataNotFoundException("Supplier not found"));

        supplierRepository.delete(supplier);

        return new ApiResponse("Supplier deleted successfully", HttpStatus.OK);
    }
}
