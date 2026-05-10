package com.pms.backend.api.service;

import java.util.List;
import java.util.Map;

public interface RestEntityService {
    List<Map<String, Object>> list(Map<String, String> params);

    Map<String, Object> create(Map<String, Object> body);

    List<Map<String, Object>> bulkCreate(List<Map<String, Object>> body);

    Map<String, Object> deleteMany(Map<String, Object> filter);

    Map<String, Object> get(String id);

    Map<String, Object> update(String id, Map<String, Object> body);

    void delete(String id);

    Map<String, Object> restore(String id);
}
