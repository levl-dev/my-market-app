package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;

public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {

    Flux<Item> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title,
            String description
    );

    @Query("""
            SELECT COUNT(*)
            FROM items
            WHERE (:search = ''
                OR LOWER(title) LIKE CONCAT('%', LOWER(:search), '%')
                OR LOWER(COALESCE(description, '')) LIKE CONCAT('%', LOWER(:search), '%'))
            """)
    Mono<Long> countBySearch(@Param("search") String search);

    @Query("""
            SELECT id, title, description, img_path, price
            FROM items
            WHERE (:search = ''
                OR LOWER(title) LIKE CONCAT('%', LOWER(:search), '%')
                OR LOWER(COALESCE(description, '')) LIKE CONCAT('%', LOWER(:search), '%'))
            ORDER BY id ASC
            LIMIT :limit OFFSET :offset
            """)
    Flux<Item> findPageOrderById(
            @Param("search") String search,
            @Param("limit") int limit,
            @Param("offset") long offset
    );

    @Query("""
            SELECT id, title, description, img_path, price
            FROM items
            WHERE (:search = ''
                OR LOWER(title) LIKE CONCAT('%', LOWER(:search), '%')
                OR LOWER(COALESCE(description, '')) LIKE CONCAT('%', LOWER(:search), '%'))
            ORDER BY LOWER(title) ASC, id ASC
            LIMIT :limit OFFSET :offset
            """)
    Flux<Item> findPageOrderByTitle(
            @Param("search") String search,
            @Param("limit") int limit,
            @Param("offset") long offset
    );

    @Query("""
            SELECT id, title, description, img_path, price
            FROM items
            WHERE (:search = ''
                OR LOWER(title) LIKE CONCAT('%', LOWER(:search), '%')
                OR LOWER(COALESCE(description, '')) LIKE CONCAT('%', LOWER(:search), '%'))
            ORDER BY price ASC, id ASC
            LIMIT :limit OFFSET :offset
            """)
    Flux<Item> findPageOrderByPrice(
            @Param("search") String search,
            @Param("limit") int limit,
            @Param("offset") long offset
    );
}
