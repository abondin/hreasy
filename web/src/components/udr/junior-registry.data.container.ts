import TableComponentDataContainer, {CreateAction, Filter} from "@/components/shared/table/TableComponentDataContainer";
import juniorService, {AddJuniorRegistryBody, JuniorDto, UpdateJuniorRegistryBody} from "@/components/udr/udr.service";
import {DataTableHeader} from "vuetify";
import permissionService from "@/store/modules/permission.service";
import {DataContainerWithExcelExportSupport} from "@/components/shared/table/DataContainerWithExcelExportSupport";
import {searchUtils, TextFilterBuilder} from "@/components/searchutils";

export class JuniorTableFilter extends Filter<JuniorDto> {
    public search = '';
    public onlyNotGraduated = true;

    applyFilter(items: JuniorDto[]): JuniorDto[] {
        return items.filter((item) => {
            let filtered = true;
            const search = this.search.toLowerCase().trim();
            const textFilters = TextFilterBuilder.of()
                .splitWords(item.juniorEmpl?.name)
                .splitWords(item.mentor?.name)
                .ignoreCase(item?.latestReport?.createdBy?.name)
                .ignoreCase(item?.role);

            filtered = filtered && searchUtils.textFilter(this.search, textFilters);
            if (this.onlyNotGraduated) {
                filtered = filtered && Boolean(!item.graduation);
            }
            return filtered;
        });

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

    public actionOnClickAllowed(): boolean {
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
