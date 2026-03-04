import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import salaryService, {SalaryIncreaseRequest, SalaryRequestLink} from "@/components/salary/salary.service";
import {DeleteAction} from "@/components/shared/table/TableComponentDataContainer";

export class SalaryRequestDeleteLinkAction extends InDialogActionDataContainer<number, void> implements DeleteAction<SalaryRequestLink> {

    constructor() {
        super((itemId, item) => this.deleteItemRequest([itemId!]));
    }


    deleteItemRequest(ids: number[]): Promise<any> {
        if (ids.length === 0) {
            return Promise.resolve([]);
        } else if (ids.length === 1) {
            return salaryService.deleteLink(ids[0]);
        } else {
            return Promise.all(ids.map(i => salaryService.deleteLink(i)));
        }
    }


}
