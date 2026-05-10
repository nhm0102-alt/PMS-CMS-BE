package com.pms.backend.api;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.pms.backend.api.service.RestEntityService;

abstract class AbstractRestEntityController {
    protected final RestEntityService service;

    protected AbstractRestEntityController(RestEntityService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> list(@RequestParam Map<String, String> params) {
        return service.list(params);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        return service.create(body);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> bulkCreate(@RequestBody List<Map<String, Object>> body) {
        return service.bulkCreate(body);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> deleteMany(@RequestBody(required = false) Map<String, Object> filter) {
        return service.deleteMany(filter);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable String id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return service.update(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @PutMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> restore(@PathVariable String id) {
        return service.restore(id);
    }
}
