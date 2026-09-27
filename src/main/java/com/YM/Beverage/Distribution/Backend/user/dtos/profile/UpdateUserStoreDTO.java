package com.YM.Beverage.Distribution.Backend.user.dtos.profile;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserStoreDTO {
    /**
     * The active store to assign. Send null to remove the user's store assignment.
     */
    private UUID storeId;
}
