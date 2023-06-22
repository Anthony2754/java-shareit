package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AllArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ItemRequestServiceTest {

    private UserService userService;
    private ItemRequestService requestService;

    private UserDto makeDefaultUser() {
        return UserDto.builder()
                .name("User Name")
                .email("email@mail.ru")
                .build();
    }

    private ItemRequestDto makeDefaultRequest() {
        return ItemRequestDto.builder()
                .description("Description request")
                .build();
    }

    @Test
    public void addRequestTest() {
        UserDto userDto = makeDefaultUser();
        userDto = userService.addUserDto(userDto);

        ItemRequestDto requestDto = makeDefaultRequest();
        requestDto = requestService.addRequest(requestDto, userDto.getId());
        requestDto.setCreated(requestDto.getCreated().truncatedTo(ChronoUnit.MICROS));
        requestDto.setItems(Set.of());


        assertEquals(requestDto, requestService.getRequestDto(requestDto.getId(), userDto.getId()));
    }

    @Test
    public void shouldBeExceptionForAddRequestFromNotFoundUser() {
        ItemRequestDto requestDto = makeDefaultRequest();
        assertThrows(NotFoundException.class, () -> requestService.addRequest(requestDto, 0));
    }

    @Test
    public void shouldBeExceptionForGetRequestFromNotFoundUser() {
        assertThrows(NotFoundException.class, () -> requestService.getRequestDto(0, 0));
    }

    @Test
    public void shouldBeExceptionForGetNotFoundRequest() {
        assertThrows(NotFoundException.class, () -> requestService.getRequest(0));
    }

    @Test
    public void getOwnItemRequestsTest() {
        UserDto userDto = makeDefaultUser();
        userDto = userService.addUserDto(userDto);

        UserDto userDto2 = makeDefaultUser();
        userDto2.setEmail("newEmail@mail.ru");
        userDto2 = userService.addUserDto(userDto2);

        ItemRequestDto requestDto = makeDefaultRequest();
        requestDto = requestService.addRequest(requestDto, userDto.getId());
        requestDto.setItems(Set.of());
        requestDto.setCreated(requestDto.getCreated().truncatedTo(ChronoUnit.MICROS));

        ItemRequestDto requestDto2 = makeDefaultRequest();
        requestService.addRequest(requestDto2, userDto2.getId());

        assertEquals(List.of(requestDto), requestService.getOwnItemRequests(userDto.getId()));
    }

    @Test
    public void shouldBeExceptionForGetOwnerItemRequestsFromNotFoundUser() {
        assertThrows(NotFoundException.class, () -> requestService.getOwnItemRequests(0));
    }

    @Test
    public void getOtherUsersRequestsTest() {
        UserDto userDto = makeDefaultUser();
        userDto = userService.addUserDto(userDto);

        UserDto userDto2 = makeDefaultUser();
        userDto2.setEmail("newEmail@mail.ru");
        userDto2 = userService.addUserDto(userDto2);

        ItemRequestDto requestDto = makeDefaultRequest();
        requestDto = requestService.addRequest(requestDto, userDto.getId());
        requestDto.setItems(Set.of());
        requestDto.setCreated(requestDto.getCreated().truncatedTo(ChronoUnit.MICROS));

        ItemRequestDto requestDto2 = makeDefaultRequest();
        requestService.addRequest(requestDto2, userDto2.getId());

        assertEquals(List.of(requestDto),
                requestService.getOtherUsersRequests(userDto2.getId(), 0, Integer.MAX_VALUE));
    }

    @Test
    public void shouldBeExceptionForGetOtherUsersRequestFromNotFoundUser() {
        assertThrows(NotFoundException.class,
                () -> requestService.getOtherUsersRequests(0, 0, Integer.MAX_VALUE));
    }
}
