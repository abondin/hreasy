import {DataTableHeader} from "vuetify";
import TableComponentDataContainer, {
    CreateAction,
    CreateBody,
    Filter,
    UpdateAction,
    UpdateBody,
    WithId
} from "@/components/shared/table/TableComponentDataContainer";


export interface CreateOrUpdateBody extends CreateBody, UpdateBody {

}

export interface CreateOrUpdateAction<T extends WithId, M extends CreateOrUpdateBody>
    extends CreateAction<T, M>, UpdateAction<T, M> {
}

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
 * Data container for table component. Encapsulate business logic
 */
export default class DictTableComponentDataContainer<T extends BasicDict, UC extends CreateOrUpdateBody, F extends BasicDictFilter<T>>
    extends TableComponentDataContainer<T, UC, UC, F> {
    private _editBody: UC | null = null;


    /**
     *
     * @param dictDataLoader - load data using REST API
     * @param dictHeaderLoader - table headers
     * @param createOrUpdateAction - update item using rest api (null in case of operation is not designed)
     * @param createAction - create item using rest api (null in case of operation is not designed)
     * @param deleteAction - delete item using rest api (null in case of operation is not designed)
     * @param _dictFilter - store filtration information and provide filter action
     * @param _dictEditable - if update/create/delete operations allowed
     */
    constructor(private dictDataLoader: () => Promise<Array<T>>,
                private dictHeaderLoader: () => DataTableHeader[],
                private createOrUpdateAction: CreateOrUpdateAction<T, UC> | null,
                private _dictFilter: F,
                private _dictEditable: boolean = false) {
        super(dictDataLoader, dictHeaderLoader, createOrUpdateAction, createOrUpdateAction, null, _dictFilter, _dictEditable);
    }

    get updateBody(): UC | null {
        return this._editBody;
    }

    set updateBody(body: UC | null) {
        this._editBody = body;
    }

    get createBody(): UC | null {
        return this._editBody;
    }

    set createBody(body: UC | null) {
        this._editBody = body;
    }

}
