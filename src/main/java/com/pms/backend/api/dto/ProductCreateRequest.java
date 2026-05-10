package com.pms.backend.api.dto;

import com.pms.backend.product.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotBlank String name,
        String sku,
        String description,
        @PositiveOrZero BigDecimal price,
        ProductStatus status
) {
}
