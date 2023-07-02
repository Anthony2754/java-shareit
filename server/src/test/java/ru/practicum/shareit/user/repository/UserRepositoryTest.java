package ru.practicum.shareit.user.repository;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserUpdateFields;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AllArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserRepositoryTest {

    private UserRepository repository;

    @Test
    public void updateUserTest() {
        Map<UserUpdateFields, Boolean> targetFields = new HashMap<>();
        targetFields.put(UserUpdateFields.EMAIL, false);
        targetFields.put(UserUpdateFields.NAME, true);

        User user1 = makeDefaultUser();
        repository.save(user1);

        User user2 = makeDefaultUser();
        user2.setId(1L);
        user2.setEmail(null);
        user2.setName("New Name");
        repository.updateUser(user2, targetFields);

        User finishedUser = makeDefaultUser();
        finishedUser.setId(1L);
        finishedUser.setName("New Name");

        Optional<User> result = repository.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(finishedUser, result.get());
    }

    private User makeDefaultUser() {
        return User.builder()
                .name("User Name")
                .email("email@mail.ru")
                .build();
    }
}
