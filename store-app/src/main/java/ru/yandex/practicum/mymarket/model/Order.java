package ru.yandex.practicum.mymarket.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

@Table("orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("total_sum")
    private long totalSum;

    @Column("created_at")
    private LocalDateTime createdAt;
}