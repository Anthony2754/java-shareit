package ru.practicum.shareit.exception;

public class WrongOwnerItemException extends RuntimeException {
    public WrongOwnerItemException(String message) {
        super(message);
    }
}
