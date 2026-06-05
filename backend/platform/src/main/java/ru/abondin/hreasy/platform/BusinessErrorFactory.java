package ru.abondin.hreasy.platform;

import reactor.core.publisher.Mono;

/**
 * Simple factory to simplify most common errors reation
 *
 * @author Alexander Bondin
 */
public class BusinessErrorFactory {
    private BusinessErrorFactory() {

    }

    /**
     * @deprecated use {@link #entityNotFound(String, int)}
     * @param id
     * @return
     * @param <T>
     */
    @Deprecated
    public static <T> Mono<T> entityNotFound(int id) {
        return entityNotFound(null, id);
    }
    public static <T> Mono<T> entityNotFound(String type, int id) {
        return entityNotFound(type, Integer.toString(id));
    }

    public static <T> Mono<T> entityNotFound(String type, String id) {
        return type ==null?
                Mono.error(new BusinessError("errors.entity.not.found", id))
                : Mono.error(new BusinessError("errors.entity_of_type.not.found", type, id));
    }

    public static <T> Mono<T> entityAlreadyExists(String entityType, int entityId) {
        return Mono.error(new BusinessError("errors.entity.already_exists", entityType, Integer.toString(entityId)));
    }
}
