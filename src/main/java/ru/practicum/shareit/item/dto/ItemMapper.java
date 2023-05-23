package ru.practicum.shareit.item.dto;


import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.model.Item;

@UtilityClass
public class ItemMapper {
    public static Item toItem(long ownerId, long itemId, ItemDto itemDto) {
        return Item.builder()
                .id(itemId)
                .ownerId(ownerId)
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    public static ItemDto toItemDto(long itemId, Item item) {
        return ItemDto.builder()
                .id(itemId)
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }
}

