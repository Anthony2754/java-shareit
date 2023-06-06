package ru.practicum.shareit.item.repository;

import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.WrongOwnerItemException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemUpdatedFields;

import java.util.Map;
import java.util.Optional;

@Transactional
public class ItemRepositoryImpl implements CustomItemRepository {

    private final ItemRepository repository;

    public ItemRepositoryImpl(@Lazy ItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public Item updateItem(Item item, Map<ItemUpdatedFields, Boolean> targetFields) {
        long itemId = item.getId();
        long ownerId = item.getOwner().getId();
        Optional<Item> itemOptional = repository.findById(itemId);

        if (itemOptional.isPresent()) {
            Item itemExisting = itemOptional.get();
            if (itemExisting.getOwner().getId() != ownerId) {
                throw new WrongOwnerItemException(String.format("Ошибка: запрос на обновление вещи с id=%d" +
                        " исходит от пользователя, не являющегося ее владельцем.", itemId));
            }

            String name = item.getName();
            String description = item.getDescription();
            Boolean available = item.getAvailable();
            Item newItem;

            if (!targetFields.get(ItemUpdatedFields.NAME)) {
                name = itemExisting.getName();
            }
            if (!targetFields.get(ItemUpdatedFields.DESCRIPTION)) {
                description = itemExisting.getDescription();
            }
            if (!targetFields.get(ItemUpdatedFields.AVAILABLE)) {
                available = itemExisting.getAvailable();
            }

            newItem = Item.builder()
                    .id(itemId)
                    .owner(itemExisting.getOwner())
                    .name(name)
                    .description(description)
                    .available(available)
                    .build();

            return repository.save(newItem);

        } else throw new NotFoundException(String.format("Ошибка обновления: вещь с id=%d не найдена.", itemId));
    }
}
