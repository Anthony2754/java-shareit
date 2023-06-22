package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemController {

    private final ItemService itemService;
    private static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<ItemDto> addItem(@RequestHeader(name = USER_ID) long ownerId,
                                           @RequestBody @Valid ItemDto itemDto) {

        return new ResponseEntity<>(itemService.addItemDto(itemDto, ownerId), HttpStatus.CREATED);
    }

    @PostMapping(path = "/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(@RequestHeader(name = USER_ID) long authorId,
                                                 @PathVariable long itemId,
                                                 @RequestBody @Valid CommentDto commentDto) {

        return ResponseEntity.ok(itemService.addCommentDto(commentDto, authorId, itemId));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<ItemDto> getItemById(@RequestHeader(name = USER_ID) long requesterId,
                                               @PathVariable long id) {

        return ResponseEntity.ok(itemService.getItemDto(id, requesterId));
    }

    @GetMapping
    public ResponseEntity<Collection<ItemDto>> getItemsOwner(@RequestHeader(name = USER_ID) long ownerId) {

        return ResponseEntity.ok(itemService.getOwnerItems(ownerId));
    }

    @GetMapping(path = "/search")
    public ResponseEntity<Collection<ItemDto>> findByNameOrDescription(
            @RequestHeader(name = USER_ID) long ownerId,
            @RequestParam String text,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false) @Positive Integer size) {

        return ResponseEntity.ok(itemService.findByNameOrDescription(ownerId, text, from, size));
    }

    @PatchMapping(path = "/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader(name = USER_ID) long ownerId,
                                              @PathVariable long itemId,
                                              @RequestBody ItemDto itemDto) {

        return ResponseEntity.ok(itemService.updateItemDto(itemDto, itemId, ownerId));
    }
}
