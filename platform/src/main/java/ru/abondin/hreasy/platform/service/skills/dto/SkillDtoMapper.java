package ru.abondin.hreasy.platform.service.skills.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.employee.skills.SkillGroupEntry;
import ru.abondin.hreasy.platform.repo.employee.skills.SkillWithRatingEntry;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

@Mapper(componentModel = "spring")
public interface SkillDtoMapper extends MapperBase {

    @Mapping(source = "archived", target = "active")
    SimpleDictDto groupToDto(SkillGroupEntry entry);

    @Mapping(source = ".", target = "group", qualifiedByName = "skillGroupToSimpleDict")
    SkillDto skillWithRating(SkillWithRatingEntry skillEntry);


    @Named("skillGroupToSimpleDict")
    default SimpleDictDto skillGroupToSimpleDict(SkillWithRatingEntry skillEntry) {
        return simpleDto(skillEntry.getGroupId(), skillEntry.getGroupName());
    }

}
