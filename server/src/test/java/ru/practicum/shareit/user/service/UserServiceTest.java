package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AllArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserServiceTest {

    private UserService service;

    @Test
    public void deleteUserTest() {
        UserDto user = service.addUserDto(makeDefaultUser());
        long userId = user.getId();
        assertEquals(user, service.getUserByIdDto(userId));

        service.deleteUserById(userId);
        assertTrue(service.getAllUsersDto().isEmpty());
    }

    @Test
    public void shouldBeExceptionForDuplicateEmail() {
        service.addUserDto(makeDefaultUser());

        assertThrows(DuplicateException.class, () -> service.addUserDto(makeDefaultUser()));
    }

    @Test
    public void shouldBeExceptionForNotFoundUser() {
        assertThrows(NotFoundException.class, () -> service.getUserByIdDto(1L));
    }

    @Test
    public void shouldBeExceptionForUpdateUserWithDuplicateEmail() {
        service.addUserDto(makeDefaultUser());
        UserDto user = makeDefaultUser();
        user.setEmail("newEmai@mail.ru");
        user = service.addUserDto(user);

        UserDto updatedUser = makeDefaultUser();
        updatedUser.setId(user.getId());

        UserDto finalUser = user;
        assertThrows(DuplicateException.class, () -> service.updateUserDto(updatedUser, finalUser.getId()));
    }

    @Test
    public void shouldBeExceptionForDeleteNonExistentUser() {
        assertThrows(NotFoundException.class, () -> service.deleteUserById(1L));
    }

    private UserDto makeDefaultUser() {
        return UserDto.builder()
                .name("User Name")
                .email("email@mail.ru")
                .build();
    }
}
