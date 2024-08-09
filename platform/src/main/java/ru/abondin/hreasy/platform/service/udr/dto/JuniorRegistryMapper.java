package ru.abondin.hreasy.platform.service.udr.dto;

import io.r2dbc.postgresql.codec.Json;
import org.mapstruct.*;
import ru.abondin.hreasy.platform.config.udr.UdrProps;
import ru.abondin.hreasy.platform.repo.udr.JuniorEntry;
import ru.abondin.hreasy.platform.repo.udr.JuniorReportEntry;
import ru.abondin.hreasy.platform.repo.udr.JuniorView;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectDictDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.dto.ValueWithStatus;
import ru.abondin.hreasy.platform.service.mapper.MapperBaseWithJsonSupport;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class JuniorRegistryMapper extends MapperBaseWithJsonSupport {
    @Mapping(source = ".", target = "budgetingAccount", qualifiedByName = "budgetingAccount")
    @Mapping(source = ".", target = "currentProject", qualifiedByName = "currentProject")
    @Mapping(source = ".", target = "juniorEmpl", qualifiedByName = "junior")
    @Mapping(source = ".", target = "mentor", qualifiedByName = "mentor")
    @Mapping(source = ".", target = "createdBy", qualifiedByName = "createdBy")
    @Mapping(source = ".", target = "graduation", qualifiedByName = "graduation")
    @Mapping(source = "view.reports", target = "reports", qualifiedByName = "reports")
    @Mapping(target = "monthsWithoutReport", ignore = true)
    @Mapping(target = "juniorInCompanyMonths", ignore = true)
    public abstract JuniorDto toDto(JuniorView view, @Context OffsetDateTime now, @Context UdrProps props);

    @AfterMapping
    protected void calculateMonths(@MappingTarget JuniorDto dto, @Context OffsetDateTime now, @Context UdrProps props) {
        var nowDate = now.toLocalDate();
        var juniorInCompanyMonthsValue = ChronoUnit.MONTHS.between(dto.getJuniorDateOfEmployment(), nowDate);
        dto.setJuniorInCompanyMonths(
                dto.getGraduation() == null ?
                        ValueWithStatus.value(
                                juniorInCompanyMonthsValue,
                                props.getMonthsInCompanyThresholds()
                        ) : ValueWithStatus.ok(juniorInCompanyMonthsValue)
        );
        var lastReportDate = dto.getReports().stream()
                .map(JuniorReportDto::getCreatedAt)
                .map(OffsetDateTime::toLocalDate)
                .sorted(Comparator.reverseOrder())
                .findFirst()
                .orElse(dto.getJuniorDateOfEmployment());
        var monthsWithoutReportValue = ChronoUnit.MONTHS.between(lastReportDate, nowDate);
        dto.setMonthsWithoutReport(
                dto.getGraduation() == null ?
                        ValueWithStatus.value(
                                monthsWithoutReportValue,
                                props.getMonthsWithoutReportThresholds()
                        ) : ValueWithStatus.ok(monthsWithoutReportValue)
        );
    }

    @Mapping(source = "body.juniorEmplId", target = "juniorEmplId")
    @Mapping(source = "body", target = ".")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "now", target = "createdAt")
    @Mapping(target = "graduatedAt", ignore = true)
    @Mapping(target = "graduatedBy", ignore = true)
    @Mapping(target = "graduatedComment", ignore = true)
    public abstract JuniorEntry toEntry(AddToJuniorRegistryBody body, Integer createdBy, OffsetDateTime now);

    public abstract void apply(@MappingTarget JuniorEntry entry, UpdateJuniorRegistryBody body);

    @Mapping(source = "body.comment", target = "graduatedComment")
    @Mapping(source = "now", target = "graduatedAt")
    @Mapping(source = "employeeId", target = "graduatedBy")
    public abstract void apply(@MappingTarget JuniorEntry entry, GraduateJuniorBody body, int employeeId, OffsetDateTime now);

    @Mapping(source = "body", target = ".")
    @Mapping(source = "registryId", target = "juniorId")
    @Mapping(source = "employeeId", target = "createdBy")
    @Mapping(source = "now", target = "createdAt")
    @Mapping(source = "body.ratings", target = "ratings", qualifiedByName = "ratingsToJson")
    public abstract JuniorReportEntry toReportEntry(AddJuniorReportBody body, Integer registryId, Integer employeeId, OffsetDateTime now);

    @Mapping(source = "body", target = ".")
    @Mapping(source = "body.ratings", target = "ratings", qualifiedByName = "ratingsToJson")
    public abstract void applyReportUpdate(@MappingTarget JuniorReportEntry entry, UpdateJuniorReportBody body);

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
        return simpleDto(view.getJuniorEmplId(), view.getJuniorEmplDisplayName());
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

    @Named("ratingsToJson")
    protected Json ratingsToJson(JuniorReportRatings ratings) {
        return Json.of(toJsonStr(ratings, false));
    }


    private SimpleDictDto graduatedBy(JuniorView view) {
        return simpleDto(view.getGraduatedBy(), view.getGraduatedByDisplayName());
    }


}
