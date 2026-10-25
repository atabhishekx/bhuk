package com.restru.order.service;

import com.restru.order.repository.OrderRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates human-readable order numbers, e.g. RSTR-20261025-0001.
 * Seeds the in-memory counter from the database so order numbers remain unique
 * across application restarts. Uniqueness is ultimately enforced by the DB
 * unique constraint on {@code orders.order_number} plus idempotency keys.
 */
@Component
public class OrderNumberGenerator {

    private static final String PREFIX = "RSTR";
    private final OrderRepository orderRepository;
    private final AtomicInteger counter = new AtomicInteger(0);
    private String lastDate = "";

    public OrderNumberGenerator(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public synchronized String generate() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        if (!today.equals(lastDate) || counter.get() == 0) {
            lastDate = today;
            String prefix = PREFIX + "-" + today + "-";
            int maxSeq = orderRepository.findMaxOrderNumberByPrefix(prefix + "%")
                    .map(num -> {
                        try {
                            return Integer.parseInt(num.substring(prefix.length()));
                        } catch (Exception e) {
                            return 0;
                        }
                    })
                    .orElse(0);
            counter.set(maxSeq);
        }
        int seq = counter.incrementAndGet();
        return String.format("%s-%s-%04d", PREFIX, today, seq);
    }
}
