package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(long ownerId, ItemDto itemDto);

    ItemDto updateItem(long ownerId, long itemId, ItemDto itemDto);

    ItemDto getItemById(long ownerId, long itemId);

    List<ItemDto> getAllItems(long ownerId);

    List<ItemDto> findByNameOrDescription(long ownerId, String text);
}
