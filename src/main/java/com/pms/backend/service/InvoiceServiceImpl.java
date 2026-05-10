package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.InvoiceService;
import com.pms.backend.model.InvoiceEntity;
import com.pms.backend.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class InvoiceServiceImpl extends AbstractRestEntityServiceImpl<InvoiceEntity> implements InvoiceService {
    public InvoiceServiceImpl(InvoiceRepository repository, ObjectMapper objectMapper) {
        super(repository, objectMapper, InvoiceEntity::new);
    }

    @Override
    protected void normalize(Map<String, Object> invoice) {
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
}
