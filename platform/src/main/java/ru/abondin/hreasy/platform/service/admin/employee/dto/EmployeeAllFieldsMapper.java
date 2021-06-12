package ru.abondin.hreasy.platform.service.admin.employee.dto;

import org.mapstruct.Mapper;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsEntry;

@Mapper(componentModel = "spring")
public interface EmployeeAllFieldsMapper {

    EmployeeWithAllDetailsEntry fromCreateOrUpdate(CreateOrUpdateEmployeeBody dto);
}
