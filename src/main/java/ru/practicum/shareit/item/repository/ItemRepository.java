package ru.practicum.shareit.item.repository;


import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    Item addItem(Item item);

    Item updateItemName(Item item);

    Item updateItemDescription(Item item);

    Item updateItemAvailable(Item item);

    Item getItemById(long itemId);

    List<Item> getAllItems(long ownerId);

    List<Item> findByNameOrDescription(String text);
}
