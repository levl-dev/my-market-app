package ru.yandex.practicum.mymarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PaymentWebClientConfig {

    @Bean
    WebClient paymentWebClient(WebClient.Builder builder) {
        return builder.baseUrl("http://localhost:8081").build();
    }
}
