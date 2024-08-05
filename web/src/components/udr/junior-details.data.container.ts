import juniorService, {
    AddOrUpdateJuniorReportBody,
    GraduateBody,
    JuniorDto,
    JuniorProgressType,
    UpdateJuniorRegistryBody
} from "@/components/udr/udr.service";
import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";


export class JuniorDetailDataContainer {
    public readonly updateDialogAction = new InDialogActionDataContainer<number, UpdateJuniorRegistryBody>((id, body) => juniorService.updateInRegistry(id!, body!));
    public readonly deleteDialogAction = new InDialogActionDataContainer<number, void>((id, body) => juniorService.deleteFromRegistry(id!));

    public readonly graduateDialogAction = new InDialogActionDataContainer<number, GraduateBody>((id, body) =>
        body ? juniorService.graduate(id!, body) : juniorService.cancelGraduation(id!));
    public readonly addOrUpdateReportDialogAction =
        new InDialogActionDataContainer<{ juniorId: number, reportId: number | null }, AddOrUpdateJuniorReportBody>((id, body) =>
            id?.reportId ? juniorService.updateReport(id.juniorId, id.reportId!, body!) : juniorService.createReport(id!.juniorId, body!)
        );

    private readonly _updateFormData: UpdateJuniorRegistryBody | null = null;
    private readonly _graduateFormData: GraduateBody | null = null;

    constructor(private _data: JuniorDto) {
        this._updateFormData = {
            mentorId: _data.mentor?.id || null,
            budgetingAccount: _data.budgetingAccount?.id || null,
            role: _data.role
        };
        this._graduateFormData = {
            comment: _data.graduation?.comment || null
        };
    }

    get item() {
        return this._data;
    }

    public openUpdateDialog() {
        return this.updateDialogAction.openDialog(this._data.id!, this._updateFormData);
    }

    public openDeleteDialog() {
        return this.deleteDialogAction.openDialog(this._data.id!, null);
    }

    public openGraduateDialog(cancelAction = false) {
        return this.graduateDialogAction.openDialog(this._data.id!, cancelAction ? null : this._graduateFormData);
    }

    public openAddReportDialog() {
        return this.addOrUpdateReportDialogAction.openDialog({
            juniorId: this._data.id!,
            reportId: null
        }, {comment: '', progress: JuniorProgressType.NO_PROGRESS});
    }
}

