package com.pms.backend.api.dto;

import com.pms.backend.product.ProductStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        String sku,
        String description,
        BigDecimal price,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
