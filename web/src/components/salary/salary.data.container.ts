import TableComponentDataContainer, {CreateAction} from "@/components/shared/table/TableComponentDataContainer";
import salaryService, {
    ClosedSalaryRequestPeriod,
    SalaryApprovalState,
    SalaryIncreaseRequest,
    SalaryRequestApproval,
    SalaryRequestReportBody
} from "@/components/salary/salary.service";
import {SalaryRequestFormData, SalaryRequestImplementAction} from "@/components/salary/SalaryRequestImplementForm.vue";
import {SalaryRequestFilter} from "@/components/salary/SalaryRequestFilterComponent.vue";
import adminSalaryService, {SalaryRequestImplementationState} from "@/components/admin/salary/admin.salary.service";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import permissionService from "@/store/modules/permission.service";
import {DataTableHeader} from "vuetify";
import {errorUtils} from "@/components/errors";
import logger from "@/logger";
import {
    SalaryRequestApproveAction,
    SalaryRequestApproveFormData
} from "@/components/salary/SalaryRequestApprovalForm.vue";
import {DataContainerWithExcelExportSupport} from "@/components/shared/table/DataContainerWithExcelExportSupport";
import {
    DataContainerWithPeriodSelectionSupport
} from "@/components/shared/table/DataContainerWithPeriodSelectionSupport";

