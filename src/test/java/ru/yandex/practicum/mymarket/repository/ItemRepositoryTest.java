package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mymarket.model.Item;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@ActiveProfiles("test")
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findByTitleOrDescriptionIsCaseInsensitive() {
        itemRepository.save(item("TestItem1", "TestDescription1", 300L)).block();
        itemRepository.save(item("TestItem2", "BLUE", 100L)).block();
        itemRepository.save(item("TestItem3", "TestDescription3", 50L)).block();

        List<Item> found = itemRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("blUe", "blUe")
                .collectList()
                .block();

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getTitle()).isEqualTo("TestItem2");
    }

    @Test
    void findByTitleOrDescriptionFindsBothFields() {
        itemRepository.save(item("TestItem1 key", "x", 100L)).block();
        itemRepository.save(item("TestItem2", "key", 200L)).block();
        itemRepository.save(item("TestItem3", "TestDescription3", 50L)).block();

        List<Item> found = itemRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("key", "key")
                .collectList()
                .block();

        assertThat(found).extracting(Item::getTitle).containsExactlyInAnyOrder("TestItem2", "TestItem1 key");
    }

    private static Item item(String title, String description, long price) {
        Item item = new Item();
        item.setTitle(title);
        item.setDescription(description);
        item.setImgPath("/img.png");
        item.setPrice(price);
        return item;
    }
}
