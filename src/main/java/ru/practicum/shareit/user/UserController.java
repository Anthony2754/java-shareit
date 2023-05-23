package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.shareit.log.Logger.logRequests;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto addUser(@Valid @RequestBody UserDto userDto) {
        logRequests(HttpMethod.POST, "/users", "no", userDto.toString());
        return userService.addUser(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable long id, @Valid @RequestBody UserDto userDto) {
        logRequests(HttpMethod.PATCH, "/users/" + id, "no", userDto.toString());
        return userService.updateUser(id, userDto);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable long id) {
        logRequests(HttpMethod.GET, "/users/" + id, "no", "no");
        return userService.getUserById(id);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        logRequests(HttpMethod.GET, "/users", "no", "no");
        return userService.getAllUsers();
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable long id) {
        logRequests(HttpMethod.DELETE, "/users/" + id, "no", "no");
        userService.deleteUserById(id);
    }
}
