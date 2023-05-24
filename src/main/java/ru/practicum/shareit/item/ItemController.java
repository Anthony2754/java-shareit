package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.shareit.log.Logger.logRequests;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto addItem(@RequestHeader(USER_ID) long userId, @Valid @RequestBody ItemDto itemDto) {
        logRequests(HttpMethod.POST, "/items", "X-Sharer-User-Id-" + userId, itemDto.toString());
        return itemService.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_ID) long userId, @PathVariable long itemId,
                              @Valid @RequestBody ItemDto itemDto) {
        logRequests(HttpMethod.PATCH, "/items/" + itemId, "X-Sharer-User-Id-" + userId, itemDto.toString());
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader(name = USER_ID) long userId, @PathVariable long itemId) {
        logRequests(HttpMethod.GET, "/items/" + itemId, "X-Sharer-User-Id-" + userId, "no");
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getAllItems(@RequestHeader(USER_ID) long userId) {
        logRequests(HttpMethod.GET, "/items", "X-Sharer-User-Id-" + userId, "no");
        return itemService.getAllItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> findByNameOrDescription(@RequestHeader(name = USER_ID) long userId,
                                                 @RequestParam String text) {
        logRequests(HttpMethod.GET, "items/search?text=" + text, "X-Sharer-User-Id-" + userId, "no");
        return itemService.findByNameOrDescription(userId, text);
    }
}
