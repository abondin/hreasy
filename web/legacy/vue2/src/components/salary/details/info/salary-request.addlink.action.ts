import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import salaryService, {SalaryRequestLink, SalaryRequestLinkType} from "@/components/salary/salary.service";
import {CreateAction} from "@/components/shared/table/TableComponentDataContainer";

export interface SalaryRequestAddLinkFormData {
    source: number | null,
    destination: number | null,
    type: number | null,
    comment: string | null
}


export class SalaryRequestAddLinkAction
    extends InDialogActionDataContainer<number, SalaryRequestAddLinkFormData>
    implements CreateAction<SalaryRequestLink, SalaryRequestAddLinkFormData> {

    constructor() {
        super((id, item) => this.createItemRequest(item!));
    }

    defaultBody(): SalaryRequestAddLinkFormData {
        return {
            source: null,
            destination: null,
            type: SalaryRequestLinkType.MULTISTAGE,
            comment: null,
        };
    }

    createItemRequest(body: SalaryRequestAddLinkFormData): Promise<number> {
        return salaryService.addLink({
            source: body.source!,
            destination: body.destination!,
            type: body.type!,
            comment: body.comment
        });
    }

}
