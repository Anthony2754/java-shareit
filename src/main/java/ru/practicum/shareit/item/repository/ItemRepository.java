package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

@Repository
public interface ItemRepository extends PagingAndSortingRepository<Item, Long>, CustomItemRepository {

    Collection<Item> findAllByOwnerId(Long id);

    @Query(value = "SELECT i FROM Item i " +
            "WHERE (LOWER(i.name) LIKE CONCAT('%', LOWER(?1), '%')" +
            "OR LOWER(i.description) LIKE CONCAT('%', LOWER(?1), '%'))" +
            " AND i.available = TRUE"
    )
    Collection<Item> searchAvailableItemsByNameAndDescription(String query);

    @Transactional
    void deleteAllByOwner(User owner);

    boolean existsItemByIdAndAvailableIsTrue(long itemId);
}
