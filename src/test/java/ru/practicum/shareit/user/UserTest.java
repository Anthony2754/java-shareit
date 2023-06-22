package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
class UserTest {
    private final User userOne = User.builder()
            .id(1L)
            .name("User")
            .email("email@mail.ru")
            .build();
    private final User userTwo = User.builder()
            .id(1L)
            .name("User")
            .email("email@mail.ru")
            .build();

    @Test
    void equalsTo() {
        assertNotEquals(null, userOne);
        assertEquals(userOne, userOne);
        assertEquals(userOne, userTwo);
    }
}
