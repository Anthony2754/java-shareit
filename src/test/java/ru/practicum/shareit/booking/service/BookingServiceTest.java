package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ActualItemBooking;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AllArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class BookingServiceTest {

    private UserService userService;
    private ItemService itemService;
    private BookingService bookingService;

    @Test
    public void addBookingTest() {
        UserDto user = userService.addUserDto(makeDefaultUser());
        ItemDto item = itemService.addItemDto(makeDefaultItem(), user.getId());

        UserDto booker = makeDefaultUser();
        booker.setEmail("newEmail@mail.ru");
        booker = userService.addUserDto(booker);

        BookingDto booking = bookingService.addBooking(makeDefaultBookingDtoRequest(item.getId()), booker.getId());

        assertEquals(booking, bookingService.getBookingDto(booking.getId(), booker.getId()));
    }

    @Test
    public void shouldBeExceptionForAddBookingWhereEndBeforeStart() {
        UserDto user = userService.addUserDto(makeDefaultUser());
        ItemDto item = itemService.addItemDto(makeDefaultItem(), user.getId());

        UserDto booker = makeDefaultUser();
        booker.setEmail("newEmail@mail.ru");
        booker = userService.addUserDto(booker);

        BookingDtoRequest request = makeDefaultBookingDtoRequest(item.getId());
        request.setEnd(request.getStart().minusDays(1));

        UserDto finalBooker = booker;
        assertThrows(ValidationException.class, () -> bookingService.addBooking(request, finalBooker.getId()));
    }

    @Test
    public void shouldBeExceptionForAddBookingInBookedTime() {
        LocalDateTime time1 = LocalDateTime.now().plusMinutes(1);
        LocalDateTime time2 = time1.plusDays(1);
        LocalDateTime time3 = time2.plusDays(1);
        LocalDateTime time4 = time3.plusDays(1);
        LocalDateTime time5 = time4.plusDays(1);
        LocalDateTime time6 = time5.plusDays(1);

        UserDto user = userService.addUserDto(makeDefaultUser());
        ItemDto item = itemService.addItemDto(makeDefaultItem(), user.getId());
        long itemId = item.getId();

        UserDto booker1 = makeDefaultUser();
        booker1.setEmail("newEmail1@mail.ru");
        booker1 = userService.addUserDto(booker1);

        UserDto booker2 = makeDefaultUser();
        booker2.setEmail("newEmail2@mail.ru");
        booker2 = userService.addUserDto(booker2);

        UserDto booker3 = makeDefaultUser();
        booker3.setEmail("newEmail3@mail.ru");
        booker3 = userService.addUserDto(booker3);

        BookingDtoRequest booking1 = makeDefaultBookingDtoRequest(itemId);
        booking1.setStart(time1);
        booking1.setEnd(time3);
        bookingService.addBooking(booking1, booker1.getId());

        BookingDtoRequest booking2 = makeDefaultBookingDtoRequest(itemId);
        booking2.setStart(time4);
        booking2.setEnd(time6);
        bookingService.addBooking(booking2, booker2.getId());

        BookingDtoRequest booking3 = makeDefaultBookingDtoRequest(itemId);
        booking3.setStart(time2);
        booking3.setEnd(time5);

        UserDto finalBooker = booker3;
        assertThrows(DuplicateException.class,
                () -> bookingService.addBooking(booking3, finalBooker.getId()));
    }

    @Test
    public void shouldBeExceptionForBookingUnavailableItem() {
        UserDto user = userService.addUserDto(makeDefaultUser());
        ItemDto item = makeDefaultItem();
        item.setAvailable(false);
        item = itemService.addItemDto(item, user.getId());

        UserDto booker = makeDefaultUser();
        booker.setEmail("newEmail@mail.ru");
        booker = userService.addUserDto(booker);

        ItemDto finalItem = item;
        UserDto finalBooker = booker;
        assertThrows(ValidationException.class,
                () -> bookingService.addBooking(makeDefaultBookingDtoRequest(finalItem.getId()), finalBooker.getId()));
    }

    @Test
    public void shouldBeExceptionForGetNotFoundBooking() {
        UserDto userDto = makeDefaultUser();
        userDto = userService.addUserDto(userDto);

        UserDto finalUserDto = userDto;
        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingDto(finalUserDto.getId(), 1));
    }

    @Test
    public void shouldBeExceptionForBookingOwnedItem() {
        UserDto user = userService.addUserDto(makeDefaultUser());
        ItemDto item = itemService.addItemDto(makeDefaultItem(), user.getId());

        assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(makeDefaultBookingDtoRequest(item.getId()), user.getId()));
    }

    @Test
    public void shouldBeExceptionForGetUnrelatedBooking() {
        UserDto owner = userService.addUserDto(makeDefaultUser());

        ItemDto item = makeDefaultItem();
        item.setAvailable(true);
        item = itemService.addItemDto(item, owner.getId());

        UserDto booker = makeDefaultUser();
        booker.setEmail("newEmail@mail.ru");
        booker = userService.addUserDto(booker);

        UserDto otherUser = makeDefaultUser();
        otherUser.setEmail("anotherEmail@mail.ru");
        otherUser = userService.addUserDto(otherUser);

        BookingDto booking = bookingService.addBooking(makeDefaultBookingDtoRequest(item.getId()), booker.getId());

        UserDto finalOtherUser = otherUser;
        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingDto(booking.getId(), finalOtherUser.getId()));
    }

    @Test
    public void getBookingsByBookerIdOrOwnerIdAndStatusTest() {
        UserDto user = userService.addUserDto(makeDefaultUser());
        long userId = user.getId();
        ItemDto item1 = itemService.addItemDto(makeDefaultItem(), userId);
        ItemDto item2 = itemService.addItemDto(makeDefaultItem(), userId);
        ItemDto item3 = itemService.addItemDto(makeDefaultItem(), userId);
        ItemDto item4 = itemService.addItemDto(makeDefaultItem(), userId);
        ItemDto item5 = itemService.addItemDto(makeDefaultItem(), userId);

        UserDto booker = makeDefaultUser();
        booker.setEmail("newEmail1@mail.ru");
        booker = userService.addUserDto(booker);
        long bookerId = booker.getId();

        BookingDtoRequest waitingBookingRequest = makeDefaultBookingDtoRequest(item1.getId());
        waitingBookingRequest.setStart(waitingBookingRequest.getStart().plusMinutes(1));
        waitingBookingRequest.setEnd(waitingBookingRequest.getEnd().plusMinutes(1));
        BookingDto waitingBooking = bookingService.addBooking(waitingBookingRequest, bookerId);
        assertEquals(List.of(waitingBooking), bookingService.getBookingsUserAndState(
                bookerId, null, BookingStatus.WAITING.toString(), 0, Integer.MAX_VALUE));

        BookingDtoRequest rejectedBookingRequest = makeDefaultBookingDtoRequest(item2.getId());
        BookingDto rejectedBooking = bookingService.addBooking(rejectedBookingRequest, bookerId);
        rejectedBooking = bookingService.setApproval(rejectedBooking.getId(), false, userId);
        assertEquals(List.of(rejectedBooking), bookingService.getBookingsUserAndState(
                bookerId, null, BookingStatus.REJECTED.toString(), 0, Integer.MAX_VALUE));

        BookingDtoRequest pastBookingRequest = makeDefaultBookingDtoRequest(item3.getId());
        pastBookingRequest.setStart(pastBookingRequest.getStart().minusDays(3));
        pastBookingRequest.setEnd(pastBookingRequest.getEnd().minusDays(3));
        BookingDto pastBooking = bookingService.addBooking(pastBookingRequest, bookerId);
        pastBooking = bookingService.setApproval(pastBooking.getId(), true, userId);
        assertEquals(List.of(pastBooking), bookingService.getBookingsUserAndState(
                bookerId, null, BookingStatus.PAST.toString(), 0, Integer.MAX_VALUE));

        BookingDtoRequest futureBookingRequest = makeDefaultBookingDtoRequest(item4.getId());
        futureBookingRequest.setStart(futureBookingRequest.getStart().plusDays(1));
        futureBookingRequest.setEnd(futureBookingRequest.getEnd().plusDays(1));
        BookingDto futureBooking = bookingService.addBooking(futureBookingRequest, bookerId);
        futureBooking = bookingService.setApproval(futureBooking.getId(), true, userId);
        assertEquals(List.of(futureBooking, waitingBooking, rejectedBooking), bookingService.getBookingsUserAndState(
                bookerId, null, BookingStatus.FUTURE.toString(), 0, Integer.MAX_VALUE));

        BookingDtoRequest currentBookingRequest = makeDefaultBookingDtoRequest(item5.getId());
        currentBookingRequest.setStart(currentBookingRequest.getStart().minusHours(1));
        currentBookingRequest.setEnd(currentBookingRequest.getEnd().plusDays(1));
        BookingDto currentBooking = bookingService.addBooking(currentBookingRequest, bookerId);
        currentBooking = bookingService.setApproval(currentBooking.getId(), true, userId);
        assertEquals(List.of(currentBooking), bookingService.getBookingsUserAndState(
                bookerId, null, BookingStatus.CURRENT.toString(), 0, Integer.MAX_VALUE));

        assertEquals(List.of(futureBooking, waitingBooking, rejectedBooking, currentBooking, pastBooking),
                bookingService.getBookingsUserAndState(
                bookerId, null, BookingStatus.ALL.toString(), 0, Integer.MAX_VALUE));
    }

    @Test
    public void shouldBeExceptionForGetBookingsByWrongState() {
        UserDto user = userService.addUserDto(makeDefaultUser());
        long userId = user.getId();
        ItemDto item1 = itemService.addItemDto(makeDefaultItem(), userId);

        UserDto booker = makeDefaultUser();
        booker.setEmail("newEmail1@mail.ru");
        booker = userService.addUserDto(booker);
        long bookerId = booker.getId();

        BookingDtoRequest waitingBookingRequest = makeDefaultBookingDtoRequest(item1.getId());
        waitingBookingRequest.setStart(waitingBookingRequest.getStart().plusMinutes(1));
        waitingBookingRequest.setEnd(waitingBookingRequest.getEnd().plusMinutes(1));
        bookingService.addBooking(waitingBookingRequest, bookerId);
        assertThrows(IllegalArgumentException.class, () -> bookingService.getBookingsUserAndState(
                bookerId, null, "wrong state", 0, Integer.MAX_VALUE));
    }

    @Test
    public void shouldBeExceptionForGetBookingsByWrongBookerIdOrOwnerId() {
        UserDto user = userService.addUserDto(makeDefaultUser());
        long userId = user.getId();
        ItemDto item1 = itemService.addItemDto(makeDefaultItem(), userId);

        UserDto booker = makeDefaultUser();
        booker.setEmail("newEmail1@mail.ru");
        booker = userService.addUserDto(booker);
        long bookerId = booker.getId();

        BookingDtoRequest waitingBookingRequest = makeDefaultBookingDtoRequest(item1.getId());
        waitingBookingRequest.setStart(waitingBookingRequest.getStart().plusMinutes(1));
        waitingBookingRequest.setEnd(waitingBookingRequest.getEnd().plusMinutes(1));
        bookingService.addBooking(waitingBookingRequest, bookerId);
        assertThrows(NotFoundException.class, () -> bookingService.getBookingsUserAndState(
                100L, null, BookingStatus.WAITING.toString(), 0, Integer.MAX_VALUE));
        assertThrows(NotFoundException.class, () -> bookingService.getBookingsUserAndState(
                null, 100L, BookingStatus.WAITING.toString(), 0, Integer.MAX_VALUE));
    }

    @Test
    public void getLastAndNextBookingByItemWithCurrentTest() {
        Map<ActualItemBooking, BookingDtoShort> bookingsMap;

        LocalDateTime timePoint1 = LocalDateTime.now().minusMinutes(1);
        LocalDateTime timePoint2 = timePoint1.plusDays(1);
        LocalDateTime timePoint3 = timePoint2.plusDays(1);
        LocalDateTime timePoint4 = timePoint3.plusDays(1);
        LocalDateTime timePoint5 = timePoint4.plusDays(1);
        LocalDateTime timePoint6 = timePoint5.plusDays(1);

        UserDto user = userService.addUserDto(makeDefaultUser());
        ItemDto itemDto = itemService.addItemDto(makeDefaultItem(), user.getId());
        Item item = itemService.getItem(itemDto.getId());
        long itemId = item.getId();

        UserDto booker1 = makeDefaultUser();
        booker1.setEmail("newEmail@mail.ru");
        booker1 = userService.addUserDto(booker1);

        BookingDtoRequest dtoRequest1 = makeDefaultBookingDtoRequest(itemId);
        dtoRequest1.setStart(timePoint1);
        dtoRequest1.setEnd(timePoint2);
        BookingDto bookingDto1 = bookingService.addBooking(dtoRequest1, booker1.getId());

        bookingsMap = bookingService.getLastAndNextBookingByItem(item, user.getId());
        assertEquals(bookingsMap.get(ActualItemBooking.LAST).getId(), bookingDto1.getId());
        assertNull(bookingsMap.get(ActualItemBooking.NEXT));

        BookingDtoRequest dtoRequest2 = makeDefaultBookingDtoRequest(itemId);
        dtoRequest2.setStart(timePoint3);
        dtoRequest2.setEnd(timePoint4);
        BookingDto bookingDto2 = bookingService.addBooking(dtoRequest2, booker1.getId());

        bookingsMap = bookingService.getLastAndNextBookingByItem(item, user.getId());
        assertEquals(bookingsMap.get(ActualItemBooking.LAST).getId(), bookingDto1.getId());
        assertEquals(bookingsMap.get(ActualItemBooking.NEXT).getId(), bookingDto2.getId());

        BookingDtoRequest dtoRequest3 = makeDefaultBookingDtoRequest(itemId);
        dtoRequest3.setStart(timePoint5);
        dtoRequest3.setEnd(timePoint6);
        bookingService.addBooking(dtoRequest3, booker1.getId());

        bookingsMap = bookingService.getLastAndNextBookingByItem(item, user.getId());
        assertEquals(bookingsMap.get(ActualItemBooking.LAST).getId(), bookingDto1.getId());
        assertEquals(bookingsMap.get(ActualItemBooking.NEXT).getId(), bookingDto2.getId());
    }

    @Test
    public void getLastAndNextBookingByItemWithoutCurrentTest() {
        Map<ActualItemBooking, BookingDtoShort> bookingsMap;

        LocalDateTime timePoint1 = LocalDateTime.now().minusMinutes(1);
        LocalDateTime timePoint2 = timePoint1.plusDays(1);
        LocalDateTime timePoint3 = timePoint2.plusDays(1);
        LocalDateTime timePoint4 = timePoint3.plusDays(1);
        LocalDateTime timePoint5 = timePoint4.plusDays(1);
        LocalDateTime timePoint6 = timePoint5.plusDays(1);

        UserDto user = userService.addUserDto(makeDefaultUser());
        ItemDto itemDto = itemService.addItemDto(makeDefaultItem(), user.getId());
        Item item = itemService.getItem(itemDto.getId());
        long itemId = item.getId();

        UserDto booker1 = makeDefaultUser();
        booker1.setEmail("newEmail@mail.ru");
        booker1 = userService.addUserDto(booker1);

        BookingDtoRequest dtoRequest2 = makeDefaultBookingDtoRequest(itemId);
        dtoRequest2.setStart(timePoint3);
        dtoRequest2.setEnd(timePoint4);
        BookingDto bookingDto2 = bookingService.addBooking(dtoRequest2, booker1.getId());

        bookingsMap = bookingService.getLastAndNextBookingByItem(item, user.getId());
        assertNull(bookingsMap.get(ActualItemBooking.LAST));
        assertEquals(bookingsMap.get(ActualItemBooking.NEXT).getId(), bookingDto2.getId());

        BookingDtoRequest dtoRequest3 = makeDefaultBookingDtoRequest(itemId);
        dtoRequest3.setStart(timePoint5);
        dtoRequest3.setEnd(timePoint6);
        bookingService.addBooking(dtoRequest3, booker1.getId());

        bookingsMap = bookingService.getLastAndNextBookingByItem(item, user.getId());
        assertNull(bookingsMap.get(ActualItemBooking.LAST));
        assertEquals(bookingsMap.get(ActualItemBooking.NEXT).getId(), bookingDto2.getId());
    }

    @Test
    public void setApprovalTest() {
        UserDto user = userService.addUserDto(makeDefaultUser());
        ItemDto item = itemService.addItemDto(makeDefaultItem(), user.getId());

        UserDto booker = makeDefaultUser();
        booker.setEmail("newEmail1@mail.ru");
        booker = userService.addUserDto(booker);
        BookingDto booking = bookingService.addBooking(makeDefaultBookingDtoRequest(item.getId()), booker.getId());
        bookingService.setApproval(booking.getId(), true, user.getId());

        assertTrue(bookingService.getBookingsUserAndState(booker.getId(), null, BookingStatus.WAITING.toString(),
                0, Integer.MAX_VALUE).isEmpty());
    }

    @Test
    public void shouldBeExceptionForWrongUserApproval() {
        UserDto user = userService.addUserDto(makeDefaultUser());
        ItemDto item = itemService.addItemDto(makeDefaultItem(), user.getId());

        UserDto booker = makeDefaultUser();
        booker.setEmail("newEmail1@mail.ru");
        booker = userService.addUserDto(booker);
        BookingDto booking = bookingService.addBooking(makeDefaultBookingDtoRequest(item.getId()), booker.getId());

        UserDto finalBooker = booker;
        assertThrows(NotFoundException.class,
                () -> bookingService.setApproval(booking.getId(), true, finalBooker.getId()));
    }

    @Test
    public void shouldBeExceptionForAlreadyApproval() {
        UserDto user = userService.addUserDto(makeDefaultUser());
        ItemDto item = itemService.addItemDto(makeDefaultItem(), user.getId());

        UserDto booker = makeDefaultUser();
        booker.setEmail("newEmail1@mail.ru");
        booker = userService.addUserDto(booker);
        BookingDto booking = bookingService.addBooking(makeDefaultBookingDtoRequest(item.getId()), booker.getId());
        bookingService.setApproval(booking.getId(), true, user.getId());

        assertThrows(ValidationException.class,
                () -> bookingService.setApproval(booking.getId(), false, user.getId()));
    }

    private BookingDtoRequest makeDefaultBookingDtoRequest(long itemId) {
        return BookingDtoRequest.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusMinutes(1).truncatedTo(ChronoUnit.SECONDS))
                .end(LocalDateTime.now().plusMinutes(1).plusDays(1).truncatedTo(ChronoUnit.SECONDS))
                .build();
    }

    private ItemDto makeDefaultItem() {
        return ItemDto.builder()
                .name("Item name")
                .description("Item description")
                .available(true)
                .build();
    }

    private UserDto makeDefaultUser() {
        return UserDto.builder()
                .name("User Name")
                .email("user@mail.ru")
                .build();
    }
}
