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

    BookingDto getBookingDto(long requesterId, long bookingId);

    Collection<BookingDto> getBookingsByBookerIdOrOwnerIdAndStatusSortedByDateDesc(
            Long bookerId, Long ownerId, String state);

    Map<ActualItemBooking, BookingDtoShort> getLastAndNextBookingByItem(Item item, long requesterId);

    BookingDto setApproval(long bookingId, boolean approved, long requesterId);

    boolean commentMadeAfterBooking(long bookerId, long itemId);
}
