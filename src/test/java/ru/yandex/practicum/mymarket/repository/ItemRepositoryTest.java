package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mymarket.model.Item;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findByTitleOrDescriptionIsCaseInsensitiveAndRespectsPageableSort() {
        itemRepository.save(item("TestItem1", "TestDescription1", 300L));
        itemRepository.save(item("TestItem2", "BLUE", 100L));
        itemRepository.save(item("TestItem3", "TestDescription3", 50L));

        Page<Item> page = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "blUe",
                "blUe",
                PageRequest.of(0, 10, Sort.by("price").ascending())
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("TestItem2");
    }

    @Test
    void findByTitleOrDescriptionFindsBothFields() {
        itemRepository.save(item("TestItem1 key", "x", 100L));
        itemRepository.save(item("TestItem2", "key", 200L));
        itemRepository.save(item("TestItem3", "TestDescription3", 50L));

        Page<Item> page = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "key",
                "key",
                PageRequest.of(0, 10, Sort.by("price").descending())
        );

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).extracting(Item::getTitle).containsExactly("TestItem2", "TestItem1 key");
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
