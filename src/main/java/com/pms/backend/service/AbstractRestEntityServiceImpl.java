package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.RestEntityService;
import com.pms.backend.model.BaseJsonEntity;
import com.pms.backend.repository.SoftDeleteRepository;
import com.pms.backend.support.NotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractRestEntityServiceImpl<E extends BaseJsonEntity> implements RestEntityService {
    protected final SoftDeleteRepository<E> repository;
    protected final ObjectMapper objectMapper;
    private final Supplier<E> entityFactory;

    protected AbstractRestEntityServiceImpl(SoftDeleteRepository<E> repository, ObjectMapper objectMapper, Supplier<E> entityFactory) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.entityFactory = entityFactory;
    }

    protected void normalize(Map<String, Object> data) {
    }

    @Override
    public List<Map<String, Object>> list(Map<String, String> params) {
        String sortBy = params.get("sort_by");
        Integer limit = parseInt(params.get("limit"));
        Integer skip = parseInt(params.get("skip"));

        Map<String, Object> filter = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : params.entrySet()) {
            String k = e.getKey();
            if ("sort_by".equals(k) || "limit".equals(k) || "skip".equals(k)) {
                continue;
            }
            filter.put(k, e.getValue());
        }

        List<E> records = repository.findByDeletedFalse();
        List<Map<String, Object>> mapped = new ArrayList<>(records.size());
        for (E r : records) {
            Map<String, Object> data = decode(r.getDataJson());
            Map<String, Object> res = toResponse(r, data);
            if (matches(res, filter)) {
                mapped.add(res);
            }
        }

        if (sortBy != null && !sortBy.isBlank()) {
            boolean desc = sortBy.startsWith("-");
            String field = desc ? sortBy.substring(1) : sortBy;
            Comparator<Map<String, Object>> comparator = (a, b) -> compareValues(a.get(field), b.get(field));
            if (desc) {
                comparator = comparator.reversed();
            }
            mapped.sort(comparator);
        }

        int s = skip == null || skip < 0 ? 0 : skip;
        int l = limit == null || limit <= 0 ? mapped.size() : limit;
        int from = Math.min(s, mapped.size());
        int to = Math.min(from + l, mapped.size());
        return mapped.subList(from, to);
    }

    @Override
    public Map<String, Object> create(Map<String, Object> body) {
        Map<String, Object> data = body == null ? new LinkedHashMap<>() : new LinkedHashMap<>(body);
        normalize(data);
        E e = entityFactory.get();
        e.setDeleted(false);
        e.setDataJson(encode(data));
        E saved = repository.save(e);
        return toResponse(saved, data);
    }

    @Override
    public List<Map<String, Object>> bulkCreate(List<Map<String, Object>> body) {
        List<Map<String, Object>> out = new ArrayList<>(body == null ? 0 : body.size());
        if (body == null) {
            return out;
        }
        for (Map<String, Object> item : body) {
            out.add(create(item));
        }
        return out;
    }

    @Override
    public Map<String, Object> deleteMany(Map<String, Object> filter) {
        List<E> records = repository.findByDeletedFalse();
        int count = 0;
        for (E r : records) {
            Map<String, Object> data = decode(r.getDataJson());
            Map<String, Object> res = toResponse(r, data);
            if (matches(res, filter)) {
                r.setDeleted(true);
                repository.save(r);
                count++;
            }
        }
        return Map.of("deleted", count);
    }

    @Override
    public Map<String, Object> get(String id) {
        E r = repository.findByIdAndDeletedFalse(id).orElseThrow(() -> new NotFoundException("Not found"));
        return toResponse(r, decode(r.getDataJson()));
    }

    @Override
    public Map<String, Object> update(String id, Map<String, Object> body) {
        E r = repository.findByIdAndDeletedFalse(id).orElseThrow(() -> new NotFoundException("Not found"));
        Map<String, Object> current = decode(r.getDataJson());
        if (body != null) {
            current.putAll(body);
        }
        normalize(current);
        r.setDataJson(encode(current));
        E saved = repository.save(r);
        return toResponse(saved, current);
    }

    @Override
    public void delete(String id) {
        E r = repository.findByIdAndDeletedFalse(id).orElseThrow(() -> new NotFoundException("Not found"));
        r.setDeleted(true);
        repository.save(r);
    }

    @Override
    public Map<String, Object> restore(String id) {
        E r = repository.findById(id).orElseThrow(() -> new NotFoundException("Not found"));
        r.setDeleted(false);
        E saved = repository.save(r);
        return toResponse(saved, decode(saved.getDataJson()));
    }

    private static Integer parseInt(String v) {
        if (v == null || v.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(v);
        } catch (Exception ex) {
            return null;
        }
    }

    private Map<String, Object> decode(String json) {
        try {
            if (json == null || json.isBlank()) {
                return new LinkedHashMap<>();
            }
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    private String encode(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data == null ? Map.of() : data);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private static Map<String, Object> toResponse(BaseJsonEntity r, Map<String, Object> data) {
        Map<String, Object> out = new LinkedHashMap<>(data == null ? Map.of() : data);
        out.put("id", r.getId());
        Instant cd = r.getCreatedDate();
        Instant ud = r.getUpdatedDate();
        out.put("created_date", (cd == null ? Instant.now() : cd).toString());
        out.put("updated_date", (ud == null ? Instant.now() : ud).toString());
        return out;
    }

    private static boolean matches(Map<String, Object> record, Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            return true;
        }
        for (Map.Entry<String, Object> e : filter.entrySet()) {
            Object actual = record.get(e.getKey());
            if (!equalsNormalized(actual, e.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalsNormalized(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a instanceof Number an && b instanceof Number bn) {
            return new BigDecimal(an.toString()).compareTo(new BigDecimal(bn.toString())) == 0;
        }
        if (a instanceof Boolean || b instanceof Boolean) {
            return String.valueOf(a).equalsIgnoreCase(String.valueOf(b));
        }
        return a.toString().equals(b.toString());
    }

    private static int compareValues(Object a, Object b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        Comparable ca = normalizeComparable(a);
        Comparable cb = normalizeComparable(b);
        if (ca == null && cb == null) {
            return 0;
        }
        if (ca == null) {
            return 1;
        }
        if (cb == null) {
            return -1;
        }
        return ca.compareTo(cb);
    }

    private static Comparable normalizeComparable(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return new BigDecimal(n.toString());
        }
        if (v instanceof Boolean b) {
            return b ? 1 : 0;
        }
        return v.toString();
    }
}
