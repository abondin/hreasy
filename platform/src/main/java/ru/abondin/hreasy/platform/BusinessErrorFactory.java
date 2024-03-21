package ru.abondin.hreasy.platform;

import reactor.core.publisher.Mono;

/**
 *
 * Simple factory to simplify most common errors reation
 *
 * @author Alexander Bondin
 */
public class BusinessErrorFactory {
    private BusinessErrorFactory() {

    }
    public static <T> Mono<T> entityNotFound  (int id) {
        return Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(id)));
    }
}
