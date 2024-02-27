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

export class SalaryRequestDataContainer extends TableComponentDataContainer<SalaryIncreaseRequest, SalaryRequestFormData, SalaryRequestReportBody, SalaryRequestFilter> {
    private implementDialog = false;
    private implementBody: SalaryRequestFormData | null = null;
    private implementAction = new SalaryRequestImplementAction();
    private _selectedPeriod = ReportPeriod.currentPeriod();
    private _closedPeriods: ClosedSalaryRequestPeriod[] = [];
    private _exportLoading = false;
    private _exportCompleted = false;


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
            new SalaryRequestImplementAction(),
            {
                createItemRequest: (body) => salaryService.reportSalaryRequest(body),
                defaultBody: () => this.defaultReportNewRequestBody(),
            } as CreateAction<SalaryIncreaseRequest, SalaryRequestReportBody>,
            null,
            new SalaryRequestFilter(),
            ()=>permissionService.canReportSalaryRequest(),
            true
        );

    }

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

    private defaultReportNewRequestBody(): SalaryRequestReportBody {
        return {
            type: this.filter.type,
            increaseStartPeriod: this._selectedPeriod.periodId(),
            budgetBusinessAccount: this.filter.ba && this.filter.ba.length > 0 ? this.filter.ba[0] : null
        } as SalaryRequestReportBody;
    }

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

}
