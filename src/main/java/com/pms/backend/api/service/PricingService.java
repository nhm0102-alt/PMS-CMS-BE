package com.pms.backend.api.service;

import java.util.Map;

public interface PricingService {
    Map<String, Object> getInventory(String propertyId, String startDate, int days);
    void updateInventory(String propertyId, Map<String, Object> updateRequest);
}
