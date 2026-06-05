package ru.abondin.hreasy.platform.service.notification.dto;

import io.r2dbc.postgresql.codec.Json;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.notification.NotificationEntry;

@Mapper(componentModel = "spring")
public interface NotificationMapper {


    @Mapping(target = "context", source = "context", qualifiedByName = "notificationContext")
    NotificationDto reportToDto(NotificationEntry entry);

    @Named("notificationContext")
    default String notificationContext(Json context) {
        return context == null ? null : context.asString();
    }
}
