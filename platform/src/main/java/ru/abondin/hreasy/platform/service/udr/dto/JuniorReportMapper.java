package ru.abondin.hreasy.platform.service.udr.dto;

import io.r2dbc.postgresql.codec.Json;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.repo.udr.JuniorEntry;
import ru.abondin.hreasy.platform.repo.udr.JuniorView;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectDictDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBaseWithJsonSupport;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class JuniorReportMapper extends MapperBaseWithJsonSupport {
    @Mapping(source = ".", target = "budgetingAccount", qualifiedByName = "budgetingAccount")
    @Mapping(source = ".", target = "currentProject", qualifiedByName = "currentProject")
    @Mapping(source = ".", target = "junior", qualifiedByName = "junior")
    @Mapping(source = ".", target = "mentor", qualifiedByName = "mentor")
    @Mapping(source = ".", target = "createdBy", qualifiedByName = "createdBy")
    @Mapping(source = ".", target = "graduation", qualifiedByName = "graduation")
    @Mapping(source = "reports", target = "reports", qualifiedByName = "reports")
    public abstract JuniorDto toDto(JuniorView view);

    @Mapping(source = "juniorEmployeeId", target = "juniorId")
    @Mapping(source = "body", target = ".")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "now", target = "createdAt")
    @Mapping(target = "new", constant = "true")
    @Mapping(target = "graduatedAt", ignore = true)
    @Mapping(target = "graduatedBy", ignore = true)
    @Mapping(target = "graduatedComment", ignore = true)
    public abstract JuniorEntry toEntry(int juniorEmployeeId, AddToJuniorRegistryBody body, Integer createdBy, OffsetDateTime now);

    @Named("budgetingAccount")
    protected SimpleDictDto budgetingAccount(JuniorView view) {
        return simpleDto(view.getBudgetingAccount(), view.getBudgetingAccountName());
    }

    @Named("currentProject")
    protected CurrentProjectDictDto currentProject(JuniorView view) {
        return currentProjectDto(view.getCurrentProjectId(), view.getCurrentProjectName(), view.getCurrentProjectRole());
    }

    @Named("junior")
    protected SimpleDictDto junior(JuniorView view) {
        return simpleDto(view.getJuniorId(), view.getJuniorDisplayName());
    }

    @Named("mentor")
    protected SimpleDictDto mentor(JuniorView view) {
        return simpleDto(view.getMentorId(), view.getMentorDisplayName());
    }

    @Named("createdBy")
    protected SimpleDictDto createdBy(JuniorView view) {
        return simpleDto(view.getCreatedBy(), view.getCreatedByDisplayName());
    }

    @Named("graduation")
    protected JuniorDto.Graduation graduation(JuniorView view) {
        return view.getGraduatedAt() == null ? null :
                new JuniorDto.Graduation(view.getGraduatedAt(), graduatedBy(view), view.getGraduatedComment());
    }

    @Named("reports")
    protected List<JuniorReportDto> reports(Json json) {
        return listFromJson(json, JuniorReportDto.class);
    }

    private SimpleDictDto graduatedBy(JuniorView view) {
        return simpleDto(view.getGraduatedBy(), view.getGraduatedByDisplayName());
    }


}
