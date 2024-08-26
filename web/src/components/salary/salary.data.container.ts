import TableComponentDataContainer, {CreateAction} from "@/components/shared/table/TableComponentDataContainer";
import salaryService, {
    ClosedSalaryRequestPeriod,
    SalaryIncreaseRequest,
    SalaryRequestReportBody
} from "@/components/salary/salary.service";
import {SalaryRequestImplementationFormData} from "@/components/salary/details/impl/SalaryRequestImplementFormFields.vue";
import {SalaryRequestFilter} from "@/components/salary/SalaryRequestFilterComponent.vue";
import adminSalaryService from "@/components/admin/salary/admin.salary.service";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import permissionService from "@/store/modules/permission.service";
import {DataTableHeader} from "vuetify";
import {DataContainerWithExcelExportSupport} from "@/components/shared/table/DataContainerWithExcelExportSupport";
import {
    DataContainerWithPeriodSelectionSupport
} from "@/components/shared/table/DataContainerWithPeriodSelectionSupport";
import {UiConstants} from "@/components/uiconstants";

export class SalaryRequestDataContainer extends TableComponentDataContainer<SalaryIncreaseRequest, SalaryRequestImplementationFormData, SalaryRequestReportBody, SalaryRequestFilter>
    implements DataContainerWithExcelExportSupport, DataContainerWithPeriodSelectionSupport {

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
            null,
            new SalaryRequestFilter(),
            () => permissionService.canReportSalaryRequest(),
            true
        );
    }

    get defaultItemsPerTablePage(): number {
        return -1;
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
        return this.reloadData();
    }

    decrementPeriod() {
        this._selectedPeriod.decrement();
        return this.reloadData();
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
                    return this.reloadData();
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
