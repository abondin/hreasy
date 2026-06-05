package ru.abondin.hreasy.platform.service.admin.dto;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.employee.admin.UserSecurityInfoEntry;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface UserSecurityInfoMapper extends MapperBase {


    @Mapping(target = "employee", source = ".", qualifiedByName = "employeeDict")
    UserSecurityInfoDto fromEntry(UserSecurityInfoEntry entry);



    @Named("employeeDict")
    default SimpleDictDto employee(UserSecurityInfoEntry entry) {
        return simpleDto(entry.getId(), entry.getDisplayName());
    }

}
