package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<BookingDto> addBooking(
            @RequestHeader(name = USER_ID) Long bookerId,
            @RequestBody BookingDtoRequest bookingDtoRequest) {

        return new ResponseEntity<>(bookingService.addBooking(bookingDtoRequest, bookerId), HttpStatus.CREATED);
    }

    @GetMapping(path = "/{bookingId}")
    public ResponseEntity<BookingDto> getBooking(
            @RequestHeader(name = USER_ID) Long requesterId,
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getBookingDto(bookingId, requesterId));
    }

    @GetMapping
    public ResponseEntity<Collection<BookingDto>> getBookingsBookerAndStatus(
            @RequestHeader(name = USER_ID) Long bookerId,
            @RequestParam String state,
            @RequestParam Integer from,
            @RequestParam Integer size) {

        return ResponseEntity.ok(bookingService.getBookingsUserAndState(
                bookerId, null, state, from, size));
    }

    @GetMapping(path = "/owner")
    public ResponseEntity<Collection<BookingDto>> getBookingsOwnerAndStatus(
            @RequestHeader(name = USER_ID) Long ownerId,
            @RequestParam String state,
            @RequestParam Integer from,
            @RequestParam Integer size) {

        return ResponseEntity.ok(bookingService.getBookingsUserAndState(
                null, ownerId, state, from, size));
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> setBookingApproval(
            @RequestHeader(name = USER_ID) Long requesterId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved) {

        return ResponseEntity.ok(bookingService.setApproval(bookingId, approved, requesterId));
    }
}
