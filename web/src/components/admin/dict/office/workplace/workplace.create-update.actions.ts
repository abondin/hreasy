import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import dictAdminService, {
    DictOfficeWorkplace,
    DictOfficeWorkplaceUpdateBody
} from "@/components/admin/dict/dict.admin.service";

export default class WorkplaceCreateOrUpdateAction
    extends InDialogActionDataContainer<{
        officeLocationId: number,
        workplaceId: number | null
    }, DictOfficeWorkplaceUpdateBody> {


    constructor() {
        super((id, body) => {
            if (id?.workplaceId) {
                return dictAdminService.updateOfficeWorkplace(id.officeLocationId, id.workplaceId, body!);
            } else {
                return dictAdminService.createOfficeWorkplace(id!.officeLocationId, body!);
            }
        });
    }

    public isNew(): boolean {
        return Boolean(!this._itemId?.workplaceId);
    }

    public openDialogForWorkplace(officeLocationId: number, workplace: DictOfficeWorkplace | null, mapCoordinates?:{x:number,y:number}) {
        const formData = {archived: false} as DictOfficeWorkplaceUpdateBody;
        if (workplace) {
            formData.name = workplace.name;
            formData.description = workplace.description;
            formData.archived = workplace.archived;
            formData.mapX = mapCoordinates?.x || workplace.mapX;
            formData.mapY = mapCoordinates?.y || workplace.mapY;
        }
        super.openDialog({officeLocationId: officeLocationId, workplaceId: workplace?.id || null}, formData);
    }

}
