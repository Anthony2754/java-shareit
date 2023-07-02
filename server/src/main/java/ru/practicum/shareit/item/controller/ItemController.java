package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;
    private static final String USER_ID = "X-Sharer-User-Id";


    @PostMapping
    public ResponseEntity<ItemDto> addItem(@RequestHeader(name = USER_ID) Long ownerId,
                                           @RequestBody ItemDto itemDto) {

        return new ResponseEntity<>(itemService.addItemDto(itemDto, ownerId), HttpStatus.CREATED);
    }

    @PostMapping(path = "/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(@RequestHeader(name = USER_ID) Long authorId,
                                                 @PathVariable Long itemId,
                                                 @RequestBody CommentDto commentDto) {

        return ResponseEntity.ok(itemService.addCommentDto(commentDto, authorId, itemId));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<ItemDto> getItemById(@RequestHeader(name = USER_ID) Long requesterId,
                                               @PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemDto(id, requesterId));
    }

    @GetMapping
    public ResponseEntity<Collection<ItemDto>> getItemsOwner(
            @RequestHeader(name = USER_ID) Long ownerId,
            @RequestParam Integer from,
            @RequestParam Integer size) {

        return ResponseEntity.ok(itemService.getOwnerItems(ownerId, from, size));
    }

    @GetMapping(path = "/search")
    public ResponseEntity<Collection<ItemDto>> searchAvailableItem(
            @RequestHeader(name = USER_ID) Long ownerId,
            @RequestParam String text,
            @RequestParam Integer from,
            @RequestParam Integer size) {

        return ResponseEntity.ok(itemService.searchAvailableItems(ownerId, text, from, size));
    }

    @PatchMapping(path = "/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader(name = USER_ID) Long ownerId,
                                              @PathVariable Long itemId,
                                              @RequestBody ItemDto itemDto) {

        return ResponseEntity.ok(itemService.updateItemDto(itemDto, itemId, ownerId));
    }
}
