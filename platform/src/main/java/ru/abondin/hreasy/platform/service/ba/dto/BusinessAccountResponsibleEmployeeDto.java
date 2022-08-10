package ru.abondin.hreasy.platform.service.ba.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.ResponsibleEmployeeDto;

import java.util.List;

/**
 * <b color="red">WARNING:</b> Display Name of the employee does not update automatically when employee profile changing
 * </br>
 * To Update Employee Display Name you have to update whole {@link BusinessAccountDto#setResponsibleEmployees(List)}
 */
@Data
public class BusinessAccountResponsibleEmployeeDto extends ResponsibleEmployeeDto {
}
