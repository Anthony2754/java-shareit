package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
@Controller
@RequestMapping(path = "/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {

    private final ItemClient itemClient;
    private static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> addItem(
            @RequestHeader(name = USER_ID) Long ownerId,
            @RequestBody @Valid ItemDto itemDto) {
        return itemClient.addItem(ownerId, itemDto);
    }

    @PostMapping(path = "/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader(name = USER_ID) Long authorId,
            @PathVariable Long itemId,
            @RequestBody @Valid CommentDto commentDto) {
        return itemClient.addComment(authorId, itemId, commentDto);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Object> getItemById(
            @RequestHeader(name = USER_ID) Long requesterId,
            @PathVariable Long id) {
        return itemClient.getItem(requesterId, id);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsOwner(
            @RequestHeader(name = USER_ID) Long ownerId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        return itemClient.getOwnerItem(ownerId, from, size);
    }

    @GetMapping(path = "/search")
    public ResponseEntity<Object> searchAvailableItem(
            @RequestHeader(name = USER_ID) Long ownerId,
            @RequestParam String text,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        return itemClient.searchAvailableItem(ownerId, text, from, size);
    }

    @PatchMapping(path = "/{itemId}")
    public ResponseEntity<Object> updateItem(
            @RequestHeader(name = USER_ID) Long ownerId,
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto) {
        return itemClient.updateItem(ownerId, itemId, itemDto);
    }
}
