package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
@Slf4j
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping
    @Validated(value = UserValidation.FullValidation.class)
    ResponseEntity<UserDto> addUser(@RequestBody @Valid UserDto userDto) {
        return new ResponseEntity<>(userService.addUserDto(userDto), HttpStatus.CREATED);
    }

    @GetMapping(path = "/{id}")
    ResponseEntity<UserDto> getUserById(@PathVariable long id) {
        return ResponseEntity.ok(userService.getUserByIdDto(id));
    }

    @GetMapping
    ResponseEntity<Collection<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsersDto());
    }

    @PatchMapping(path = "/{id}")
    @Validated(value = UserValidation.PartialValidation.class)
    ResponseEntity<UserDto> updateUser(@RequestBody @Valid UserDto userDto, @PathVariable long id) {
        return ResponseEntity.ok(userService.updateUserDto(userDto, id));
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    void deleteUserById(@PathVariable long id) {
        userService.deleteUserById(id);
    }
}
