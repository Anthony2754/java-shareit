package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemUpdatedFields;

import java.util.Map;

@Repository
public interface CustomItemRepository {
    Item updateItem(Item item, Map<ItemUpdatedFields, Boolean> targetFields);
}
