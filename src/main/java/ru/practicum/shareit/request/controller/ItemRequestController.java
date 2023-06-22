package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    private static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<ItemRequestDto> addItemRequest(@RequestHeader(name = USER_ID) long reqsterId,
                                                         @RequestBody @Valid ItemRequestDto itemRequestDto) {
        return ResponseEntity.ok(itemRequestService.addRequest(itemRequestDto, reqsterId));
    }

    @GetMapping(path = "/{requestId}")
    public ResponseEntity<ItemRequestDto> getItemRequest(@RequestHeader(name = USER_ID) long requesterId,
                                                         @PathVariable long requestId) {
        return ResponseEntity.ok(itemRequestService.getRequestDto(requestId, requesterId));
    }

    @GetMapping
    public ResponseEntity<Collection<ItemRequestDto>> getOwnItemRequests(
            @RequestHeader(name = USER_ID) long requesterId) {
        return ResponseEntity.ok(itemRequestService.getOwnItemRequests(requesterId));
    }

    @GetMapping(path = "/all")
    public ResponseEntity<Collection<ItemRequestDto>> getOtherUsersRequests(
            @RequestHeader(name = USER_ID) long requesterId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false) @PositiveOrZero Integer size) {
        return ResponseEntity.ok(itemRequestService.getOtherUsersRequests(requesterId, from, size));
    }
}
