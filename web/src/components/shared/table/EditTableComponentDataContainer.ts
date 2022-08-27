import {DataTableHeader} from "vuetify";
import TableComponentDataContainer, {
    CreateAction,
    CreateBody,
    DeleteAction,
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


/**
 * Extension of TableComponentDataContainer for the case when one form can be used for update and create
 */
export default class EditTableComponentDataContainer<T extends WithId, UC extends CreateOrUpdateBody, F extends Filter<T>>
    extends TableComponentDataContainer<T, UC, UC, F> {
    private _editBody: UC | null = null;


    /**
     *
     * @param editDataLoader - load data using REST API
     * @param editHeaderLoader - table headers
     * @param createOrUpdateAction - update item using rest api (null in case of operation is not designed)
     * @param editDeleteAction - delete item using rest api (null in case of operation is not designed)
     * @param _editFilter - store filtration information and provide filter action
     * @param _editEditable - if update/create/delete operations allowed
     */
    constructor(private editDataLoader: () => Promise<Array<T>>,
                private editHeaderLoader: () => DataTableHeader[],
                private createOrUpdateAction: CreateOrUpdateAction<T, UC> | null,
                private editDeleteAction: DeleteAction<T> | null,
                private _editFilter: F,
                private _editEditable: ()=>boolean) {
        super(editDataLoader, editHeaderLoader, createOrUpdateAction, createOrUpdateAction, editDeleteAction, _editFilter, _editEditable);
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
