package ru.abondin.hreasy.platform.service.admin.employee.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(of = {"email"})
public class CreateOrUpdateEmployeeBody extends EmployeeAllFields {

}
