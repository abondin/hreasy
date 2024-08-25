import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import salaryService, {SalaryIncreaseRequest} from "@/components/salary/salary.service";
import {DeleteAction} from "@/components/shared/table/TableComponentDataContainer";

export class SalaryRequestDeleteAction extends InDialogActionDataContainer<number, void> implements DeleteAction<SalaryIncreaseRequest> {

    constructor() {
        super((itemId, item) => this.deleteItemRequest([itemId!]));
    }


    deleteItemRequest(ids: number[]): Promise<any> {
        if (ids.length === 0) {
            return Promise.resolve([]);
        } else if (ids.length === 1) {
            return salaryService.deleteSalaryRequest(ids[0]);
        } else {
            return Promise.all(ids.map(i => salaryService.deleteSalaryRequest(i)));
        }
    }


}