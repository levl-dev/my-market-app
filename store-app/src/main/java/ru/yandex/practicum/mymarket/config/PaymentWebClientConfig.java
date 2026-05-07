package ru.yandex.practicum.mymarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PaymentWebClientConfig {

    @Value("${payment.base-url}")
    private String paymentBaseUrl;

    @Bean
    WebClient paymentWebClient(WebClient.Builder builder) {
        return builder.baseUrl(paymentBaseUrl).build();
    }
}
