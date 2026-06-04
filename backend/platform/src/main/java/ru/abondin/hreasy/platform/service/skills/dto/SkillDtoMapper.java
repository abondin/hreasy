package ru.abondin.hreasy.platform.service.skills.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.employee.skills.SkillGroupEntry;
import ru.abondin.hreasy.platform.repo.employee.skills.SkillWithRatingEntry;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

@Mapper(componentModel = "spring")
public interface SkillDtoMapper extends MapperBase, RatingsMapper {

    @Mapping(source = "archived", target = "active")
    SimpleDictDto groupToDto(SkillGroupEntry entry);


    default SkillDto skillWithRatings(SkillWithRatingEntry skillEntry, int loggedInEmployeeId) {
        var result = baseFields(skillEntry);
        if (result != null) {
            result.setRatings(ratings(skillEntry.getRatings(), loggedInEmployeeId));
        }
        return result;
    }

    @Mapping(source = ".", target = "group", qualifiedByName = "skillGroupToSimpleDict")
    @Mapping(target = "ratings", ignore = true)
    SkillDto baseFields(SkillWithRatingEntry skillEntry);

    @Named("skillGroupToSimpleDict")
    default SimpleDictDto skillGroupToSimpleDict(SkillWithRatingEntry skillEntry) {
        return simpleDto(skillEntry.getGroupId(), skillEntry.getGroupName());
    }

}
