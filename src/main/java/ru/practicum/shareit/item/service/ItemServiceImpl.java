package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.service.ActualItemBooking.LAST;
import static ru.practicum.shareit.item.service.ActualItemBooking.NEXT;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final ItemRequestService itemRequestService;

    @Override
    public ItemDto addItemDto(ItemDto itemDto, long ownerId) {
        Item item;
        ItemRequest request;
        Long requestId = itemDto.getRequestId();

        if (requestId != null) {
            request = itemRequestService.getRequest(requestId);
        } else {
            request = null;
        }

        item = itemMapper.mapToItemModel(itemDto, userService.getUserById(ownerId), request);
        item.setId(null);
        item = itemRepository.save(item);

        log.debug("Добавлена вещь: {}", item);
        return itemMapper.mapToItemDto(item, null, null);
    }

    @Override
    @Transactional
    public ItemDto getItemDto(long id, long requesterId) {
        Item item = this.getItem(id);
        Map<ActualItemBooking, BookingDtoShort> lastAndNextBooking =
                bookingService.getLastAndNextBookingByItem(item, requesterId);

        return itemMapper.mapToItemDto(
                item,
                lastAndNextBooking.get(LAST),
                lastAndNextBooking.get(NEXT));
    }

    @Override
    public Item getItem(long itemId) {
        Optional<Item> optionalItem = itemRepository.findById(itemId);

        if (optionalItem.isEmpty()) {
            throw new NotFoundException(String.format("Ошибка получения: item с id=%d не найдена", itemId));
        }

        return optionalItem.get();
    }

    @Override
    public Collection<ItemDto> getOwnerItems(long ownerId) {

        return itemRepository.findAllByOwnerId(ownerId).stream()
                .map(item -> {
                    Map<ActualItemBooking, BookingDtoShort> itemDtoBookingsMap =
                            bookingService.getLastAndNextBookingByItem(item, ownerId);
                    return itemMapper.mapToItemDto(item, itemDtoBookingsMap.get(LAST), itemDtoBookingsMap.get(NEXT));
                })
                .sorted(Comparator.comparing(ItemDto::getId))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> findByNameOrDescription(
            long userId, String text,  int startingIndex, Integer collectionSize) {
        if (collectionSize == null) {
            collectionSize = Integer.MAX_VALUE;
        }

        if (!text.isEmpty()) {
            return itemRepository.searchAvailableItemsByNameAndDescription(
                    text, Pageable.ofSize(startingIndex + collectionSize)).stream()
                    .map(item -> {
                        Map<ActualItemBooking, BookingDtoShort> itemDtoBookingsMap = bookingService.getLastAndNextBookingByItem(item, userId);
                        return itemMapper.mapToItemDto(item, itemDtoBookingsMap.get(LAST), itemDtoBookingsMap.get(NEXT));
                    })
                    .sorted(Comparator.comparing(ItemDto::getId))
                    .collect(Collectors.toList());
        } else return List.of();
    }

    @Override
    @Transactional
    public ItemDto updateItemDto(ItemDto itemDto, long itemId, long ownerId) {
        Map<ItemUpdatedFields, Boolean> targetFields = new HashMap<>();
        boolean empty = true;
        Item item;
        Map<ActualItemBooking, BookingDtoShort> itemDtoBookingsMap;

        if (itemDto.getName() != null) {
            targetFields.put(ItemUpdatedFields.NAME, true);
            empty = false;
        } else {
            targetFields.put(ItemUpdatedFields.NAME, false);
        }

        if (itemDto.getDescription() != null) {
            targetFields.put(ItemUpdatedFields.DESCRIPTION, true);
            empty = false;
        } else {
            targetFields.put(ItemUpdatedFields.DESCRIPTION, false);
        }

        if (itemDto.getAvailable() != null) {
            targetFields.put(ItemUpdatedFields.AVAILABLE, true);
            empty = false;
        } else {
            targetFields.put(ItemUpdatedFields.AVAILABLE, false);
        }

        if (empty) {
            throw new ValidationException("Ошибка обновления вещи: поля запроса равны null");
        }

        item = itemMapper.mapToItemModel(itemDto, userService.getUserById(ownerId), null);
        item.setId(itemId);
        item = itemRepository.updateItem(item, targetFields);
        itemDtoBookingsMap = bookingService.getLastAndNextBookingByItem(item, ownerId);

        log.debug("Обновлена вещь: {}", item);
        return itemMapper.mapToItemDto(item, itemDtoBookingsMap.get(LAST), itemDtoBookingsMap.get(NEXT));
    }

    @Override
    @Transactional
    public CommentDto addCommentDto(CommentDto commentDto, long authorId, long itemId) {
        if (!bookingService.commentMadeAfterBooking(authorId, itemId)) {
            throw new ValidationException(String.format("Ошибка добавления комментария: " +
                    "пользователь с id=%d не оформлял бронирование вещи с id=%d.", authorId, itemId));
        }

        Comment comment = commentMapper.mapToModel(
                commentDto, userService.getUserById(authorId), this.getItem(itemId));
        comment.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        comment = commentRepository.save(comment);

        log.debug("Добавлен комментарий: {}", comment);
        return commentMapper.mapToDto(comment);
    }
}
