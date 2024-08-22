import {
    SalaryApprovalState,
    SalaryIncreaseRequest,
    SalaryRequestApproval,
    SalaryRequestType
} from "@/components/salary/salary.service";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import {SalaryRequestApproveAction} from "@/components/salary/details/approval/salary-request.approve.action";
import {SalaryRequestImplementAction} from "@/components/salary/details/impl/salary-request.implement.action";
import {SalaryRequestImplementationState} from "@/components/admin/salary/admin.salary.service";
import {SalaryRequestDeleteAction} from "@/components/salary/details/info/salary-request.delete.action";
import {
    SalaryRequestUpdateAction,
    SalaryRequestUpdateBody
} from "@/components/salary/details/info/salary-request.update.action";

export class SalaryDetailsDataContainer {

    private _approveAction = new SalaryRequestApproveAction();
    private _implementAction = new SalaryRequestImplementAction();

    private _deleteRequestAction = new SalaryRequestDeleteAction();
    private _updateRequestAction = new SalaryRequestUpdateAction();

    public constructor(private _periodHolder: { period: ReportPeriod, closed: boolean }, private _item: SalaryIncreaseRequest) {
    }

    get item() {
        return this._item;
    }

    isSalaryRequest(): boolean {
        return Boolean(this.item.type == SalaryRequestType.SALARY_INCREASE);
    }

    isImplemented(){
        return Boolean(this.item.impl?.implementedAt)
    }


    get periodClosed(): boolean {
        return this._periodHolder &&
            this._periodHolder.closed;
    }

    //<editor-fold desc="Request and general information">
    get deleteAction(): SalaryRequestDeleteAction {
        return this._deleteRequestAction;
    }
    public openDeleteDialog(itemId: number) {
        this._deleteRequestAction.openDialog(itemId, null);
    }
    get updateAction(): SalaryRequestUpdateAction {
        return this._updateRequestAction;
    }
    openUpdateDialog(id: number, formData: SalaryRequestUpdateBody) {
        this._updateRequestAction.openDialog(id, formData);
    }
    //</editor-fold>

    //<editor-fold desc="Approval">
    get approveAction(): SalaryRequestApproveAction {
        return this._approveAction;
    }

    public openApproveDialog(item: SalaryIncreaseRequest, approval: SalaryRequestApproval | null, action?: SalaryApprovalState) {
        const approveBody = this._approveAction.itemToBody(approval, action);
        this._approveAction.openDialog(item?.id || null, approveBody);
    }
    //</editor-fold>

    //<editor-fold desc="Implementation">
    get implementAction(): SalaryRequestImplementAction {
        return this._implementAction;
    }
    openImplementDialog(item: SalaryIncreaseRequest, state?: SalaryRequestImplementationState) {
        const body = this._implementAction.itemToBody(item, state);
        this._implementAction.openDialog(item.id, body);
    }
    //</editor-fold>


}
