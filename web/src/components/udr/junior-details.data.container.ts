import juniorService, {GraduateBody, JuniorDto, UpdateJuniorRegistryBody} from "@/components/udr/udr.service";
import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import {ca} from "@fullcalendar/core/internal-common";
import logger from "@/logger";


export class JuniorDetailDataContainer {
    public readonly updateDialogAction = new InDialogActionDataContainer<UpdateJuniorRegistryBody>((id, body) => juniorService.updateInRegistry(id!, body!));
    public readonly deleteDialogAction = new InDialogActionDataContainer<void>((id, body) => juniorService.deleteFromRegistry(id!));

    public readonly graduateDialogAction = new InDialogActionDataContainer<GraduateBody>((id, body) =>
        body ? juniorService.graduate(id!, body) : juniorService.cancelGraduation(id!)) ;

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

    public openGraduateDialog(cancelAction=false) {
        return this.graduateDialogAction.openDialog(this._data.id!, cancelAction? null : this._graduateFormData);
    }

}

