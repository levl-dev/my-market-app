package ru.yandex.practicum.paymentservice.service;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.paymentservice.model.BalanceResponse;
import ru.yandex.practicum.paymentservice.model.PaymentResponse;

@Service
public class PaymentService {

    private static final long INITIAL_BALANCE = 10_000L;
    private static final int MAX_RETRIES = 100;

    private final ConcurrentHashMap<String, AtomicLong> balances = new ConcurrentHashMap<>();

    public Mono<BalanceResponse> getBalance(String username) {
        return Mono.fromSupplier(() -> new BalanceResponse(balanceFor(username).get()));
    }

    public Mono<PaymentResponse> makePayment(String username, long amount) {
        return Mono.defer(() -> {
            AtomicLong balance = balanceFor(username);
            if (amount < 0) {
                return Mono.just(new PaymentResponse(false, balance.get(), "Amount must be non-negative"));
            }
            if (amount == 0) {
                return Mono.just(new PaymentResponse(true, balance.get(), "Payment completed"));
            }
            for (int i = 0; i < MAX_RETRIES; i++) {
                long current = balance.get();
                if (amount > current) {
                    return Mono.just(new PaymentResponse(false, current, "Not enough balance"));
                }
                long updated = current - amount;
                if (balance.compareAndSet(current, updated)) {
                    return Mono.just(new PaymentResponse(true, updated, "Payment completed"));
                }
                Thread.onSpinWait();
            }
            return Mono.just(new PaymentResponse(false, balance.get(), "Payment retry limit exceeded"));
        });
    }

    private AtomicLong balanceFor(String username) {
        return balances.computeIfAbsent(username, unused -> new AtomicLong(INITIAL_BALANCE));
    }
}
