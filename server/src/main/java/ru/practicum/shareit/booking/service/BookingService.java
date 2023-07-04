package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ActualItemBooking;

import java.util.Collection;
import java.util.Map;

public interface BookingService {

    BookingDto addBooking(BookingDtoRequest bookingDtoRequest, long bookerId);

    BookingDto getBookingDto(long bookingId, long requesterId);

    Collection<BookingDto> getBookingsUserAndState(
            Long bookerId, Long ownerId, String state, int startingIndex, Integer collectionSize);

    Map<ActualItemBooking, BookingDtoShort> getLastAndNextBookingByItem(Item item, long requesterId);

    BookingDto setApproval(long bookingId, boolean approved, long requesterId);

    boolean dontMakeBookings(long bookerId, long itemId);
}