export class SalaryRequestDataContainer extends TableComponentDataContainer<SalaryIncreaseRequest, SalaryRequestFormData, SalaryRequestReportBody, SalaryRequestFilter>
    implements DataContainerWithExcelExportSupport, DataContainerWithPeriodSelectionSupport {
    private _implementDialog = false;
    private _implementBody: SalaryRequestFormData | null = null;
    private readonly _implementAction: SalaryRequestImplementAction;

    private _approveDialog = false;
    private _approveBody: SalaryRequestApproveFormData | null = null;
    private readonly _approveAction: SalaryRequestApproveAction;

    private _selectedPeriod = ReportPeriod.currentPeriod();
    private _closedPeriods: ClosedSalaryRequestPeriod[] = [];

    constructor(_headerLoader: () => DataTableHeader[], private _clickOnRowAction: (period: ReportPeriod, item: SalaryIncreaseRequest) => any =
        (p, i) => {
            // do nothing
        }) {
        super(() => salaryService.getClosedSalaryRequestPeriods()
                .then(data => {
                    this.setClosedPeriods(data);
                    return data;
                }).then(d => {
                    return this.canAdmin() ? adminSalaryService.loadAllSalaryRequests(this._selectedPeriod.periodId()) :
                        salaryService.load(this._selectedPeriod.periodId());
                }),
            _headerLoader,
            null,
            {
                createItemRequest: (body) => salaryService.reportSalaryRequest(body),
                defaultBody: () => this.defaultReportNewRequestBody(),
            } as CreateAction<SalaryIncreaseRequest, SalaryRequestReportBody>,
            {
                deleteItemRequest: ids => salaryService.deleteSalaryRequest(ids)
            },
            new SalaryRequestFilter(),
            () => permissionService.canReportSalaryRequest(),
            true
        );
        this._implementAction = new SalaryRequestImplementAction();
        this._approveAction = new SalaryRequestApproveAction();
    }

    clickOnRowAction(item: SalaryIncreaseRequest): void {
        return this._clickOnRowAction(this._selectedPeriod, item);
    }
    public get actionOnClickAllowed(): boolean {
        return true;
    }

    //<editor-fold desc="Report new request">
    private defaultReportNewRequestBody(): SalaryRequestReportBody {
        return {
            type: this.filter.type,
            increaseStartPeriod: this._selectedPeriod.periodId(),
            budgetBusinessAccount: this.filter.ba && this.filter.ba.length > 0 ? this.filter.ba[0] : null
        } as SalaryRequestReportBody;
    }

    //</editor-fold>

    //<editor-fold desc="Implement request">

    /**
     * Allow to open update dialog
     */
    public implementAllowed(): boolean {
        return this._implementAction ? true : false;
    }

    get implementDialog(): boolean {
        return this._implementDialog;
    }

    get implementBody(): SalaryRequestFormData | null {
        return this._implementBody;
    }

    set implementBody(body: SalaryRequestFormData | null) {
        this._implementBody = body;
    }

    public openImplementDialog(item: SalaryIncreaseRequest, state?: SalaryRequestImplementationState) {
        if (this._implementAction && this.implementAllowed()) {
            this.implementBody = this._implementAction.itemToBody(item, state);
            this.selectedItems = [item];
            this._implementDialog = true;
            this._actionError = null;
        }
    }

    public closeImplementDialog() {
        this.implementBody = null;
        this._implementDialog = false;
    }

    public submitImplementForm() {
        if (this.implementBody && this._implementAction && this.selectedItemId) {
            this._loading = true;
            this._actionError = null;
            this._implementAction.implementItemRequest(this.selectedItemId, this.implementBody)
                .then(() => {
                    this._implementDialog = false;
                    return this.reloadData();
                })
                .catch(error => {
                    this._actionError = errorUtils.shortMessage(error);
                })
                .finally(() => {
                    this._loading = false;
                });
        } else {
            logger.error("Unable to submit implement form without selected item");
        }
    }

    public isImplemented() {
        return Boolean(this.selectedItems && this.selectedItems.length > 0 && this.selectedItems[0].impl);
    }

    //</editor-fold>

    //<editor-fold desc="Approve request">
    /**
     * Allow to open approve or comment request dialog
     */
    public approveAllowed(): boolean {
        return this._approveAction ? true : false;
    }

    get approveDialog(): boolean {
        return this._approveDialog;
    }

    get approveBody(): SalaryRequestApproveFormData | null {
        return this._approveBody;
    }

    set approveBody(body: SalaryRequestApproveFormData | null) {
        this._approveBody = body;
    }

    public openApproveDialog(item: SalaryIncreaseRequest, approval: SalaryRequestApproval | null, action?: SalaryApprovalState) {
        if (this._approveAction && this.approveAllowed()) {
            this.approveBody = this._approveAction.itemToBody(approval, action);
            this.selectedItems = [item];
            this._approveDialog = true;
            this._actionError = null;
        }
    }

    public closeApproveDialog() {
        this.approveBody = null;
        this._approveDialog = false;
    }

    public submitApproveForm() {
        if (this.approveBody && this._approveAction && this.selectedItemId) {
            this._loading = true;
            this._actionError = null;
            this._approveAction.doApprovalAction(this.selectedItemId, this.approveBody)
                .then(() => {
                    this._approveDialog = false;
                    return this.reloadData();
                })
                .catch(error => {
                    this._actionError = errorUtils.shortMessage(error);
                })
                .finally(() => {
                    this._loading = false;
                });
        } else {
            logger.error("Unable to submit approve form without selected item");
        }
    }

    //</editor-fold>

    //<editor-fold desc="Export to excel">
    public exportToExcel() {
        return adminSalaryService.export(this._selectedPeriod.periodId());
    }

    //</editor-fold>

    //<editor-fold desc="Periods">
    get selectedPeriod() {
        return this._selectedPeriod;
    }


    incrementPeriod() {
        this._selectedPeriod.increment();
        this.reloadData();
    }

    decrementPeriod() {
        this._selectedPeriod.decrement();
        this.reloadData();
    }

    get periodClosed(): boolean {
        return this._selectedPeriod &&
            this._closedPeriods
            && this._closedPeriods.map(p => p.period).indexOf(this._selectedPeriod.periodId()) >= 0;
    }

    closePeriod() {
        if (this._selectedPeriod) {
            return this.doInLoadingSection(() => {
                return adminSalaryService.closeReportPeriod(this._selectedPeriod.periodId()).then(() => {
                    return this.reloadData();
                })
            });
        }
    }

    reopenPeriod() {
        if (this._selectedPeriod) {
            return this.doInLoadingSection(() => {
                return adminSalaryService.reopenReportPeriod(this._selectedPeriod.periodId()).then(() => {
                    this.reloadData();
                })
            });
        }
    }

    private setClosedPeriods(data: ClosedSalaryRequestPeriod[]) {
        this._closedPeriods = data;
    }

    //</editor-fold>


    //<editor-fold desc="Permissions">
    public canClosePeriod(): boolean {
        return this._selectedPeriod && this.canAdmin();
    }

    public canAdmin(): boolean {
        return permissionService.canAdminSalaryRequests();
    }

    public canReport(): boolean {
        return permissionService.canReportSalaryRequest();
    }

    public canExport(): boolean {
        return permissionService.canAdminSalaryRequests();
    }

    get showSelectCheckbox(): boolean {
        return false;
    }

    //</editor-fold>

}
