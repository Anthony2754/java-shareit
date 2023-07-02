package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
@Controller
@RequestMapping(path = "/requests")
@Slf4j
@RequiredArgsConstructor
public class ItemRequestController {

    private final RequestClient requestClient;
    private static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> addItemRequest(
            @RequestHeader(name = USER_ID) Long requesterId,
            @RequestBody @Valid ItemRequestDto requestDto) {
        return requestClient.addItemRequest(requesterId, requestDto);
    }

    @GetMapping(path = "/{requestId}")
    public ResponseEntity<Object> getItemRequest(
            @RequestHeader(name = USER_ID) Long requesterId,
            @PathVariable Long requestId) {
        return requestClient.getItemRequest(requesterId, requestId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnItemRequests(
            @RequestHeader(name = USER_ID) Long requesterId) {
        return requestClient.getOwnItemRequests(requesterId);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<Object> getOtherUsersRequests(
            @RequestHeader(name = USER_ID) Long requesterId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        return requestClient.getOtherUsersRequests(requesterId, from, size);
    }
}
