import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import logger from "@/logger";
import salaryService, {
    SalaryApprovalState, SalaryRequestApproval,
    SalaryRequestApproveBody, SalaryRequestCommentBody,
    SalaryRequestDeclineBody
} from "@/components/salary/salary.service";
import {SalaryRequestApproveFormData} from "@/components/salary/details/approval/SalaryRequestApprovalFormFields.vue";

export class SalaryRequestApproveAction extends InDialogActionDataContainer<number, SalaryRequestApproveFormData> {

    constructor() {
        super((itemId, item) => this.doApprovalAction(itemId!, item!));
    }

    public doApprovalAction(requestId: number, formData: SalaryRequestApproveFormData) {
        if (formData.approvalId) {
            logger.log(`Delete approval/comment ${requestId}:${formData.approvalId}`);
            return salaryService.deleteApproval(requestId, formData.approvalId);
        }
        const body = {
            comment: formData.comment,
        };
        switch (formData.action) {
            case SalaryApprovalState.APPROVE:
                logger.log(`Approve salary request ${requestId}: ${body}`);
                return salaryService.approve(requestId, body as SalaryRequestApproveBody);
            case SalaryApprovalState.DECLINE:
                logger.log(`Decline salary request ${requestId}: ${body}`);
                return salaryService.decline(requestId, body as SalaryRequestDeclineBody);
            case SalaryApprovalState.COMMENT:
                logger.log(`Comment salary request ${requestId}: ${body}`);
                return salaryService.comment(requestId, body as SalaryRequestCommentBody);
            default:
                throw new Error(`Unsupported action ${formData.action}`);
        }
    }

    public itemToBody(item: SalaryRequestApproval | null, action?: SalaryApprovalState): SalaryRequestApproveFormData {
        if (item == null) {
            return {
                action: action || SalaryApprovalState.COMMENT,
                comment: null,
                approvalId: null
            } as SalaryRequestApproveFormData;
        } else {
            return {
                approvalId: item.id,
                action: item.state,
                comment: item.comment,
            } as SalaryRequestApproveFormData;
        }
    }
}