import DictTableComponentDataContainer, {
    BasicDictFilter
} from "@/components/admin/dict/DictTableComponentDataContainer";
import dictAdminService, {
    DictOfficeLocation,
    DictOfficeLocationUpdateBody
} from "@/components/admin/dict/dict.admin.service";
import {CreateOrUpdateAction} from "@/components/shared/table/EditTableComponentDataContainer";
import permissionService from "@/store/modules/permission.service";
import dictService, {OfficeOrOfficeLocationMap} from "@/store/modules/dict.service";
import {DataTableHeader} from "vuetify";


export default class DictAdminOfficeLocationsDataTableContainer extends DictTableComponentDataContainer<DictOfficeLocation, DictOfficeLocationUpdateBody, BasicDictFilter<DictOfficeLocation>> {

    private _allMaps: OfficeOrOfficeLocationMap[] = [];

    public constructor(headerLoader: () => DataTableHeader[]) {
        super(
            () => dictService.getOfficeLocationMaps().then(maps => {
                this._allMaps = maps;
            }).then(() => dictAdminService.loadOfficeLocations()),
            headerLoader,
            {
                updateItemRequest: (id, body) => (dictAdminService.updateOfficeLocation(id, body)),
                itemEditable: (id, body) => true,
                itemToUpdateBody: item =>
                    ({
                        name: item.name,
                        archived: item.archived,
                        officeId: item.office?.id,
                        description: item.description
                    } as DictOfficeLocationUpdateBody),
                createItemRequest: (body) => (dictAdminService.createOfficeLocation(body)),
                defaultBody: () => ({name: '', archived: false} as DictOfficeLocationUpdateBody)
            } as CreateOrUpdateAction<DictOfficeLocation, DictOfficeLocationUpdateBody>,
            new BasicDictFilter(),
            permissionService.canAdminDictOfficeLocations()
        );
    }

    get allMaps(): OfficeOrOfficeLocationMap[] {
        return this._allMaps;
    }
}
