package ru.practicum.shareit.booking.repository;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AllArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class BookingRepositoryTest {

    private UserRepository userRepository;
    private ItemRepository itemRepository;
    private BookingRepository bookingRepository;

    @Test
    public void getAllByBookerIdOrOwnerIdAndOrderByStartTimeDescTest() {
        Item item1 = getItem();

        User booker = makeDefaultUser();
        booker.setEmail("newEmail@mail.ru");
        booker = userRepository.save(booker);

        Booking futureBooking = makeDefaultBooking(item1, booker);
        futureBooking.setStartTime(futureBooking.getStartTime().plusMonths(1));
        futureBooking.setEndTime(futureBooking.getEndTime().plusMonths(1));
        futureBooking = bookingRepository.save(futureBooking);
        assertEquals(List.of(futureBooking), bookingRepository.getAllByBookerIdOrItemOwnerIdOrderByStartTimeDesc(
                booker.getId(), null, Pageable.unpaged()).getContent());

        User owner2 = makeDefaultUser();
        owner2.setEmail("anotherEmail@mail.com");
        owner2 = userRepository.save(owner2);
        Item item2 = makeDefaultItem(owner2);
        item2 = itemRepository.save(item2);

        Booking currentBooking = makeDefaultBooking(item2, booker);
        currentBooking = bookingRepository.save(currentBooking);
        assertEquals(List.of(futureBooking, currentBooking),
                bookingRepository.getAllByBookerIdOrItemOwnerIdOrderByStartTimeDesc(
                        booker.getId(), null, Pageable.unpaged()).getContent());
    }

    @Test
    public void getAllByBookerIdOrOwnerIdAndApprovedOrderByStartTimeDescTest() {
        Item item1 = getItem();

        User booker = makeDefaultUser();
        booker.setEmail("email@mail.ru");
        booker = userRepository.save(booker);

        Booking preApprovedBooking = makeDefaultBooking(item1, booker);
        preApprovedBooking.setApproved(true);
        preApprovedBooking = bookingRepository.save(preApprovedBooking);
        assertEquals(List.of(preApprovedBooking),
                bookingRepository.getWaitingOrRejectedBookings(
                        booker.getId(), null, true, Pageable.unpaged()).getContent());

        User owner2 = makeDefaultUser();
        owner2.setEmail("anotherEmail@mail.com");
        owner2 = userRepository.save(owner2);
        Item item2 = makeDefaultItem(owner2);
        item2 = itemRepository.save(item2);

        Booking preRejectedBooking = makeDefaultBooking(item2, booker);
        preRejectedBooking.setApproved(false);
        preRejectedBooking = bookingRepository.save(preRejectedBooking);
        assertEquals(List.of(preRejectedBooking),
                bookingRepository.getWaitingOrRejectedBookings(
                        booker.getId(), null, false, Pageable.unpaged()).getContent());
    }

    @Test
    public void getPastBookingsTest() {
        Item item1 = getItem();

        User booker = makeDefaultUser();
        booker.setEmail("newEmail@mail.ru");
        booker = userRepository.save(booker);

        Booking pastBooking = makeDefaultBooking(item1, booker);
        pastBooking.setApproved(true);
        pastBooking.setStartTime(pastBooking.getStartTime().minusMonths(1));
        pastBooking.setEndTime(pastBooking.getEndTime().minusMonths(1));
        pastBooking = bookingRepository.save(pastBooking);
        assertEquals(List.of(pastBooking), bookingRepository.getPastBookingsByBookerIdOrOwnerId(
                booker.getId(), null, Pageable.unpaged()).getContent());

        User owner2 = makeDefaultUser();
        owner2.setEmail("anotherEmail@mail.com");
        owner2 = userRepository.save(owner2);
        Item item2 = makeDefaultItem(owner2);
        item2 = itemRepository.save(item2);

        Booking currentBooking = makeDefaultBooking(item2, booker);
        currentBooking.setApproved(true);
        bookingRepository.save(currentBooking);
        assertEquals(List.of(pastBooking),
                bookingRepository.getPastBookingsByBookerIdOrOwnerId(
                        booker.getId(), null, Pageable.unpaged()).getContent());
    }

    @Test
    public void getCurrentBookingsTest() {
        Item item1 = getItem();

        User booker = makeDefaultUser();
        booker.setEmail("newEmail@mail.ru");
        booker = userRepository.save(booker);

        Booking pastBooking = makeDefaultBooking(item1, booker);
        pastBooking.setApproved(true);
        pastBooking.setStartTime(pastBooking.getStartTime().minusMonths(1));
        pastBooking.setEndTime(pastBooking.getEndTime().minusMonths(1));
        bookingRepository.save(pastBooking);
        assertEquals(List.of(), bookingRepository.getCurrentBookings(
                booker.getId(), null, Pageable.unpaged()).getContent());

        User owner2 = makeDefaultUser();
        owner2.setEmail("anotherEmail@mail.com");
        owner2 = userRepository.save(owner2);
        Item item2 = makeDefaultItem(owner2);
        item2 = itemRepository.save(item2);

        Booking currentBooking = makeDefaultBooking(item2, booker);
        currentBooking.setApproved(true);
        currentBooking.setStartTime(LocalDateTime.now().minusSeconds(1));
        currentBooking = bookingRepository.save(currentBooking);
        assertEquals(List.of(currentBooking),
                bookingRepository.getCurrentBookings(
                        booker.getId(), null, Pageable.unpaged()).getContent());
    }

    @Test
    public void getFutureBookingsTest() {
        User owner1 = userRepository.save(makeDefaultUser());
        Item item1 = makeDefaultItem(owner1);
        item1 = itemRepository.save(item1);

        User booker = makeDefaultUser();
        booker.setEmail("newEmail@mail.ru");
        booker = userRepository.save(booker);

        Booking futureBooking = makeDefaultBooking(item1, booker);
        futureBooking.setApproved(true);
        futureBooking.setStartTime(futureBooking.getStartTime().plusMonths(1));
        futureBooking.setEndTime(futureBooking.getEndTime().plusMonths(1));
        futureBooking = bookingRepository.save(futureBooking);
        assertEquals(List.of(futureBooking), bookingRepository.getAllByItemOwnerId(owner1.getId()));

        User owner2 = makeDefaultUser();
        owner2.setEmail("anotherEmail@mail.com");
        owner2 = userRepository.save(owner2);
        Item item2 = makeDefaultItem(owner2);
        item2 = itemRepository.save(item2);

        Booking currentBooking = makeDefaultBooking(item2, booker);
        currentBooking.setApproved(true);
        currentBooking.setStartTime(LocalDateTime.now().minusSeconds(1));
        bookingRepository.save(currentBooking);
        assertEquals(List.of(currentBooking),
                bookingRepository.getAllByItemOwnerId(owner2.getId()));
    }

    @Test
    public void getAllByOwnerIdTest() {
        User owner1 = userRepository.save(makeDefaultUser());
        Item item1 = makeDefaultItem(owner1);
        item1 = itemRepository.save(item1);

        User booker = makeDefaultUser();
        booker.setEmail("newEmail@mail.ru");
        booker = userRepository.save(booker);

        Booking booking1 = makeDefaultBooking(item1, booker);
        booking1.setApproved(true);
        booking1.setStartTime(booking1.getStartTime().plusMonths(1));
        booking1.setEndTime(booking1.getEndTime().plusMonths(1));
        booking1 = bookingRepository.save(booking1);
        assertEquals(List.of(booking1), bookingRepository.getAllByItemOwnerId(owner1.getId()));

        User owner2 = makeDefaultUser();
        owner2.setEmail("anotherEmail@mail.com");
        owner2 = userRepository.save(owner2);
        Item item2 = makeDefaultItem(owner2);
        item2 = itemRepository.save(item2);

        Booking booking2 = makeDefaultBooking(item2, booker);
        booking2.setApproved(true);
        bookingRepository.save(booking2);
        assertEquals(List.of(booking2),
                bookingRepository.getAllByItemOwnerId(owner2.getId()));
    }

    private Item getItem() {
        User owner1 = userRepository.save(makeDefaultUser());
        Item item1 = makeDefaultItem(owner1);
        item1 = itemRepository.save(item1);
        return item1;
    }

    private Booking makeDefaultBooking(Item item, User booker) {
        return Booking.builder()
                .item(item)
                .booker(booker)
                .startTime(LocalDateTime.now().plusMinutes(1).truncatedTo(ChronoUnit.SECONDS))
                .endTime(LocalDateTime.now().plusMinutes(1).plusDays(1).truncatedTo(ChronoUnit.SECONDS))
                .build();
    }

    private Item makeDefaultItem(User owner) {
        return Item.builder()
                .owner(owner)
                .name("Item name")
                .description("Item description")
                .available(true)
                .build();
    }

    private User makeDefaultUser() {
        return User.builder()
                .name("User Name")
                .email("user@mail.ru")
                .build();
    }
}
