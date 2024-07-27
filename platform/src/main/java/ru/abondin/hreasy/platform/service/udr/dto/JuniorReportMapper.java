package ru.abondin.hreasy.platform.service.udr.dto;

import io.r2dbc.postgresql.codec.Json;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.udr.JuniorView;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBaseWithJsonSupport;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class JuniorReportMapper extends MapperBaseWithJsonSupport {
    @Mapping(source = ".", target = "budgetingAccount", qualifiedByName = "budgetingAccount")
    @Mapping(source = ".", target = "junior", qualifiedByName = "junior")
    @Mapping(source = ".", target = "mentor", qualifiedByName = "mentor")
    @Mapping(source = ".", target = "createdBy", qualifiedByName = "createdBy")
    @Mapping(source = ".", target = "graduation", qualifiedByName = "graduation")
    @Mapping(source = "reports", target = "reports", qualifiedByName = "reports")
    public abstract JuniorDto toDto(JuniorView view);

    @Named("budgetingAccount")
    private SimpleDictDto budgetingAccount(JuniorView view) {
        return simpleDto(view.getBudgetingAccount(), view.getBudgetingAccountName());
    }

    @Named("junior")
    private SimpleDictDto junior(JuniorView view) {
        return simpleDto(view.getJuniorId(), view.getJuniorDisplayName());
    }

    @Named("mentor")
    private SimpleDictDto mentor(JuniorView view) {
        return simpleDto(view.getMentorId(), view.getMentorDisplayName());
    }

    @Named("createdBy")
    private SimpleDictDto createdBy(JuniorView view) {
        return simpleDto(view.getCreatedBy(), view.getCreatedByDisplayName());
    }

    @Named("graduation")
    private JuniorDto.Graduation graduation(JuniorView view) {
        return view.getGraduatedAt() == null ? null :
                new JuniorDto.Graduation(view.getGraduatedAt(), graduatedBy(view), view.getGraduatedComment());
    }

    @Named("reports")
    private List<JuniorReportDto> reports(Json json) {
        return listFromJson(json, JuniorReportDto.class);
    }

    private SimpleDictDto graduatedBy(JuniorView view) {
        return simpleDto(view.getGraduatedBy(), view.getGraduatedByDisplayName());
    }

}
