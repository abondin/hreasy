import {DataTableHeader} from "vuetify";
import {Filter, WithId} from "@/components/shared/table/TableComponentDataContainer";
import EditTableComponentDataContainer, {
    CreateOrUpdateAction,
    CreateOrUpdateBody
} from "@/components/shared/table/EditTableComponentDataContainer";


export interface BasicDict extends WithId {
    name: string,
    archived: boolean
}

export class BasicDictFilter<T extends BasicDict> extends Filter<T> {
    private search: string = '';
    private onlyNotArchived = true;

    applyFilter(items: T[]): T[] {
        return items.filter((item) => {
            let result = true
            if (this.onlyNotArchived) {
                result = result && !item.archived;
            }
            if (this.search && this.search.trim().length > 0) {
                result = (!item.name) || (item.name.toLocaleLowerCase().indexOf(this.search.toLocaleLowerCase().trim()) >= 0);
            }
            return result;
        });
    }
}

/**
 * Data container to admin dictionaries
 */
export default class DictTableComponentDataContainer<T extends BasicDict, UC extends CreateOrUpdateBody, F extends BasicDictFilter<T>>
    extends EditTableComponentDataContainer<T, UC, F> {

    constructor(private dictDataLoader: () => Promise<Array<T>>,
                private dictHeaderLoader: () => DataTableHeader[],
                private dictCreateOrUpdateAction: CreateOrUpdateAction<T, UC> | null,
                private _dictFilter: F,
                private _dictEditable: boolean = false) {
        super(dictDataLoader, dictHeaderLoader, dictCreateOrUpdateAction, null, _dictFilter, () => _dictEditable);
    }
}
