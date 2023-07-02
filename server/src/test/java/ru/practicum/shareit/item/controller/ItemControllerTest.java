package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
public class ItemControllerTest {

    @LocalServerPort
    private int port;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    private MockHttpServletResponse servletResponse;
    private ItemDto itemDto;
    private HttpHeaders headers;
    private final String defaultUri = String.format("http://localhost:%d/items", port);

    @BeforeEach
    public void beforeEach() {
        itemDto = makeDefaultItemDto();
        headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "1");
    }

    @Test
    public void addItemTest() throws Exception {
        addDefaultUser("email@mail.ru");

        MockHttpServletResponse servletResponse = mvc.perform(
                        post(defaultUri)
                                .headers(headers)
                                .content(mapper.writeValueAsString(itemDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(HttpStatus.CREATED.value(), servletResponse.getStatus());
        itemDto.setComments(null);
        assertEquals(itemDto, mapper.readValue(servletResponse.getContentAsString(), ItemDto.class));
    }

    @Test
    public void shouldBeExceptionForAddItemWithoutOwner() throws Exception {
        MockHttpServletResponse servletResponse = mvc.perform(
                        post(defaultUri)
                                .headers(headers)
                                .content(mapper.writeValueAsString(itemDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), servletResponse.getStatus());
    }

    @Test
    public void shouldBeExceptionForAddCommentWithoutCurrentOrPastBooking() throws Exception {
        UserDto user = addDefaultUser("email@mail.ru");
        UserDto booker = addDefaultUser("newEmail@mail.ru");
        long userId = user.getId();
        long bookerId = booker.getId();

        ItemDto item = addItem(makeDefaultItemDto(), userId);
        long itemId = item.getId();

        BookingDto bookingDto = addBooking(makeDefaultBookingDtoRequest(itemId), bookerId);
        setApproved(userId, bookingDto.getId());

        CommentDto comment = CommentDto.builder().text("comment text").build();

        MockHttpServletResponse servletResponse = mvc.perform(
                        post(defaultUri + "/" + itemId + "/comment")
                                .content(mapper.writeValueAsString(comment))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(bookerId)))
                .andReturn().getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), servletResponse.getStatus());
    }

    @Test
    public void getItemTest() throws Exception {
        addDefaultUser("email@mail.ru");

        mvc.perform(
                post(defaultUri)
                        .headers(headers)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON));

        MockHttpServletResponse servletResponse = mvc.perform(
                        get(defaultUri + "/1")
                                .headers(headers)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.OK.value(), servletResponse.getStatus());
        assertEquals(itemDto, mapper.readValue(servletResponse.getContentAsString(), ItemDto.class));
    }

    @Test
    public void shouldBeExceptionNotFoundItem() throws Exception {
        addDefaultUser("email@mail.ru");

        MockHttpServletResponse servletResponse = mvc.perform(
                        get(defaultUri + "/1")
                                .headers(headers)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), servletResponse.getStatus());
    }

    @Test
    public void searchingAvailableItemsTest() throws Exception {
        addDefaultUser("email@mail.ru");

        ItemDto item1 = makeDefaultItemDto();
        item1.setName("name item1");
        item1.setDescription("description item1");
        item1.setAvailable(true);

        mvc.perform(
                post(defaultUri)
                        .headers(headers)
                        .content(mapper.writeValueAsString(item1))
                        .contentType(MediaType.APPLICATION_JSON));

        ItemDto item2 = makeDefaultItemDto();
        item2.setId(2L);
        item2.setName("name item2");
        item2.setDescription("description item2");
        item2.setAvailable(true);

        mvc.perform(
                post(defaultUri)
                        .headers(headers)
                        .content(mapper.writeValueAsString(item2))
                        .contentType(MediaType.APPLICATION_JSON));

        ItemDto item3 = makeDefaultItemDto();
        item3.setId(3L);
        item3.setName("name item3");
        item3.setDescription("description item3");
        item3.setAvailable(false);

        mvc.perform(
                post(defaultUri)
                        .headers(headers)
                        .content(mapper.writeValueAsString(item3))
                        .contentType(MediaType.APPLICATION_JSON));

        MockHttpServletResponse servletResponse = mvc.perform(
                        get(defaultUri + "/search")
                                .headers(headers)
                                .param("from", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("text", "item"))
                .andReturn().getResponse();

        assertEquals(List.of(item1, item2),
                mapper.readValue(servletResponse.getContentAsString(), new TypeReference<List<ItemDto>>() {
                }));
    }

    @Test
    public void updateItemTest() throws Exception {
        addDefaultUser("email@mail.ru");

        ItemDto item1 = makeDefaultItemDto();
        item1.setName("New item1");
        item1.setDescription("item description");
        item1.setAvailable(false);

        mvc.perform(
                post(defaultUri)
                        .headers(headers)
                        .content(mapper.writeValueAsString(item1))
                        .contentType(MediaType.APPLICATION_JSON));

        item1.setName(null);
        item1.setDescription("New item description");
        item1.setAvailable(true);

        MockHttpServletResponse servletResponse = mvc.perform(
                        patch(defaultUri + "/1")
                                .headers(headers)
                                .content(mapper.writeValueAsString(item1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        item1.setName("New item1");

        assertEquals(HttpStatus.OK.value(), servletResponse.getStatus());
        item1.setComments(null);
        assertEquals(item1, mapper.readValue(servletResponse.getContentAsString(), ItemDto.class));
    }

    @Test
    public void shouldBeExceptionForRequestWithNullValues() throws Exception {
        addDefaultUser("email@mail.ru");

        ItemDto item1 = makeDefaultItemDto();

        mvc.perform(
                post(defaultUri)
                        .headers(headers)
                        .content(mapper.writeValueAsString(item1))
                        .contentType(MediaType.APPLICATION_JSON));

        item1.setName(null);
        item1.setDescription(null);
        item1.setAvailable(null);

        MockHttpServletResponse servletResponse = mvc.perform(
                        patch(defaultUri + "/1")
                                .headers(headers)
                                .content(mapper.writeValueAsString(item1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), servletResponse.getStatus());
    }

    @Test
    public void shouldBeExceptionWithWrongOwnerUpdatingItem() throws Exception {
        UserDto userDto = addDefaultUser("email@mail.ru");
        userDto.setEmail("newEmail@mail.ru");
        mvc.perform(
                post(String.format("http://localhost:%d/users", port))
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON));

        ItemDto item1 = makeDefaultItemDto();
        item1.setName("item1");
        item1.setDescription("item description");
        item1.setAvailable(false);

        mvc.perform(
                post(defaultUri)
                        .headers(headers)
                        .content(mapper.writeValueAsString(item1))
                        .contentType(MediaType.APPLICATION_JSON));

        item1.setName(null);
        item1.setDescription("New item description");
        item1.setAvailable(true);

        headers.set("X-Sharer-User-Id", "2");

        MockHttpServletResponse servletResponse = mvc.perform(
                        patch(defaultUri + "/1")
                                .headers(headers)
                                .content(mapper.writeValueAsString(item1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.FORBIDDEN.value(), servletResponse.getStatus());
    }

    private HttpHeaders getDefaultHeader(Long ownerId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", ownerId.toString());

        return headers;
    }

    private void setApproved(long requesterId, long bookingId) throws Exception {
        MockHttpServletResponse servletResponse = mvc.perform(
                        patch(String.format("http://localhost:%d/bookings", port) + "/" + bookingId)
                                .headers(getDefaultHeader(requesterId))
                                .param("approved", ((Boolean) true).toString()))
                .andReturn().getResponse();
        mapper.readValue(servletResponse.getContentAsString(), BookingDto.class);
    }

    private BookingDto addBooking(BookingDtoRequest bookingDtoRequest, long bookerId) throws Exception {
        MockHttpServletResponse servletResponse = mvc.perform(
                        post(String.format("http://localhost:%d/bookings", port))
                                .content(mapper.writeValueAsString(bookingDtoRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(bookerId)))
                .andReturn().getResponse();
        return mapper.readValue(servletResponse.getContentAsString(), BookingDto.class);
    }

    private BookingDtoRequest makeDefaultBookingDtoRequest(long itemId) {
        return BookingDtoRequest.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS))
                .itemId(itemId)
                .build();
    }

    private ItemDto addItem(ItemDto itemDto, long ownerId) throws Exception {
        MockHttpServletResponse servletResponse = mvc.perform(
                        post(defaultUri)
                                .headers(getDefaultHeader(ownerId))
                                .content(mapper.writeValueAsString(itemDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        return mapper.readValue(servletResponse.getContentAsString(), ItemDto.class);
    }

    private ItemDto makeDefaultItemDto() {
        return ItemDto.builder()
                .id(1L)
                .name("Item name")
                .description("Item description")
                .available(true)
                .comments(List.of())
                .build();
    }

    private UserDto addDefaultUser(String email) throws Exception {

        UserDto userDto = UserDto.builder()
                .name("User Name")
                .email(email)
                .build();

        MockHttpServletResponse servletResponse = mvc.perform(
                        post(String.format("http://localhost:%d/users", port))
                                .content(mapper.writeValueAsString(userDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        return mapper.readValue(servletResponse.getContentAsString(), UserDto.class);
    }
}
