package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.dto.ItemMapper.toItem;
import static ru.practicum.shareit.item.dto.ItemMapper.toItemDto;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto addItem(long ownerId, ItemDto itemDto) {
        userService.getUserById(ownerId);
        Item item = toItem(ownerId, itemDto.getId(), itemDto);
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Заполните название");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Заполните описание");
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("Заполните статус аренды");
        }
        Item itemStorage = itemRepository.addItem(item);
        return toItemDto(itemStorage.getId(), itemStorage);
    }

    @Override
    public ItemDto updateItem(long ownerId, long itemId, ItemDto itemDto) {
        userService.getUserById(ownerId);
        Item item = toItem(ownerId, itemId, itemDto);
        Item itemStorage = Item.builder().build();
        if (item.getName() != null && !item.getName().isBlank()) {
            itemStorage = itemRepository.updateItemName(item);
        }
        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            itemStorage = itemRepository.updateItemDescription(item);
        }
        if (item.getAvailable() != null) {
            itemStorage = itemRepository.updateItemAvailable(item);
        }
        return toItemDto(itemStorage.getId(), itemStorage);
    }

    @Override
    public ItemDto getItemById(long ownerId, long itemId) {
        userService.getUserById(ownerId);
        Item itemStorage = itemRepository.getItemById(itemId);
        return toItemDto(itemStorage.getId(), itemStorage);
    }

    @Override
    public List<ItemDto> getAllItems(long ownerId) {
        userService.getUserById(ownerId);
        return itemRepository.getAllItems(ownerId).stream()
                .map(item -> toItemDto(item.getId(), item))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> findByNameOrDescription(long ownerId, String text) {
        userService.getUserById(ownerId);
        if (text != null && !text.isBlank()) {
            return itemRepository.findByNameOrDescription(text).stream()
                    .map(item -> toItemDto(item.getId(), item))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
