package com.pms.backend.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.entities.EntityService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/entities/{entityName:^(?!Invoice$).+}")
public class EntitiesController {
    private static final TypeReference<Map<String, Object>> MAP_REF = new TypeReference<>() {
    };

    private final EntityService entityService;
    private final ObjectMapper objectMapper;

    public EntitiesController(EntityService entityService, ObjectMapper objectMapper) {
        this.entityService = entityService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public List<Map<String, Object>> list(
            @PathVariable String entityName,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, name = "sort_by") String sortBy,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer skip
    ) {
        return entityService.list(entityName, parseFilter(q), sortBy, limit, skip);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> create(@PathVariable String entityName, @RequestBody Map<String, Object> body) {
        return entityService.create(entityName, body);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> bulkCreate(@PathVariable String entityName, @RequestBody List<Map<String, Object>> body) {
        return entityService.bulkCreate(entityName, body);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> deleteMany(@PathVariable String entityName, @RequestBody(required = false) Map<String, Object> filter) {
        int deleted = entityService.deleteMany(entityName, filter);
        return Map.of("deleted", deleted);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable String entityName, @PathVariable String id) {
        return entityService.get(entityName, id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> update(@PathVariable String entityName, @PathVariable String id, @RequestBody Map<String, Object> body) {
        return entityService.update(entityName, id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable String entityName, @PathVariable String id) {
        entityService.delete(entityName, id);
    }

    @PutMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> restore(@PathVariable String entityName, @PathVariable String id) {
        return entityService.restore(entityName, id);
    }

    private Map<String, Object> parseFilter(String q) {
        if (q == null || q.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(q, MAP_REF);
        } catch (Exception ex) {
            return Map.of();
        }
    }
}
