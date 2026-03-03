import salaryService, {EmployeeWithLatestSalaryRequest} from "@/components/salary/salary.service";
import TableComponentDataContainer, {
    CreateBody,
    UpdateBody
} from "@/components/shared/table/TableComponentDataContainer";
import {DataTableHeader} from "vuetify";
import {
    EmployeeWithLatestSalaryRequestFilter
} from "@/components/salary/overview/EmployeeWithLatestSalaryRequestFilterComponent.vue";


export class EmployeeWithLatestSalaryRequestDataContainer extends TableComponentDataContainer<EmployeeWithLatestSalaryRequest, UpdateBody, CreateBody, EmployeeWithLatestSalaryRequestFilter> {

    public constructor(headerLoader: () => DataTableHeader[]) {
        super(
            () => salaryService.getEmployeesWithLatestSalaryRequest(),
            headerLoader,
            null,
            null,
            null,
            new EmployeeWithLatestSalaryRequestFilter(),
            () => false,
            true
        )
    }


}
