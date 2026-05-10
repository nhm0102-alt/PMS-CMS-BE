package com.pms.backend.product;

import com.pms.backend.api.dto.ProductCreateRequest;
import com.pms.backend.api.dto.ProductResponse;
import com.pms.backend.api.dto.ProductUpdateRequest;
import com.pms.backend.support.ConflictException;
import com.pms.backend.support.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list() {
        return productRepository.findAll().stream().map(ProductService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return toResponse(p);
    }

    @Transactional
    public ProductResponse create(ProductCreateRequest req) {
        if (req.sku() != null && !req.sku().isBlank() && productRepository.existsBySku(req.sku())) {
            throw new ConflictException("SKU already exists");
        }

        Product p = new Product();
        applyCreateOrUpdate(p, req.name(), req.sku(), req.description(), req.price(), req.status());
        return toResponse(productRepository.save(p));
    }

    @Transactional
    public ProductResponse update(long id, ProductUpdateRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (req.sku() != null && !req.sku().isBlank()) {
            String currentSku = p.getSku();
            if (currentSku == null || !currentSku.equals(req.sku())) {
                if (productRepository.existsBySku(req.sku())) {
                    throw new ConflictException("SKU already exists");
                }
            }
        }

        applyCreateOrUpdate(p, req.name(), req.sku(), req.description(), req.price(), req.status());
        return toResponse(productRepository.save(p));
    }

    @Transactional
    public void delete(long id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }

    private static void applyCreateOrUpdate(
            Product p,
            String name,
            String sku,
            String description,
            java.math.BigDecimal price,
            ProductStatus status
    ) {
        p.setName(name);
        p.setSku(sku == null || sku.isBlank() ? null : sku);
        p.setDescription(description);
        p.setPrice(price);
        if (status != null) {
            p.setStatus(status);
        }
    }

    private static ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getSku(),
                p.getDescription(),
                p.getPrice(),
                p.getStatus(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
