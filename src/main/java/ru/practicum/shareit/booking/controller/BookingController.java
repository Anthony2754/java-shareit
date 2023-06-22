package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@Validated
@RestController
@RequestMapping(path = "/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<BookingDto> addBooking(
            @RequestHeader(name = USER_ID) long bookerId,
            @RequestBody @Valid BookingDtoRequest bookingDtoRequest) {

        return new ResponseEntity<>(bookingService.addBooking(bookingDtoRequest, bookerId), HttpStatus.CREATED);
    }

    @GetMapping(path = "/{bookingId}")
    public ResponseEntity<BookingDto> getBooking(
            @RequestHeader(name = USER_ID) long requesterId,
            @PathVariable long bookingId) {
        return ResponseEntity.ok(bookingService.getBookingDto(bookingId, requesterId));
    }

    @GetMapping
    public ResponseEntity<Collection<BookingDto>> getBookingsBookerAndStatus(
            @RequestHeader(name = USER_ID) long bookerId,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false) @Positive Integer size) {

        return ResponseEntity.ok(bookingService.getBookingsUserAndState(
                bookerId, null, state, from, size));
    }

    @GetMapping(path = "/owner")
    public ResponseEntity<Collection<BookingDto>> getBookingsOwnerAndStatus(
            @RequestHeader(name = USER_ID) long ownerId,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false) @Positive Integer size) {

        return ResponseEntity.ok(bookingService.getBookingsUserAndState(
                null, ownerId, state, from, size));
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> setBookingApproval(
            @RequestHeader(name = USER_ID) long requesterId,
            @PathVariable long bookingId,
            @RequestParam boolean approved) {

        return ResponseEntity.ok(bookingService.setApproval(bookingId, approved, requesterId));
    }
}
