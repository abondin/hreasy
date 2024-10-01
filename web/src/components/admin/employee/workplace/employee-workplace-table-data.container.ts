import adminEmployeeService, {EmployeeWithAllDetails} from "@/components/admin/employee/admin.employee.service";
import TableComponentDataContainer, {
    CreateBody,
    Filter,
    UpdateBody
} from "@/components/shared/table/TableComponentDataContainer";
import {DataTableHeader} from "vuetify";

export class EmployeeWorkplaceTableFilter extends Filter<EmployeeWithAllDetails> {

}

export interface EmployeeWorkplaceAssignFormData extends UpdateBody, CreateBody {
}

export class EmployeeWorkplaceTableDataContainer extends TableComponentDataContainer<EmployeeWithAllDetails, EmployeeWorkplaceAssignFormData, EmployeeWorkplaceAssignFormData, EmployeeWorkplaceTableFilter> {


    constructor(_headerLoader: () => DataTableHeader[]) {
        super(dataLoader, _headerLoader, null, null, null, new EmployeeWorkplaceTableFilter(), false, true);
    }

    private dataLoader() {
        return adminEmployeeService.findAll(false);
    }
}
