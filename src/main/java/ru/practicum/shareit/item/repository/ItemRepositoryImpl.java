package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.shareit.log.Logger.logChanges;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private long id;

    @Override
    public Item addItem(Item item) {
        if (!items.containsKey(item.getId())) {
            generateId();
            item.setId(id);
            items.put(item.getId(), item);
            logChanges("Добавлено", item.toString());
            return item;
        } else {
            throw new DuplicateException(String.format("Item с id %s уже существует", item.getId()));
        }
    }

    @Override
    public Item updateItemName(Item item) {
        long itemId = item.getId();
        checkItem(itemId);
        checkOwner(item.getOwnerId(), itemId);
        items.get(itemId).setName(item.getName());
        Item itemStorage = items.get(itemId);
        logChanges("Обновлено", itemStorage.toString());
        return itemStorage;
    }

    @Override
    public Item updateItemDescription(Item item) {
        long itemId = item.getId();
        checkItem(itemId);
        checkOwner(item.getOwnerId(), itemId);
        items.get(itemId).setDescription(item.getDescription());
        Item itemStorage = items.get(itemId);
        logChanges("Обновлено", itemStorage.toString());
        return itemStorage;
    }

    @Override
    public Item updateItemAvailable(Item item) {
        long itemId = item.getId();
        checkItem(itemId);
        checkOwner(item.getOwnerId(), itemId);
        items.get(itemId).setAvailable(item.getAvailable());
        Item itemStorage = items.get(itemId);
        logChanges("Обновлено", itemStorage.toString());
        return itemStorage;
    }

    @Override
    public Item getItemById(long itemId) {
        checkItem(itemId);
        return items.get(itemId);
    }

    @Override
    public List<Item> getAllItems(long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwnerId() == ownerId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findByNameOrDescription(String text) {
        return items.values().stream()
                .filter(item -> (item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                        && item.getAvailable())
                .collect(Collectors.toList());
    }

    private void checkOwner(long userId, long itemId) {
        long ownerId = items.get(itemId).getOwnerId();
        if (userId != ownerId) {
            throw new NotFoundException(String.format("Пользователь с id %s не может изменить item пользователя с id %s",
                    userId, ownerId));
        }
    }

    private void checkItem(long itemId) {
        if (!items.containsKey(itemId)) {
            throw new NotFoundException(String.format("Item с id %s не найдено", itemId));
        }
    }

    private void generateId() {
        id++;
    }
}

