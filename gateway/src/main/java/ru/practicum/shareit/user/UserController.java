package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/users")
@Slf4j
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserClient userClient;

    @PostMapping
    @Validated(value = UserValidation.FullValidation.class)
    public ResponseEntity<Object> addUser(@RequestBody @Valid UserDto userDto) {
        return userClient.addUser(userDto);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable Long id) {
        return userClient.getUser(id);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        return userClient.getUsers();
    }

    @PatchMapping(path = "/{id}")
    @Validated(value = UserValidation.PartialValidation.class)
    public ResponseEntity<Object> updateUser(@RequestBody @Valid UserDto userDto, @PathVariable Long id) {
        return userClient.updateUser(userDto, id);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public void deleteUserById(@PathVariable Long id) {
        userClient.deleteUser(id);
    }
}