import {UiConstants} from "@/components/uiconstants";
import {DataTableHeader} from "vuetify";
import {errorUtils} from "@/components/errors";
import logger from "@/logger";

export interface WithId {
    id?: number;
}

export interface BasicDict extends WithId {
    name: string,
    archived: boolean
}

/**
 * Edit on form
 */
export interface UpdateBody {

}

/**
 * Add new item form
 */
export interface CreateBody {

}

export interface UpdateAction<T extends WithId, M extends UpdateBody> {
    updateItemRequest: (id: number, body: M) => Promise<T>,
    itemToUpdateBody: (item: T) => M,
}

export interface CreateAction<T extends WithId, C extends CreateBody> {
    createItemRequest: (body: C) => Promise<T>,
    defaultBody: () => C
}

export interface DeleteAction<T extends WithId> {
    deleteItemRequest: (id: number) => Promise<void>,
}


export abstract class Filter<T extends WithId> {
    public abstract applyFilter(items: T[]): T[];
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
export default class TableComponentDataContainer<T extends WithId, M extends UpdateBody, C extends CreateBody, F extends Filter<T>> {
    private _loading = false;
    private _error: string | null = null;
    private _updateDialog = false;
    private _updateError: string | null = null;

    private _createDialog = false;
    private _createError: string | null = null;

    private _deleteDialog = false;
    private _deleteError: string | null = null;

    private _items: T[] = [];
    private _updateItemId: number | null | undefined = null;
    private _updateBody: M | null = null;

    private _createBody: C | null = null;

    private _deleteItemId: number | null | undefined = null;

    private _headers: DataTableHeader[] = [];

    private _defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;

    private _initialized = false;

    /**
     *
     * @param dataLoader - load data from backend
     * @param headerLoader - table header
     * @param updateItemRequest - Promise to update item
     * @param itemToUpdateBody - converted from item to update form content
     * @param newCreateBody - default create form
     * @param _filter - table filter
     * @param _editable - is create/update/delete functionality enabled
     */
    constructor(private dataLoader: () => Promise<Array<T>>,
                private headerLoader: () => DataTableHeader[],
                private updateAction: UpdateAction<T, M> | null,
                private createAction: CreateAction<T, C> | null,
                private deleteAction: DeleteAction<T> | null,
                private _filter: F,
                private _editable: boolean = false) {
    }

    public init() {
        this.initHeaders();
        this._initialized = true;
        return this.reloadData();
    }

    public updateAllowed(): boolean {
        return this.updateAction ? true : false;
    }

    public createAllowed(): boolean {
        return this.createAction ? true : false;
    }

    public deleteAllowed(): boolean {
        return this.deleteAction ? true : false;
    }

    public editable(): boolean {
        return this._editable;
    }

    get initialized(): boolean {
        return this._initialized;
    }

    get defaultItemsPerTablePage(): number {
        return this._defaultItemsPerTablePage;
    }

    get filter() {
        return this._filter;
    }


    get loading(): boolean {
        return this._loading;
    }

    get error(): string | null {
        return this._error;
    }

    get items(): T[] {
        return this._items;
    }

    get headers(): DataTableHeader[] {
        return this._headers;
    }

//<editor-fold desc="Update Actions">
    get updateError(): string | null {
        return this._updateError;
    }

    get updateDialog(): boolean {
        return this._updateDialog;
    }

    get updateBody(): M | null {
        return this._updateBody;
    }

    public openUpdateDialog(item: T) {
        if (this.updateAction) {
            this._updateBody = this.updateAction.itemToUpdateBody(item);
            this._updateItemId = item.id;
            this._updateDialog = true;
            this._updateError = null;
        }
    }

    public closeUpdateDialog() {
        this._updateBody = null;
        this._updateDialog = false;
        this._updateItemId = null;
    }

    public submitUpdateForm() {
        if (this._updateBody && this.updateAction && this._updateItemId) {
            this._loading = true;
            this._updateError = null;
            this.updateAction.updateItemRequest(this._updateItemId, this._updateBody)
                .then(() => {
                    this._updateDialog = false;
                    return this.reloadData();
                })
                .catch(error => {
                    this._updateError = errorUtils.shortMessage(error);
                })
                .finally(() => {
                    this._loading = false;
                });
        } else {
            logger.error("Unable to submit update form without selected item");
        }
    }

//</editor-fold>

// <editor-fold desc="Create Actions">
    get createError(): string | null {
        return this._createError;
    }

    get createDialog(): boolean {
        return this._createDialog;
    }

    get createBody(): C | null {
        return this._createBody;
    }

    public openCreateDialog() {
        if (this.createAction) {
            this._createBody = this.createAction.defaultBody();
            this._createDialog = true;
            this._createError = null;
        }
    }

    public closeCreateDialog() {
        this._createBody = null;
        this._createDialog = false;
    }

    public submitCreateForm() {
        if (this._createBody && this.createAction) {
            this._loading = true;
            this._createError = null;
            this.createAction.createItemRequest(this._createBody)
                .then(() => {
                    this._createDialog = false;
                    return this.reloadData();
                })
                .catch(error => {
                    this._createError = errorUtils.shortMessage(error);
                })
                .finally(() => {
                    this._loading = false;
                });
        } else {
            logger.error("Unable to submit create form without selected item to edit");
        }
    }

//</editor-fold>

//<editor-fold desc="Delete Actions">
    get deleteError(): string | null {
        return this._deleteError;
    }

    get deleteDialog(): boolean {
        return this._deleteDialog;
    }

    public openDeleteDialog(item: T) {
        if (this.deleteAction) {
            this._deleteItemId = item.id;
            this._deleteDialog = true;
            this._deleteError = null;
        }
    }

    public closeDeleteDialog() {
        this._deleteItemId = null;
        this._deleteDialog = false;
    }

    public submitDeleteForm() {
        if (this.deleteAction && this._deleteItemId) {
            this._loading = true;
            this._deleteError = null;
            this.deleteAction.deleteItemRequest(this._deleteItemId)
                .then(() => {
                    this._deleteDialog = false;
                    return this.reloadData();
                })
                .catch(error => {
                    this._deleteError = errorUtils.shortMessage(error);
                })
                .finally(() => {
                    this._loading = false;
                });
        } else {
            logger.error("Unable to delete without selected item");
        }
    }

//</editor-fold>

    public initHeaders() {
        this._headers.length = 0;
        this._headers = this.headerLoader();
    }

    public reloadData() {
        this._error = null;
        this._loading = true;
        this._items.length = 0;
        return this.dataLoader()
            .then(data => {
                this._items = data;
            })
            .catch(error => {
                this._error = errorUtils.shortMessage(error);
            })
            .finally(() => {
                this._loading = false;
            });
    }


    public filteredItems(): T[] {
        return this._filter.applyFilter(this._items);
    }

}
