package ru.abondin.hreasy.platform.service.notification.upcomingvacations;

import org.mapstruct.*;
import ru.abondin.hreasy.platform.repo.vacation.VacationView;
import ru.abondin.hreasy.platform.repo.vacation.VacationViewWithManagers;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UpcomingVacationMapper extends MapperBase {

    @Mapping(target = "clientUuid", qualifiedByName = "uuid", source = ".")
    UpcomingVacationNotificationTemplate.UpcomingVacationContext toEmailContext(VacationViewWithManagers v);

    @Named("uuid")
    default String uuid(VacationView v) {
        return UUID.randomUUID().toString();
    }
}
