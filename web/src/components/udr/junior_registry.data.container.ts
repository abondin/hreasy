import TableComponentDataContainer, {Filter} from "@/components/shared/table/TableComponentDataContainer";
import juniorService, {
    AddToJuniorRegistryBody,
    JuniorDto,
    UpdateJuniorRegistryBody
} from "@/components/udr/udr.service";
import {DataTableHeader} from "vuetify";
import permissionService from "@/store/modules/permission.service";
import adminSalaryService from "@/components/admin/salary/admin.salary.service";

export class JuniorTableFilter extends Filter<JuniorDto> {
    applyFilter(items: JuniorDto[]): JuniorDto[] {
        return items;
    }

}

export class JuniorRegistryDataContainer extends TableComponentDataContainer<JuniorDto, UpdateJuniorRegistryBody, AddToJuniorRegistryBody, JuniorTableFilter> {
    private _exportLoading = false;
    private _exportCompleted = false;
    constructor(_headerLoader: () => DataTableHeader[]) {
        super(() => juniorService.juniors(),
            _headerLoader,
            {
                updateItemRequest: (id, body) => juniorService.updateInRegistry(id, body),
                itemToUpdateBody: (item) => this.itemToUpdateBody(item),
                itemEditable: (itemId: number, updateBody: UpdateJuniorRegistryBody) => true,
            },
            {
                createItemRequest: (body) => juniorService.addToRegistry(body),
                defaultBody: () => this.defaultNewRecordBody()
            },
            {
                deleteItemRequest: (ids) => juniorService.deleteFromRegistry(ids)
            },
            new JuniorTableFilter(),
            () => true,
            true
        )
    }

    private itemToUpdateBody(item: JuniorDto): UpdateJuniorRegistryBody {
        return {
            mentorId: item.mentor?.id || null,
            budgetingAccount: item.budgetingAccount?.id || null,
            role: item.role
        };
    }

    private defaultNewRecordBody(): AddToJuniorRegistryBody {
        return {
            juniorId: null,
            mentorId: null,
            budgetingAccount: null,
            role: null
        }
    }

    public canExport(): boolean {
        return permissionService.canAdminJuniorRegistry();
    }

    public exportToExcel() {
        this._exportLoading = true;
        this._exportCompleted = false;
        juniorService.export().then(() => {
            this._exportCompleted = true;
        }).finally(() => {
            this._exportLoading = false;
        })
    }
}
