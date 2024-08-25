import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import logger from "@/logger";
import {SalaryIncreaseRequest} from "@/components/salary/salary.service";
import salaryAdminService, {
    SalaryRequestImplementationState,
    SalaryRequestImplementBody
} from "@/components/admin/salary/admin.salary.service";
import {SalaryRequestImplementationFormData} from "@/components/salary/details/impl/SalaryRequestImplementFormFields.vue";

export class SalaryRequestImplementAction extends InDialogActionDataContainer<number, SalaryRequestImplementationFormData> {

    constructor() {
        super((itemId, item) => this.implementItemRequest(itemId!, item!));
    }

    public implementItemRequest(id: number, formData: SalaryRequestImplementationFormData) {
        if (formData.completed) {
            logger.log(`Reset implementation for request ${id}`);
            return salaryAdminService.resetImplementation(id);
        }
        if (formData.state == SalaryRequestImplementationState.REJECTED) {
            const body = {
                comment: formData.comment,
                reason: formData.rejectReason
            };
            logger.log(`Reject salary request ${id}: ${body}`);
            return salaryAdminService.reject(id, body);
        } else {
            const body = {
                comment: formData.comment,
                increaseStartPeriod: formData.increaseStartPeriod,
                increaseAmount: formData.increaseAmount,
                salaryAmount: formData.salaryAmount,
                newPosition: formData.newPosition
            } as SalaryRequestImplementBody;
            logger.log(`Mark salary request ${id} as implemented: ${body}`);
            return salaryAdminService.markAsImplemented(id, body);
        }
    }

    public itemToBody(item: SalaryIncreaseRequest, state?: SalaryRequestImplementationState): SalaryRequestImplementationFormData {
        return {
            type: item.type,
            state: item.impl ? item.impl.state : state,
            increaseAmount: item.impl ? item.impl.increaseAmount : item.req.increaseAmount,
            salaryAmount: item.impl ? item.impl.salaryAmount : item.req.plannedSalaryAmount,
            increaseStartPeriod: item.impl ? item.impl.increaseStartPeriod : item.req.increaseStartPeriod,
            rejectReason: item.impl ? item.impl.rejectReason : '',
            newPosition: item.impl ? item.impl.newPosition?.id : item.req.newPosition?.id,
            completed: Boolean(item.impl)
        } as SalaryRequestImplementationFormData;
    }
}