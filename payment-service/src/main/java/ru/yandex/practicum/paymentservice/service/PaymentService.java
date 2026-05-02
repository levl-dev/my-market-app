package ru.yandex.practicum.paymentservice.service;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.paymentservice.model.BalanceResponse;
import ru.yandex.practicum.paymentservice.model.PaymentResponse;

@Service
public class PaymentService {

    private static final long INITIAL_BALANCE = 10_000L;

    private final AtomicLong balance = new AtomicLong(INITIAL_BALANCE);

    public BalanceResponse getBalance() {
        return new BalanceResponse(balance.get());
    }

    public synchronized PaymentResponse makePayment(long amount) {
        long current = balance.get();

        if (amount > current) {
            return new PaymentResponse(false, current, "Not enough balance");
        }

        long updated = balance.addAndGet(-amount);
        return new PaymentResponse(true, updated, "Payment completed");
    }
}
