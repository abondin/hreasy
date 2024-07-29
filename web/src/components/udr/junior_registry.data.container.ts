import TableComponentDataContainer, {
    CreateAction,
    Filter,
    UpdateAction
} from "@/components/shared/table/TableComponentDataContainer";
import juniorService, {AddJuniorRegistryBody, JuniorDto, UpdateJuniorRegistryBody} from "@/components/udr/udr.service";
import {DataTableHeader} from "vuetify";
import permissionService from "@/store/modules/permission.service";
import {DataContainerWithExcelExportSupport} from "@/components/shared/table/DataContainerWithExcelExportSupport";

export class JuniorTableFilter extends Filter<JuniorDto> {
    applyFilter(items: JuniorDto[]): JuniorDto[] {
        return items;
    }

}

export class JuniorRegistryDataContainer extends TableComponentDataContainer<JuniorDto, UpdateJuniorRegistryBody, AddJuniorRegistryBody, JuniorTableFilter>
    implements DataContainerWithExcelExportSupport {
    private _exportLoading = false;
    private _exportCompleted = false;

    constructor(_headerLoader: () => DataTableHeader[], _addOrUpdateAction = new AddOrUpdateJuniorActionHandler()) {
        super(() => juniorService.juniors(),
            _headerLoader,
            _addOrUpdateAction,
            _addOrUpdateAction,
            {
                deleteItemRequest: (ids) => juniorService.deleteFromRegistry(ids)
            },
            new JuniorTableFilter(),
            () => true,
            true
        )
    }

    public canExport(): boolean {
        return permissionService.canAdminJuniorRegistry();
    }

    public confirmExportCompleted() {
        this._exportCompleted = true;
    }

    public exportToExcel() {
        this._exportLoading = true;
        this._exportCompleted = false;
        return juniorService.export().then(() => {
            this._exportCompleted = true;
        }).finally(() => {
            this._exportLoading = false;
        })
    }

    get exportCompleted() {
        return this._exportCompleted;
    }

}

export class AddOrUpdateJuniorActionHandler implements CreateAction<JuniorDto, AddJuniorRegistryBody>, UpdateAction<JuniorDto, UpdateJuniorRegistryBody> {
    createItemRequest(body: AddJuniorRegistryBody): Promise<any> {
        return juniorService.addToRegistry(body);
    }

    defaultBody(): AddJuniorRegistryBody {
        return {
            juniorId: null,
            mentorId: null,
            budgetingAccount: null,
            role: null
        }
    }

    itemToUpdateBody(item: JuniorDto): UpdateJuniorRegistryBody {
        return {
            mentorId: item.mentor?.id || null,
            budgetingAccount: item.budgetingAccount?.id || null,
            role: item.role
        };
    }

    updateItemRequest(id: number, body: UpdateJuniorRegistryBody): Promise<any> {
        return juniorService.updateInRegistry(id, body)
    }

    itemEditable(itemId: number, updateBody: UpdateJuniorRegistryBody): boolean {
        return true;
    }

}
