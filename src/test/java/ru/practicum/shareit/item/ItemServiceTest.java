package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AllArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ItemServiceTest {

    private UserService userService;
    private ItemService itemService;
    private BookingService bookingService;

    private BookingDtoRequest makeDefaultBookingDtoRequest(long itemId) {
        return BookingDtoRequest.builder()
                .itemId(itemId)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusMinutes(1).plusDays(1).truncatedTo(ChronoUnit.SECONDS))
                .build();
    }

    private CommentDto makeDefaultComment() {
        return CommentDto.builder()
                .text("default comment")
                .build();
    }

    private ItemDto makeDefaultItem() {
        return ItemDto.builder()
                .name("default item")
                .description("default item description")
                .available(true)
                .build();
    }

    private UserDto makeDefaultUser() {
        return UserDto.builder()
                .name("User Name")
                .email("email@mail.ru")
                .build();
    }

    @Test
    public void addItemTest() {
        UserDto user = userService.addUserDto(makeDefaultUser());
        ItemDto item = itemService.addItemDto(makeDefaultItem(), user.getId());
        item.setComments(List.of());

        assertEquals(item, itemService.getItemDto(item.getId(), user.getId()));
    }

    @Test
    public void shouldBeExceptionForAddItemWithoutOwner() {
        UserDto user = makeDefaultUser();
        user.setId(1L);
        ItemDto item = makeDefaultItem();

        assertThrows(NotFoundException.class, () -> itemService.addItemDto(item, user.getId()));
    }

    @Test
    public void shouldBeExceptionAddItemWithoutNameOrDescription() {
        UserDto user = userService.addUserDto(makeDefaultUser());
        ItemDto item = makeDefaultItem();
        item.setName("");
        assertThrows(DataIntegrityViolationException.class, () -> itemService.addItemDto(item, user.getId()));

        item.setName("name");
        item.setDescription("");
        assertThrows(DataIntegrityViolationException.class, () -> itemService.addItemDto(item, user.getId()));

        item.setName("");
        assertThrows(DataIntegrityViolationException.class, () -> itemService.addItemDto(item, user.getId()));
    }

    @Test
    public void shouldBeExceptionForGetNotFoundItem() {
        UserDto user = userService.addUserDto(makeDefaultUser());

        assertThrows(NotFoundException.class, () -> itemService.getItemDto(1L, user.getId()));
    }

    @Test
    public void addCommentTest() {
        UserDto user = userService.addUserDto(makeDefaultUser());
        UserDto booker = userService.addUserDto(UserDto.builder().name("Booker Name").email("booker@mail.ru").build());
        long userId = user.getId();
        long bookerId = booker.getId();

        ItemDto item = itemService.addItemDto(makeDefaultItem(), userId);
        long itemId = item.getId();

        BookingDtoRequest request = makeDefaultBookingDtoRequest(itemId);

        BookingDto bookingDto = bookingService.addBooking(request, bookerId);
        bookingService.setApproval(bookingDto.getId(), true, userId);

        CommentDto comment = makeDefaultComment();
        comment = itemService.addCommentDto(comment, bookerId, itemId);

        assertEquals(List.of(comment), itemService.getItemDto(itemId, userId).getComments());
    }

    @Test
    public void shouldBeExceptionForCommenterWithoutBooking() {
        UserDto user = userService.addUserDto(makeDefaultUser());
        UserDto booker = userService.addUserDto(UserDto.builder().name("Booker Name").email("booker@mail.ru").build());
        long userId = user.getId();
        long bookerId = booker.getId();

        ItemDto item = itemService.addItemDto(makeDefaultItem(), userId);
        long itemId = item.getId();

        CommentDto comment = makeDefaultComment();

        assertThrows(ValidationException.class,
                () -> itemService.addCommentDto(comment, bookerId, itemId));
    }

    @Test
    public void getOwnerItemsTest() {
        UserDto userDto1 = makeDefaultUser();
        userDto1 = userService.addUserDto(userDto1);

        UserDto userDto2 = makeDefaultUser();
        userDto2.setEmail("newEmail@mail.ru");
        userDto2 = userService.addUserDto(userDto2);

        ItemDto item1 = itemService.addItemDto(makeDefaultItem(), userDto1.getId());
        item1.setComments(List.of());
        ItemDto item2 = itemService.addItemDto(makeDefaultItem(), userDto1.getId());
        item2.setComments(List.of());

        assertEquals(List.of(item1, item2),
                itemService.getOwnerItems(userDto1.getId()));
        assertEquals(List.of(),
                itemService.getOwnerItems(userDto2.getId()));
    }
}
