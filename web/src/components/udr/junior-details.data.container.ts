import juniorService, {JuniorDto, UpdateJuniorRegistryBody} from "@/components/udr/udr.service";
import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";


export class JuniorDetailDataContainer {
    public readonly  updateDialogAction = new InDialogActionDataContainer<UpdateJuniorRegistryBody>((id, body) => juniorService.updateInRegistry(id!, body!));
    public readonly deleteDialogAction = new InDialogActionDataContainer<void>((id, body) => juniorService.deleteFromRegistry(id!));

    private readonly _updateFormData: UpdateJuniorRegistryBody | null = null;

    constructor(private _data: JuniorDto) {
        this._updateFormData = {
            mentorId: _data.mentor?.id || null,
            budgetingAccount: _data.budgetingAccount?.id || null,
            role: _data.role
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

}

