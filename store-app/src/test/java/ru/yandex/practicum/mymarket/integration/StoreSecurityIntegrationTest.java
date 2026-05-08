package ru.yandex.practicum.mymarket.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.client.PaymentClient;
import ru.yandex.practicum.mymarket.model.AppUser;
import ru.yandex.practicum.mymarket.repository.AppUserRepository;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class StoreSecurityIntegrationTest {
    private static final String USERNAME = "secured-user";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @MockBean
    private PaymentClient paymentClient;

    @BeforeEach
    void setUp() {
        when(paymentClient.getBalance(anyString())).thenReturn(Mono.just(10_000L));
        orderItemRepository.deleteAll()
                .then(orderRepository.deleteAll())
                .then(cartItemRepository.deleteAll())
                .then(appUserRepository.deleteAll())
                .then(itemRepository.deleteAll())
                .then(appUserRepository.save(user(USERNAME, "password")))
                .block();
    }

    @Test
    void anonymousGetItemsIsAllowed() {
        webTestClient.get().uri("/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void anonymousGetCartRedirectsToLogin() {
        webTestClient.get().uri("/cart/items")
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    void anonymousGetOrdersRedirectsToLogin() {
        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    void anonymousPostItemsRedirectsToLogin() {
        webTestClient.post().uri("/items")
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    void authenticatedGetCartIsAllowed() {
        webTestClient.mutateWith(mockUser(USERNAME)).get().uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    private AppUser user(String username, String rawPassword) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        return user;
    }
}
