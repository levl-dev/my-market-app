package ru.yandex.practicum.paymentservice.service;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import ru.yandex.practicum.paymentservice.model.BalanceResponse;
import ru.yandex.practicum.paymentservice.model.PaymentResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentServiceTest {

    private final PaymentService paymentService = new PaymentService();

    @Test
    void balancesAreUserSpecific() {
        PaymentResponse payment = paymentService.makePayment("user1", 2_500L).block();
        BalanceResponse user1Balance = paymentService.getBalance("user1").block();
        BalanceResponse user2Balance = paymentService.getBalance("user2").block();

        assertThat(payment.getSuccess()).isTrue();
        assertThat(payment.getBalance()).isEqualTo(7_500L);
        assertThat(user1Balance.getBalance()).isEqualTo(7_500L);
        assertThat(user2Balance.getBalance()).isEqualTo(10_000L);
    }

    @Test
    void negativeAmountReturnsFailureAndDoesNotIncreaseBalance() {
        PaymentResponse payment = paymentService.makePayment("user", -1L).block();
        BalanceResponse balance = paymentService.getBalance("user").block();

        assertThat(payment.getSuccess()).isFalse();
        assertThat(payment.getMessage()).isEqualTo("Amount must be non-negative");
        assertThat(payment.getBalance()).isEqualTo(10_000L);
        assertThat(balance.getBalance()).isEqualTo(10_000L);
    }

    @Test
    void tooLargeAmountReturnsFailure() {
        PaymentResponse payment = paymentService.makePayment("user", 20_000L).block();

        assertThat(payment.getSuccess()).isFalse();
        assertThat(payment.getMessage()).isEqualTo("Not enough balance");
        assertThat(payment.getBalance()).isEqualTo(10_000L);
    }

    @Test
    void drainsBalanceToZeroAndNextPaymentFails() {
        String user = "zero-balance-user";

        PaymentResponse fullWithdraw = paymentService.makePayment(user, 10_000L).block();
        assertThat(fullWithdraw.getSuccess()).isTrue();
        assertThat(fullWithdraw.getBalance()).isEqualTo(0L);

        BalanceResponse afterDrain = paymentService.getBalance(user).block();
        assertThat(afterDrain.getBalance()).isEqualTo(0L);

        PaymentResponse overdraft = paymentService.makePayment(user, 1L).block();
        assertThat(overdraft.getSuccess()).isFalse();
        assertThat(overdraft.getMessage()).isEqualTo("Not enough balance");
        assertThat(overdraft.getBalance()).isEqualTo(0L);

        assertThat(paymentService.getBalance(user).block().getBalance()).isEqualTo(0L);
    }

    @Test
    void concurrentPaymentsOnlyTenOfTwentySucceedWhenBalanceTenThousand() {
        String user = "concurrent-user";

        List<PaymentResponse> results = Flux.range(0, 20)
                .flatMap(i -> paymentService.makePayment(user, 1000L)
                        .subscribeOn(Schedulers.parallel()))
                .collectList()
                .block();

        long successes = results.stream().filter(PaymentResponse::getSuccess).count();
        long failures = results.stream().filter(r -> !r.getSuccess()).count();

        assertThat(successes).isEqualTo(10);
        assertThat(failures).isEqualTo(10);
        assertThat(paymentService.getBalance(user).block().getBalance()).isEqualTo(0L);
    }
}
