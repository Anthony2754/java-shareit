package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ActualItemBooking;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.dto.BookingStatus.*;
import static ru.practicum.shareit.item.service.ActualItemBooking.LAST;
import static ru.practicum.shareit.item.service.ActualItemBooking.NEXT;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final BookingMapper mapper;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto addBooking(BookingDtoRequest bookingDto, long bookerId) {
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();
        long itemId = bookingDto.getItemId();
        long itemOwnerId;
        Booking booking;
        Optional<Item> itemOptional;
        Item item;

        if (end.isBefore(start) || end.equals(start)) {
            throw new ValidationException(String.format(
                    "Ошибка при добавлении бронирования для вещи с id=%d от пользователя с id=%d: " +
                            "дата окончания бронирования раньше или равна дате начала.",
                    itemId,
                    bookerId
            ));
        }

        itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isEmpty()) {
            throw new NotFoundException(String.format("Ошибка получения: вещь с id=%d не найдена.", itemId));
        }
        item = itemOptional.get();

        booking = mapper.mapToModel(
                bookingDto,
                userService.getUserById(bookerId),
                item);

        item = booking.getItem();
        itemId = item.getId();
        itemOwnerId = item.getOwner().getId();

        if (itemOwnerId != bookerId && itemRepository.existsItemByIdAndAvailableIsTrue(itemId)) {
            if (freeBookingTime(booking)) {
                log.debug("Добавлено новое бронирование: {}", booking);
                return mapper.mapToDto(bookingRepository.save(booking), this.statusChange(booking));

            } else throw new DuplicateException(
                    String.format("Ошибка при добавлении бронирования с %s по %s: " +
                                    "временной промежуток полностью или частично занят.",
                            booking.getStartTime(), booking.getEndTime()));

        } else if (itemOwnerId == bookerId) {
            throw new NotFoundException(String.format(
                    "Ошибка добавления бронирования: " +
                            "попытка пользователя с id=%d забронировать собственную вещь.", bookerId
            ));

        } else throw new ValidationException(
                String.format("Ошибка добавления бронирования: " +
                        "вещь с id=%d недоступна для бронирования.", itemId));
    }

    @Override
    @Transactional
    public BookingDto getBookingDto(long bookingId, long requesterId) {
        Booking booking = this.getBooking(bookingId);

        if (requesterId == booking.getBooker().getId() || requesterId == booking.getItem().getOwner().getId()) {
            return mapper.mapToDto(booking, this.statusChange(booking));

        } else throw new NotFoundException(
                String.format("Ошибка: попытка получения информации о бронировании с id=%d пользователем с id=%d, " +
                        "не являющимся автором бронирования или владельцем вещи.", bookingId, requesterId
                ));
    }

    @Override
    @Transactional
    public Collection<BookingDto> getBookingsUserAndState(
            Long bookerId, Long ownerId, String state, int startingIndex, Integer collectionSize) {
        if (ownerId != null && userService.userNotFound(ownerId)) {
            throw new NotFoundException(
                    String.format("Ошибка при получении бронирований по владельцу вещи: " +
                            "пользователя с id=%d не существует.", ownerId));
        }

        if (bookerId != null && userService.userNotFound(bookerId)) {
            throw new NotFoundException(
                    String.format("Ошибка при получении бронирований по автору: " +
                            "пользователя с id=%d не существует.", bookerId));
        }
        if (collectionSize == null) {
            collectionSize = Integer.MAX_VALUE;
        }
        Pageable pageable = Pageable.ofSize(startingIndex + collectionSize);
        Collection<Booking> collection;
        BookingStatus status;

        status = parseStatus(state);

        if (status == ALL) {
            collection = bookingRepository.getAllByBookerIdOrItemOwnerIdOrderByStartTimeDesc(
                    bookerId, ownerId, pageable).getContent();
            log.debug("Получен список: {}", collection);

        } else if (status == WAITING) {
            collection = bookingRepository.getWaitingOrRejectedBookings(
                    bookerId, ownerId, null, pageable).getContent();

        } else if (status == REJECTED) {
            collection = bookingRepository.getWaitingOrRejectedBookings(
                    bookerId, ownerId, false, pageable).getContent();

        } else if (status == PAST) {
            collection = bookingRepository.getPastBookingsByBookerIdOrOwnerId(
                    bookerId, ownerId, pageable).getContent();

        } else if (status == FUTURE) {
            collection = bookingRepository.getFutureBookings(bookerId, ownerId, pageable).getContent();

        } else {
            collection = bookingRepository.getCurrentBookings(bookerId, ownerId, pageable).getContent();
        }
        return collection.stream()
                .skip(startingIndex)
                .map(booking -> mapper.mapToDto(booking, this.statusChange(booking)))
                .collect(Collectors.toCollection(ArrayList::new));

    }

    @Transactional
    @Override
    public BookingDto setApproval(long bookingId, boolean approved, long requesterId) {
        Booking booking = this.getBooking(bookingId);
        if (booking.getItem().getOwner().getId() != requesterId) {
            throw new NotFoundException(String.format("Ошибка: попытка изменить статус одобрения бронирования " +
                    "со стороны пользователя с id=%d, не являющегося владельцем бронируемой вещи.", requesterId));

        } else if (booking.getApproved() != null) {
            throw new ValidationException(String.format(
                    "Ошибка: статус одобрения бронирования с id=%d уже был изменен ранее.", bookingId));
        }
        booking.setApproved(approved);

        log.debug("Одобрение бронирования с id={} изменено на {}", bookingId, approved);
        return mapper.mapToDto(booking, this.statusChange(booking));
    }

    @Override
    public Map<ActualItemBooking, BookingDtoShort> getLastAndNextBookingByItem(Item item, long requesterId) {
        Map<ActualItemBooking, BookingDtoShort> bookingsMap;

        if (item.getOwner().getId() == requesterId) {
            List<Booking> currentAndFutureBookings = new ArrayList<>(
                    bookingRepository.getActiveBookings(item.getId()));
            bookingsMap = new HashMap<>();
            Booking lastBooking = null;
            Booking nextBooking = null;

            if (currentAndFutureBookings.size() > 1) {
                if (currentAndFutureBookings.get(0).getStartTime().isBefore(LocalDateTime.now())) {
                    lastBooking = currentAndFutureBookings.get(0);
                    nextBooking = currentAndFutureBookings.get(1);

                } else {
                    nextBooking = currentAndFutureBookings.get(0);
                    lastBooking = getLastBooking(item);
                }

            } else if (currentAndFutureBookings.size() == 1) {
                if (currentAndFutureBookings.get(0).getStartTime().isBefore(LocalDateTime.now())) {
                    lastBooking = currentAndFutureBookings.get(0);

                } else {
                    nextBooking = currentAndFutureBookings.get(0);
                    lastBooking = getLastBooking(item);
                }
            }
            bookingsMap.put(ActualItemBooking.LAST, mapper.mapToShortDto(lastBooking));
            bookingsMap.put(ActualItemBooking.NEXT, mapper.mapToShortDto(nextBooking));

        } else {
            bookingsMap = new HashMap<>();
            bookingsMap.put(LAST, null);
            bookingsMap.put(NEXT, null);
        }
        return bookingsMap;
    }

    @Override
    public boolean dontMakeBookings(long bookerId, long itemId) {
        return bookingRepository.getApprovedBookingsNotInFuture(bookerId, itemId).isEmpty();
    }

    private boolean freeBookingTime(Booking booking) {
        List<Booking> activeBookings = new ArrayList<>(
                bookingRepository.getActiveBookings(booking.getItem().getId()));
        final LocalDateTime startTime = booking.getStartTime();
        final LocalDateTime endTime = booking.getEndTime();
        LocalDateTime leftBorder = LocalDateTime.now();
        LocalDateTime rightBorder;

        if (activeBookings.size() > 0) {
            for (Booking activeBooking : activeBookings) {
                rightBorder = activeBooking.getStartTime();

                if (leftBorder.isBefore(startTime) && rightBorder.isAfter(endTime)) {
                    return true;

                } else if (leftBorder.isAfter(endTime)) {
                    return false;
                }
                leftBorder = rightBorder;
            }
            return leftBorder.isBefore(startTime);

        } else return true;
    }

    private Booking getBooking(long bookingId) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);

        if (bookingOptional.isPresent()) {
            return bookingOptional.get();
        } else throw new NotFoundException(String.format(
                "Ошибка при получении бронирования: объект с id=%d не найден.", bookingId));
    }

    private Booking getLastBooking(Item item) {
        List<Booking> pastBookings = new ArrayList<>(bookingRepository.getPastBookingsByItemId(item.getId()));
        Booking lastBooking = null;

        if (!pastBookings.isEmpty()) {
            lastBooking = pastBookings.get(0);

        }
        return lastBooking;
    }

    private BookingStatus statusChange(Booking booking) {
        Boolean approved = booking.getApproved();

        if (approved == null) {
            return WAITING;

        } else if (!approved) {
            return REJECTED;

        } else return APPROVED;
    }

    private BookingStatus parseStatus(String state) {
        BookingStatus status;

        if (state != null) {
            try {
                status = BookingStatus.valueOf(state.toUpperCase());

            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown state: UNSUPPORTED_STATUS");
            }
        } else {
            status = ALL;
        }
        return status;
    }
}
