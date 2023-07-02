package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.Collection;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;
    private static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<ItemRequestDto> addItemRequest(@RequestHeader(name = USER_ID) Long requesterId,
                                                         @RequestBody ItemRequestDto requestDto) {
        return ResponseEntity.ok(itemRequestService.addRequest(requestDto, requesterId));
    }

    @GetMapping(path = "/{requestId}")
    public ResponseEntity<ItemRequestDto> getItemRequest(@RequestHeader(name = USER_ID) Long requesterId,
                                                         @PathVariable Long requestId) {
        return ResponseEntity.ok(itemRequestService.getRequestDto(requestId, requesterId));
    }

    @GetMapping
    public ResponseEntity<Collection<ItemRequestDto>> getOwnItemRequests(
            @RequestHeader(name = USER_ID) Long requesterId) {

        return ResponseEntity.ok(itemRequestService.getOwnItemRequests(requesterId));
    }

    @GetMapping(path = "/all")
    public ResponseEntity<Collection<ItemRequestDto>> getOtherUsersRequests(
            @RequestHeader(name = USER_ID) Long requesterId,
            @RequestParam Integer from,
            @RequestParam Integer size) {

        return ResponseEntity.ok(itemRequestService.getOtherUsersRequests(requesterId, from, size));
    }
}
