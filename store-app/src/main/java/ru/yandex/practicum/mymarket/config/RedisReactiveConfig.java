package ru.yandex.practicum.mymarket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import ru.yandex.practicum.mymarket.model.Item;

@Configuration
public class RedisReactiveConfig {

    @Bean
    ReactiveRedisTemplate<String, Item> itemReactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory,
            ObjectMapper objectMapper) {
        StringRedisSerializer stringSerializer = StringRedisSerializer.UTF_8;
        Jackson2JsonRedisSerializer<Item> jsonSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Item.class);

        RedisSerializationContext<String, Item> context = RedisSerializationContext
                .<String, Item>newSerializationContext(stringSerializer)
                .key(stringSerializer)
                .value(jsonSerializer)
                .hashKey(stringSerializer)
                .hashValue(jsonSerializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
