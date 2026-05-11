package ru.yandex.practicum.mymarket.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("app_users")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    private Long id;

    private String username;

    private String password;

    private boolean enabled;
}
