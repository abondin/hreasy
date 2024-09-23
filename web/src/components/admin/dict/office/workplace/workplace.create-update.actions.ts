import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import dictAdminService, {
    DictOfficeWorkplace,
    DictOfficeWorkplaceUpdateBody
} from "@/components/admin/dict/dict.admin.service";
import logger from "@/logger";

export default class WorkplaceCreateOrUpdateAction
    extends InDialogActionDataContainer<{ officeLocationId: number, workplaceId: number|null }, DictOfficeWorkplaceUpdateBody> {


    constructor() {
        super((id, body) => {
            if (id?.workplaceId) {
                return dictAdminService.updateOfficeWorkplace(id.officeLocationId, id.workplaceId, body!);
            } else {
                return dictAdminService.createOfficeWorkplace(id!.officeLocationId, body!);
            }
        });
    }

    public isNew(): boolean{
        return Boolean(!this._itemId?.workplaceId);
    }

    public openDialogForWorkplace(officeLocationId: number, workplace: DictOfficeWorkplace | null) {
        const formData = {archived: false} as DictOfficeWorkplaceUpdateBody;
        if (workplace) {
            formData.name = workplace.name;
            formData.description = workplace.description;
            formData.archived = workplace.archived;
            formData.mapX = workplace.mapX;
            formData.mapY = workplace.mapY;
        }
        super.openDialog({officeLocationId: officeLocationId, workplaceId: workplace?.id || null}, formData);
    }

}
