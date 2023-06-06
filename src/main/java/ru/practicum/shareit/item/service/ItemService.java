package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import java.util.Collection;

public interface ItemService {

    ItemDto addItemDto(ItemDto itemDto, long ownerId);

    ItemDto getItemDto(long id, long requesterId);

    Item getItem(long itemId);

    Collection<ItemDto> getOwnerItems(long ownerId);

    Collection<ItemDto> findByNameOrDescription(long ownerId, String text);

    ItemDto updateItemDto(ItemDto itemDto, long itemId, long ownerId);

    CommentDto addCommentDto(CommentDto commentDto, long authorId, long itemId);
}
