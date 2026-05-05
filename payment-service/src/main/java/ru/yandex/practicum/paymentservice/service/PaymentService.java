package ru.yandex.practicum.paymentservice.service;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.paymentservice.model.BalanceResponse;
import ru.yandex.practicum.paymentservice.model.PaymentResponse;

@Service
public class PaymentService {

    private static final long INITIAL_BALANCE = 10_000L;

    private final AtomicLong balance = new AtomicLong(INITIAL_BALANCE);

    public Mono<BalanceResponse> getBalance() {
        return Mono.just(new BalanceResponse(balance.get()));
    }

    public Mono<PaymentResponse> makePayment(long amount) {
        while (true) {
            long current = balance.get();

            if (amount > current) {
                return Mono.just(new PaymentResponse(false, current, "Not enough balance"));
            }

            long updated = current - amount;
            if (balance.compareAndSet(current, updated)) {
                return Mono.just(new PaymentResponse(true, updated, "Payment completed"));
            }
        }
    }
}
