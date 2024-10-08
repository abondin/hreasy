import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import salaryService, {SalaryIncreaseRequest} from "@/components/salary/salary.service";
import {UpdateAction} from "@/components/shared/table/TableComponentDataContainer";

export interface SalaryRequestUpdateBody {
    budgetExpectedFundingUntil: string | null;
    //TODO After salary storing feature implemented populate this field automatically
    currentSalaryAmount: number | null;
    plannedSalaryAmount: number | null;

    //TODO After salary storing feature implemented populate this field automatically
    previousSalaryIncreaseText: string | null;
    previousSalaryIncreaseDate: string | null;

    assessmentId: number | null;
    comment: string | null;
    newPosition: number | null;
}

export class SalaryRequestUpdateAction extends InDialogActionDataContainer<number, SalaryRequestUpdateBody> implements UpdateAction<SalaryIncreaseRequest, SalaryRequestUpdateBody> {

    constructor() {
        super((itemId, item) => this.updateItemRequest(itemId!, item!));
    }

    itemToUpdateBody(item: SalaryIncreaseRequest): SalaryRequestUpdateBody {
        return {
            budgetExpectedFundingUntil: item.budgetExpectedFundingUntil,
            currentSalaryAmount: item.employeeInfo.currentSalaryAmount,
            plannedSalaryAmount: item.req?.plannedSalaryAmount || null,
            previousSalaryIncreaseText: item.employeeInfo.previousSalaryIncreaseText,
            previousSalaryIncreaseDate: item.employeeInfo.previousSalaryIncreaseDate,
            assessmentId: item.assessment?.id || null,
            comment: item.req?.comment,
            newPosition: item.req?.newPosition?.id || null
        };
    }

    updateItemRequest(id: number, body: SalaryRequestUpdateBody): Promise<any> {
        return salaryService.updateSalaryRequest(id, body);
    }

    itemEditable(itemId: number, updateBody: SalaryRequestUpdateBody): boolean {
        return true;
    }


}