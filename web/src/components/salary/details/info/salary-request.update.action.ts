import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import salaryService, {SalaryIncreaseRequest} from "@/components/salary/salary.service";
import {DeleteAction} from "@/components/shared/table/TableComponentDataContainer";

export class SalaryRequestDeleteAction extends InDialogActionDataContainer<number, void> implements DeleteAction<SalaryIncreaseRequest> {

    constructor() {
        super((itemId, item) => this.deleteItemRequest([itemId!]));
    }


    deleteItemRequest(ids: number[]): Promise<Array<any>> {
        return salaryService.deleteSalaryRequest(ids);
    }


}