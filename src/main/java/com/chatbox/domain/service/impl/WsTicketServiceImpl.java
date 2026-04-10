package com.chatbox.domain.service.impl;

import com.chatbox.domain.service.WsTicketService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WsTicketServiceImpl implements WsTicketService {

    private static final Duration TTL = Duration.ofSeconds(45);

    private final Map<String, TicketRecord> store = new ConcurrentHashMap<>();

    @Override
    public IssueResponse issueForUsername(String username) {
        cleanupExpired();

        String ticket = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        long exp = now + TTL.toMillis();

        store.put(ticket, new TicketRecord(username, now, exp, false));
        return new IssueResponse(ticket, (int) TTL.getSeconds());
    }

    @Override
    public ValidationResult validateAndConsume(String ticket) {
        long now = System.currentTimeMillis();
        final ValidationResult[] result = {ValidationResult.invalid("NOT_FOUND")};

        store.compute(ticket, (k, rec) -> {
            if (rec == null) return null;

            if (now > rec.expiresAtEpochMs) {
                result[0] = ValidationResult.invalid("EXPIRED");
                return null;
            }

            if (rec.used) {
                result[0] = ValidationResult.invalid("ALREADY_USED");
                return null;
            }

            result[0] = ValidationResult.valid(rec.username, rec.issuedAtEpochMs);

            return new TicketRecord(
                    rec.username,
                    rec.issuedAtEpochMs,
                    rec.expiresAtEpochMs,
                    true
            );
        });

        return result[0];
    }

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(e -> now > e.getValue().expiresAtEpochMs);
    }

    private record TicketRecord(String username, long issuedAtEpochMs, long expiresAtEpochMs, boolean used) {}
}