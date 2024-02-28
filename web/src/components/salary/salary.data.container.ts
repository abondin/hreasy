import TableComponentDataContainer, {CreateAction} from "@/components/shared/table/TableComponentDataContainer";
import salaryService, {
    ClosedSalaryRequestPeriod,
    SalaryIncreaseRequest,
    SalaryRequestReportBody
} from "@/components/salary/salary.service";
import {SalaryRequestFormData, SalaryRequestImplementAction} from "@/components/salary/SalaryRequestImplementForm.vue";
import {SalaryRequestFilter} from "@/components/salary/SalaryRequestFilterComponent.vue";
import adminSalaryService from "@/components/admin/salary/admin.salary.service";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import permissionService from "@/store/modules/permission.service";
import {DataTableHeader} from "vuetify";
import {errorUtils} from "@/components/errors";
import logger from "@/logger";

export class SalaryRequestDataContainer extends TableComponentDataContainer<SalaryIncreaseRequest, SalaryRequestFormData, SalaryRequestReportBody, SalaryRequestFilter> {
    private _implementDialog = false;
    private _implementBody: SalaryRequestFormData | null = null;
    private _selectedPeriod = ReportPeriod.currentPeriod();
    private _closedPeriods: ClosedSalaryRequestPeriod[] = [];
    private _exportLoading = false;
    private _exportCompleted = false;
    private readonly _implementAction: SalaryRequestImplementAction;


    constructor(_headerLoader: () => DataTableHeader[]) {
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
            null,
            new SalaryRequestFilter(),
            () => permissionService.canReportSalaryRequest(),
            true
        );
        this._implementAction = new SalaryRequestImplementAction();
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
    public implementCommitAllowed(): boolean {
        if (!this._implementAction || !this.implementBody || !this.selectedItemId) {
            return false;
        }
        return this._implementAction.itemEditable(this.selectedItemId, this.implementBody);
    }

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

    public openImplementDialog(item: SalaryIncreaseRequest) {
        if (this._implementAction && this.implementAllowed()) {
            this.implementBody = this._implementAction.itemToBody(item);
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

    //</editor-fold>

    //<editor-fold desc="Export to excel">
    public exportToExcel() {
        this._exportLoading = true;
        this._exportCompleted = false;
        adminSalaryService.export(this._selectedPeriod.periodId()).then(() => {
            this._exportCompleted = true;
        }).finally(() => {
            this._exportLoading = false;
        })
    }

    get exportLoading(): boolean {
        return this._exportLoading;
    }

    get exportCompleted(): boolean {
        return this._exportCompleted;
    }

    //</editor-fold>

    //<editor-fold desc="Periods">
    get selectedPeriod() {
        return this._selectedPeriod;
    }

    public confirmExportCompleted() {
        this._exportCompleted = true;
    }


    private incrementPeriod() {
        this._selectedPeriod.increment();
        this.reloadData();
    }

    private decrementPeriod() {
        this._selectedPeriod.decrement();
        this.reloadData();
    }

    public periodClosed(): boolean {
        return this._selectedPeriod &&
            this._closedPeriods
            && this._closedPeriods.map(p => p.period).indexOf(this._selectedPeriod.periodId()) >= 0;
    }

    private closePeriod() {
        if (this._selectedPeriod) {
            this.doInLoadingSection(() => {
                return adminSalaryService.closeReportPeriod(this._selectedPeriod.periodId()).then(() => {
                    this.reloadData();
                })
            });
        }
    }

    private reopenPeriod() {
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

    //</editor-fold>

}
