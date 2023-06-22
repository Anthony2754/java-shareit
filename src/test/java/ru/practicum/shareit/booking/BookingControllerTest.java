package ru.practicum.shareit.booking;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
public class BookingControllerTest {

    @LocalServerPort
    private int port;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;


    private final String defaultUri = String.format("http://localhost:%d/bookings", port);

    private HttpHeaders getDefaultHeader(Long userId) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("X-Sharer-User-Id", userId.toString());
        return httpHeaders;
    }

    private Collection<BookingDto> getBookingsOwnerAndStatus(Long ownerId, String state) throws Exception {

        MockHttpServletResponse servletResponse = mockMvc.perform(
                        get(defaultUri + "/owner")
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(ownerId))
                                .param("state", state)
                                .param("from", ((Integer) 0).toString())
                                .param("size", ((Integer) Integer.MAX_VALUE).toString()))
                        .andReturn().getResponse();

        return objectMapper.readValue(servletResponse.getContentAsString(), new TypeReference<>() {});
    }

    private Collection<BookingDto> getBookingsByBookerAndStatus(Long booker, String state) throws Exception {

        MockHttpServletResponse servletResponse = mockMvc.perform(get(defaultUri)
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(booker))
                                .param("state", state)
                                .param("from", ((Integer) 0).toString())
                                .param("size", ((Integer) Integer.MAX_VALUE).toString()))
                                .andReturn().getResponse();

        return objectMapper.readValue(servletResponse.getContentAsString(), new TypeReference<>() {});
    }

    private BookingDto setApproved(long requesterId, long bookingId, Boolean approved) throws Exception {

        MockHttpServletResponse servletResponse = mockMvc.perform(patch(defaultUri + "/" + bookingId)
                                .headers(getDefaultHeader(requesterId))
                                .param("approved", approved.toString()))
                                .andReturn().getResponse();

        return objectMapper.readValue(servletResponse.getContentAsString(), BookingDto.class);
    }

    private BookingDto addBooking(BookingDtoRequest bookingDtoRequest, long bookerId) throws Exception {

        MockHttpServletResponse servletResponse = mockMvc.perform(post(defaultUri)
                                .content(objectMapper.writeValueAsString(bookingDtoRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(bookerId)))
                                .andReturn().getResponse();

        return objectMapper.readValue(servletResponse.getContentAsString(), BookingDto.class);
    }

    private BookingDtoRequest makeDefaultBookingDtoRequest(long itemId) {
        return BookingDtoRequest.builder()
                .start(LocalDateTime.now().plusMinutes(1).truncatedTo(ChronoUnit.SECONDS))
                .end(LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS))
                .itemId(itemId)
                .build();
    }

    private ItemDto addDefaultItem(long ownerId) throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .name("Item name")
                .description("Item description")
                .available(true)
                .build();

        MockHttpServletResponse servletResponse = mockMvc.perform(post(String.format("http://localhost:%d/items", port))
                                .content(objectMapper.writeValueAsString(itemDto))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(ownerId)))
                                .andReturn().getResponse();

        return objectMapper.readValue(servletResponse.getContentAsString(), ItemDto.class);
    }

    private UserDto addDefaultUser(String email) throws Exception {
        UserDto userDto = UserDto.builder()
                .name("User Name")
                .email(email)
                .build();

        MockHttpServletResponse servletResponse = mockMvc.perform(post(String.format("http://localhost:%d/users", port))
                                .content(objectMapper.writeValueAsString(userDto))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andReturn().getResponse();

        return objectMapper.readValue(servletResponse.getContentAsString(), UserDto.class);
    }

    @Test
    public void addBookingTest() throws Exception {
        UserDto owner = addDefaultUser("email@mail.ru");
        UserDto booker = addDefaultUser("newEmail@mail.ru");

        ItemDto itemDto = addDefaultItem(owner.getId());
        BookingDtoRequest bookingDtoRequest = makeDefaultBookingDtoRequest(itemDto.getId());

        MockHttpServletResponse servletResponse = mockMvc.perform(
                        post(defaultUri)
                                .content(objectMapper.writeValueAsString(bookingDtoRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(booker.getId())))
                .andReturn().getResponse();
        assertEquals(servletResponse.getStatus(), HttpStatus.CREATED.value());
        BookingDto bookingDto = objectMapper.readValue(servletResponse.getContentAsString(), BookingDto.class);

        servletResponse = mockMvc.perform(
                        get(defaultUri + "/" + bookingDto.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(owner.getId())))
                .andReturn().getResponse();

        assertEquals(servletResponse.getStatus(), HttpStatus.OK.value());
        assertEquals(bookingDto, objectMapper.readValue(servletResponse.getContentAsString(), BookingDto.class));
    }

    @Test
    public void shouldBeExceptionForAddBookingWhereEndBeforeStart() throws Exception {
        UserDto user = addDefaultUser("email@mail.ru");
        ItemDto item = addDefaultItem(user.getId());

        UserDto booker = addDefaultUser("newEmail@mail.ru");

        BookingDtoRequest request = makeDefaultBookingDtoRequest(item.getId());
        request.setEnd(request.getStart().minusDays(1));

        MockHttpServletResponse servletResponse = mockMvc.perform(
                        post(defaultUri)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(booker.getId())))
                .andReturn().getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), servletResponse.getStatus());
    }

    @Test
    public void shouldBeExceptionForAddBookingInBookedTime() throws Exception {
        LocalDateTime time1 = LocalDateTime.now().plusMinutes(1);
        LocalDateTime time2 = time1.plusDays(1);
        LocalDateTime time3 = time2.plusDays(1);
        LocalDateTime time4 = time3.plusDays(1);
        LocalDateTime time5 = time4.plusDays(1);
        LocalDateTime time6 = time5.plusDays(1);

        UserDto user = addDefaultUser("email@mail.ru");
        ItemDto item = addDefaultItem(user.getId());
        long itemId = item.getId();

        UserDto booker1 = addDefaultUser("newEmail1@mail.ru");
        UserDto booker2 = addDefaultUser("newEmail2@mail.com");
        UserDto booker3 = addDefaultUser("newEmail3@mail.ru");

        BookingDtoRequest booking1 = makeDefaultBookingDtoRequest(itemId);
        booking1.setStart(time1);
        booking1.setEnd(time3);
        addBooking(booking1, booker1.getId());

        BookingDtoRequest booking2 = makeDefaultBookingDtoRequest(itemId);
        booking2.setStart(time4);
        booking2.setEnd(time6);
        addBooking(booking2, booker2.getId());

        BookingDtoRequest booking3 = makeDefaultBookingDtoRequest(itemId);
        booking3.setStart(time2);
        booking3.setEnd(time5);
        MockHttpServletResponse servletResponse = mockMvc.perform(
                        post(defaultUri)
                                .content(objectMapper.writeValueAsString(booking3))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(booker3.getId())))
                .andReturn().getResponse();

        assertEquals(HttpStatus.CONFLICT.value(), servletResponse.getStatus());
    }

    @Test
    public void shouldBeExceptionForBookingUnavailableItem() throws Exception {
        UserDto user = addDefaultUser("email@mail.ru");
        UserDto booker = addDefaultUser("newEmail@mail.ru");

        ItemDto itemDto = ItemDto.builder()
                .name("Item name")
                .description("Description item")
                .available(false)
                .build();
        MockHttpServletResponse servletResponse = mockMvc.perform(post(String.format("http://localhost:%d/items", port))
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(getDefaultHeader(user.getId())))
                .andReturn().getResponse();
        itemDto = objectMapper.readValue(servletResponse.getContentAsString(), ItemDto.class);

        BookingDtoRequest booking = makeDefaultBookingDtoRequest(itemDto.getId());
        servletResponse = mockMvc.perform(
                        post(defaultUri)
                                .content(objectMapper.writeValueAsString(booking))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(booker.getId())))
                .andReturn().getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), servletResponse.getStatus());
    }

    @Test
    public void shouldBeExceptionForGetNotFoundBooking() throws Exception {
        UserDto userDto = addDefaultUser("email@mail.ru");

        MockHttpServletResponse servletResponse = mockMvc.perform(
                        get(defaultUri + "/0")
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(userDto.getId())))
                .andReturn().getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), servletResponse.getStatus());
    }

    @Test
    public void getBookingsWithBookerOrOwnerAndStatusTest() throws Exception {
        UserDto user = addDefaultUser("email@mail.ru");
        long userId = user.getId();
        ItemDto item1 = addDefaultItem(userId);
        ItemDto item2 = addDefaultItem(userId);
        ItemDto item3 = addDefaultItem(userId);

        UserDto booker = addDefaultUser("newEmail@mail.ru");
        long bookerId = booker.getId();

        BookingDtoRequest bookingDtoRequest = makeDefaultBookingDtoRequest(item1.getId());
        bookingDtoRequest.setStart(bookingDtoRequest.getStart().plusMinutes(1));
        bookingDtoRequest.setEnd(bookingDtoRequest.getEnd().plusMinutes(1));
        BookingDto bookingDto = addBooking(bookingDtoRequest, bookerId);
        assertEquals(List.of(bookingDto),
                getBookingsByBookerAndStatus(bookerId, BookingStatus.WAITING.toString()));
        assertEquals(List.of(bookingDto),
                getBookingsOwnerAndStatus(userId, BookingStatus.WAITING.toString()));

        BookingDtoRequest rejectedBookingDtoRequest = makeDefaultBookingDtoRequest(item2.getId());
        BookingDto rejectedBookingDto = addBooking(rejectedBookingDtoRequest, bookerId);
        rejectedBookingDto = setApproved(userId, rejectedBookingDto.getId(), false);
        assertEquals(List.of(rejectedBookingDto),
                getBookingsByBookerAndStatus(bookerId, BookingStatus.REJECTED.toString()));
        assertEquals(List.of(rejectedBookingDto),
                getBookingsOwnerAndStatus(userId, BookingStatus.REJECTED.toString()));

        BookingDtoRequest futureBookingDtoRequest = makeDefaultBookingDtoRequest(item3.getId());
        futureBookingDtoRequest.setStart(futureBookingDtoRequest.getStart().plusDays(1));
        futureBookingDtoRequest.setEnd(futureBookingDtoRequest.getEnd().plusDays(1));
        BookingDto futureBookingDto = addBooking(futureBookingDtoRequest, bookerId);
        futureBookingDto = setApproved(userId, futureBookingDto.getId(), true);
        assertEquals(List.of(futureBookingDto, bookingDto, rejectedBookingDto),
                getBookingsByBookerAndStatus(bookerId, BookingStatus.FUTURE.toString()));
        assertEquals(List.of(futureBookingDto, bookingDto, rejectedBookingDto),
                getBookingsOwnerAndStatus(userId, BookingStatus.FUTURE.toString()));

        assertEquals(List.of(futureBookingDto, bookingDto, rejectedBookingDto),
                getBookingsOwnerAndStatus(
                        userId, BookingStatus.ALL.toString()));
        assertEquals(List.of(futureBookingDto, bookingDto, rejectedBookingDto),
                getBookingsOwnerAndStatus(
                        userId, BookingStatus.ALL.toString()));
    }

    @Test
    public void setApprovalTest() throws Exception {
        UserDto user = addDefaultUser("email@mail.ru");
        ItemDto item = addDefaultItem(user.getId());
        UserDto booker = addDefaultUser("newEmail@mail.ru");

        BookingDtoRequest bookingDtoRequest = makeDefaultBookingDtoRequest(item.getId());
        BookingDto bookingDto = addBooking(bookingDtoRequest, booker.getId());
        setApproved(user.getId(), bookingDto.getId(), true);

        MockHttpServletResponse servletResponse = mockMvc.perform(
                        get(defaultUri + "/" + bookingDto.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(booker.getId())))
                .andReturn().getResponse();

        assertEquals(BookingStatus.APPROVED,
                objectMapper.readValue(servletResponse.getContentAsString(), BookingDto.class).getStatus());
    }
}
