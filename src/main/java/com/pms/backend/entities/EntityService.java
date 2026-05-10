package com.pms.backend.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.support.NotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntityService {
    private static final TypeReference<Map<String, Object>> MAP_REF = new TypeReference<>() {
    };

    private final EntityRecordRepository repository;
    private final ObjectMapper objectMapper;

    public EntityService(EntityRecordRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(String entityName, Map<String, Object> filter, String sortBy, Integer limit, Integer skip) {
        List<EntityRecord> records = repository.findByEntityNameAndDeletedFalse(entityName);
        List<Map<String, Object>> mapped = new ArrayList<>(records.size());
        for (EntityRecord r : records) {
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

    @Transactional(readOnly = true)
    public Map<String, Object> get(String entityName, String id) {
        EntityRecord r = repository.findByEntityNameAndId(entityName, id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new NotFoundException(entityName + " not found"));
        return toResponse(r, decode(r.getDataJson()));
    }

    @Transactional
    public Map<String, Object> create(String entityName, Map<String, Object> data) {
        Map<String, Object> normalized = data == null ? new LinkedHashMap<>() : new LinkedHashMap<>(data);
        if ("Invoice".equals(entityName)) {
            normalizeInvoice(normalized);
        }
        if ("Reservation".equals(entityName)) {
            normalizeReservation(normalized);
        }
        EntityRecord r = new EntityRecord();
        r.setId(UUID.randomUUID().toString());
        r.setEntityName(entityName);
        r.setDeleted(false);
        r.setCreatedBy("system");
        r.setDataJson(encode(normalized));
        EntityRecord saved = repository.save(r);
        return toResponse(saved, normalized);
    }

    @Transactional
    public List<Map<String, Object>> bulkCreate(String entityName, List<Map<String, Object>> items) {
        List<Map<String, Object>> out = new ArrayList<>(items.size());
        for (Map<String, Object> item : items) {
            out.add(create(entityName, item));
        }
        return out;
    }

    @Transactional
    public Map<String, Object> update(String entityName, String id, Map<String, Object> patch) {
        EntityRecord r = repository.findByEntityNameAndId(entityName, id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new NotFoundException(entityName + " not found"));

        Map<String, Object> current = decode(r.getDataJson());
        current.putAll(patch);
        if ("Invoice".equals(entityName)) {
            normalizeInvoice(current);
        }
        if ("Reservation".equals(entityName)) {
            normalizeReservation(current);
        }
        r.setDataJson(encode(current));
        EntityRecord saved = repository.save(r);
        return toResponse(saved, current);
    }

    @Transactional
    public void delete(String entityName, String id) {
        EntityRecord r = repository.findByEntityNameAndId(entityName, id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new NotFoundException(entityName + " not found"));
        r.setDeleted(true);
        repository.save(r);
    }

    @Transactional
    public int deleteMany(String entityName, Map<String, Object> filter) {
        List<EntityRecord> records = repository.findByEntityNameAndDeletedFalse(entityName);
        int count = 0;
        for (EntityRecord r : records) {
            Map<String, Object> data = decode(r.getDataJson());
            Map<String, Object> res = toResponse(r, data);
            if (matches(res, filter)) {
                r.setDeleted(true);
                repository.save(r);
                count++;
            }
        }
        return count;
    }

    @Transactional
    public Map<String, Object> restore(String entityName, String id) {
        EntityRecord r = repository.findByEntityNameAndId(entityName, id)
                .orElseThrow(() -> new NotFoundException(entityName + " not found"));
        r.setDeleted(false);
        EntityRecord saved = repository.save(r);
        return toResponse(saved, decode(saved.getDataJson()));
    }

    private Map<String, Object> toResponse(EntityRecord r, Map<String, Object> data) {
        Map<String, Object> out = new LinkedHashMap<>(data);
        out.put("id", r.getId());
        out.put("created_date", r.getCreatedDate() == null ? Instant.now().toString() : r.getCreatedDate().toString());
        out.put("updated_date", r.getUpdatedDate() == null ? Instant.now().toString() : r.getUpdatedDate().toString());
        if (r.getCreatedBy() != null) {
            out.put("created_by", r.getCreatedBy());
        }
        return out;
    }

    private Map<String, Object> decode(String json) {
        try {
            if (json == null || json.isBlank()) {
                return new LinkedHashMap<>();
            }
            return objectMapper.readValue(json, MAP_REF);
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

    private static void normalizeInvoice(Map<String, Object> invoice) {
        BigDecimal subtotal = decimalOrNull(invoice.get("subtotal"));
        BigDecimal tax = decimalOrNull(invoice.get("tax_amount"));
        BigDecimal discount = decimalOrZero(invoice.get("discount_amount"));
        BigDecimal total = decimalOrNull(invoice.get("total_amount"));
        BigDecimal paid = decimalOrZero(invoice.get("paid_amount"));

        if (total == null) {
            BigDecimal s = subtotal == null ? BigDecimal.ZERO : subtotal;
            BigDecimal t = tax == null ? BigDecimal.ZERO : tax;
            total = s.add(t).subtract(discount);
        }

        String status = invoice.get("status") == null ? null : invoice.get("status").toString();
        BigDecimal balance;
        if ("paid".equals(status)) {
            paid = total;
            balance = BigDecimal.ZERO;
        } else if ("cancelled".equals(status)) {
            balance = BigDecimal.ZERO;
        } else {
            balance = total.subtract(paid);
            if (balance.signum() < 0) {
                balance = BigDecimal.ZERO;
            }
        }

        invoice.put("discount_amount", discount);
        invoice.put("paid_amount", paid);
        invoice.put("total_amount", total);
        invoice.put("balance_due", balance);
    }

    private static BigDecimal decimalOrNull(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal bd) {
            return bd;
        }
        if (v instanceof Number n) {
            return new BigDecimal(n.toString());
        }
        String s = v.toString();
        if (s.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(s);
        } catch (Exception ex) {
            return null;
        }
    }

    private static BigDecimal decimalOrZero(Object v) {
        BigDecimal d = decimalOrNull(v);
        return d == null ? BigDecimal.ZERO : d;
    }

    private static void normalizeReservation(Map<String, Object> reservation) {
        String checkIn = reservation.get("check_in_date") == null ? null : reservation.get("check_in_date").toString();
        String checkOut = reservation.get("check_out_date") == null ? null : reservation.get("check_out_date").toString();
        Object nightsObj = reservation.get("nights");
        Integer nights = null;
        if (nightsObj instanceof Number n) {
            nights = n.intValue();
        } else if (nightsObj != null) {
            try {
                nights = Integer.parseInt(nightsObj.toString());
            } catch (Exception ex) {
                nights = null;
            }
        }
        if ((nights == null || nights <= 0) && checkIn != null && checkOut != null) {
            try {
                java.time.LocalDate ci = java.time.LocalDate.parse(checkIn);
                java.time.LocalDate co = java.time.LocalDate.parse(checkOut);
                long diff = java.time.temporal.ChronoUnit.DAYS.between(ci, co);
                if (diff > 0) {
                    reservation.put("nights", (int) diff);
                }
            } catch (Exception ex) {
            }
        }

        BigDecimal total = decimalOrZero(reservation.get("total_amount"));
        BigDecimal paid = decimalOrZero(reservation.get("paid_amount"));
        BigDecimal deposit = decimalOrZero(reservation.get("deposit_amount"));
        BigDecimal balance = total.subtract(paid).subtract(deposit);
        if (balance.signum() < 0) {
            balance = BigDecimal.ZERO;
        }

        reservation.put("total_amount", total);
        reservation.put("paid_amount", paid);
        reservation.put("deposit_amount", deposit);
        reservation.put("balance_due", balance);
    }
}
