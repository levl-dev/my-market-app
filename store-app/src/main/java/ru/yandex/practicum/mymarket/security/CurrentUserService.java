package ru.yandex.practicum.mymarket.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.AppUser;
import ru.yandex.practicum.mymarket.repository.AppUserRepository;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final AppUserRepository appUserRepository;

    public Mono<AppUser> currentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .map(Authentication::getName)
                .flatMap(username -> appUserRepository.findByUsername(username))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated")));
    }

    public Mono<Long> currentUserId() {
        return currentUser().map(AppUser::getId);
    }
}
