import TableComponentDataContainer, {CreateAction, Filter} from "@/components/shared/table/TableComponentDataContainer";
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

    constructor(_headerLoader: () => DataTableHeader[], private _clickOnRowAction: (item: JuniorDto) => any, _addAction = new AddJuniorActionHandler()) {
        super(() => juniorService.juniors(),
            _headerLoader,
            null,
            _addAction,
            null,
            new JuniorTableFilter(),
            () => true,
            true
        )
    }

    clickOnRowAction(item: JuniorDto): void {
        return this._clickOnRowAction(item);
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
    public actionOnClickAllowed(): boolean{
        return true;
    }


}

export class AddJuniorActionHandler implements CreateAction<JuniorDto, AddJuniorRegistryBody> {
    createItemRequest(body: AddJuniorRegistryBody): Promise<any> {
        return juniorService.addToRegistry(body);
    }

    defaultBody(): AddJuniorRegistryBody {
        return {
            juniorEmplId: null,
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
}
