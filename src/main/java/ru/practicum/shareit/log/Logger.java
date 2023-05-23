package ru.practicum.shareit.log;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

@UtilityClass
@Slf4j
public class Logger {

    public static void logRequests(HttpMethod method, String uri, String headers, String body) {
        log.info("Получен запрос: {} {}. Заголовок запроса: {}. Тело запроса: {}", method, uri, headers, body);
    }

    public static void logChanges(String action, String object) {
        log.info("{}: {}", action, object);
    }

    public static void logExceptionWarning(Throwable e) {
        log.warn(e.getClass().getSimpleName(), e);
    }
}
