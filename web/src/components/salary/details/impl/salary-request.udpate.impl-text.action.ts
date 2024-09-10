import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import {SalaryIncreaseRequest} from "@/components/salary/salary.service";
import salaryAdminService from "@/components/admin/salary/admin.salary.service";
import {
    SalaryRequestUpdateImplTextFormData
} from "@/components/salary/details/impl/SalaryRequestUpdateImplTextFormFields.vue";

export class SalaryRequestUpdateImplTextAction extends InDialogActionDataContainer<number, SalaryRequestUpdateImplTextFormData> {

    constructor() {
        super((itemId, item) => this.updateImplIncreaseText(itemId!, item!));
    }

    public updateImplIncreaseText(id: number, formData: SalaryRequestUpdateImplTextFormData) {
        const body = {
            increaseText: formData.increaseText,
        };
        return salaryAdminService.updateImplIncreaseText(id, body);
    }

    public itemToBody(item: SalaryIncreaseRequest): SalaryRequestUpdateImplTextFormData {
        return {
            increaseText: item.impl?.increaseText
        } as SalaryRequestUpdateImplTextFormData;
    }
}