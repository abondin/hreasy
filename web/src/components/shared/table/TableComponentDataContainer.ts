import {UiConstants} from "@/components/uiconstants";
import {DataTableHeader} from "vuetify";
import {errorUtils} from "@/components/errors";
import logger from "@/logger";

export interface WithId {
    id?: number;
}


/**
 * Edit on form
 */
// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface UpdateBody {

}

/**
 * Add new item form
 */
// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface CreateBody {

}

export interface UpdateAction<T extends WithId, M extends UpdateBody> {
    updateItemRequest: (id: number, body: M) => Promise<any>,
    itemToUpdateBody: (item: T) => M,

    itemEditable(itemId: number, updateBody: M): boolean;
}

export interface CreateAction<T extends WithId, C extends CreateBody> {
    createItemRequest: (body: C) => Promise<any>,
    defaultBody: () => C
}

export interface DeleteAction<T extends WithId> {
    deleteItemRequest: (ids: number[]) => Promise<Array<any>>,
}


export abstract class Filter<T extends WithId> {
    public abstract applyFilter(items: T[]): T[];
}

/**
 * Data container for table component. Encapsulate business logic
 * <T> - items type
 * <M> - update item DTO that sends to the backend
 * <C> - create item DTO that sends to the backend
 * <F> - filter type
 */
export default class TableComponentDataContainer<T extends WithId, M extends UpdateBody, C extends CreateBody, F extends Filter<T>> {
    protected _loading = false;
    private _error: string | null = null;
    protected _actionError: string | null = null;

    private _updateDialog = false;

    private _createDialog = false;

    private _deleteDialog = false;

    private _items: T[] = [];
    private _selectedItems: T[] = [];

    private _updateBody: M | null = null;

    private _createBody: C | null = null;

    private _headers: DataTableHeader[] = [];


    private _initialized = false;

    /**
     *
     * @param dataLoader - load data using REST API
     * @param headerLoader - table headers
     * @param updateAction - update item using rest api (null in case of operation is not designed)
     * @param createAction - create item using rest api (null in case of operation is not designed)
     * @param deleteAction - delete item using rest api (null in case of operation is not designed)
     * @param _filter - store filtration information and provide filter action
     * @param _editable - if update/create/delete operations allowed
     * @param _singleSelect - true by default. False if your table supports bulk update
     */
    constructor(private dataLoader: () => Promise<Array<T>>,
                private headerLoader: () => DataTableHeader[],
                private updateAction: UpdateAction<T, M> | null,
                private createAction: CreateAction<T, C> | null,
                private deleteAction: DeleteAction<T> | null,
                private _filter: F,
                private _editable: () => boolean,
                private _singleSelect: boolean = true) {
    }

    public init(reload = true) {
        this.initHeaders();
        this._initialized = true;
        if (reload) {
            return this.reloadData();
        }
    }

    public editable(): boolean {
        return this._editable();
    }

    get singleSelect(): boolean {
        return this._singleSelect;
    }

    get initialized(): boolean {
        return this._initialized;
    }

    get defaultItemsPerTablePage(): number {
        return UiConstants.defaultItemsPerTablePage;
    }
    get lgItemsPerTablePage(): number {
        return UiConstants.lgItemsPerTablePage;
    }

    get filter(): F {
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

    get selectedItems(): T[] {
        return this._selectedItems;
    }

    set selectedItems(values: T[]) {
        this._selectedItems = values;
    }

    get headers(): DataTableHeader[] {
        return this._headers;
    }

    get actionError(): string | null {
        return this._actionError;
    }

    get selectedItemId(): number | null | undefined {
        return this._selectedItems.length > 0 ? this._selectedItems[0].id : null;
    }

//<editor-fold desc="Update Actions">
    /**
     * Allow to apply the changes
     * If false - update form must be readonly
     */
    public updateCommitAllowed(): boolean {
        if (!this.updateAction || !this.updateBody || !this.selectedItemId) {
            return false;
        }
        return this.updateAction.itemEditable(this.selectedItemId, this.updateBody);
    }

    public get actionOnClickAllowed(): boolean {
        return this.updateAllowed();
    }

    /**
     * Allow to open update dialog
     */
    public updateAllowed(): boolean {
        return this.updateAction ? true : false;
    }


    get updateDialog(): boolean {
        return this._updateDialog;
    }

    get updateBody(): M | null {
        return this._updateBody;
    }

    set updateBody(body: M | null) {
        this._updateBody = body;
    }


    public clickOnRowAction(item: T) {
        return this.openUpdateDialog(item);
    }

    public openUpdateDialog(item: T) {
        if (this.updateAction && this.updateAllowed()) {
            this.updateBody = this.updateAction.itemToUpdateBody(item);
            this.selectedItems = [item];
            this._updateDialog = true;
            this._actionError = null;
        }
    }

    public closeUpdateDialog() {
        this.updateBody = null;
        this._updateDialog = false;
    }

    public submitUpdateForm() {
        if (this.updateBody && this.updateAction && this.selectedItemId) {
            this._loading = true;
            this._actionError = null;
            this.updateAction.updateItemRequest(this.selectedItemId, this.updateBody)
                .then(() => {
                    this._updateDialog = false;
                    return this.reloadData();
                })
                .catch(error => {
                    this._actionError = errorUtils.shortMessage(error);
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
    public createAllowed(): boolean {
        return this.createAction ? true : false;
    }

    get createDialog(): boolean {
        return this._createDialog;
    }

    get createBody(): C | null {
        return this._createBody;
    }


    set createBody(body: C | null) {
        this._createBody = body;
    }

    public openCreateDialog() {
        if (this.createAction) {
            this.selectedItems.length = 0;
            this.createBody = this.createAction.defaultBody();
            this._createDialog = true;
            this._actionError = null;
        }
    }

    public closeCreateDialog() {
        this.createBody = null;
        this._createDialog = false;
    }

    public submitCreateForm() {
        if (this.createBody && this.createAction) {
            this._loading = true;
            this._actionError = null;
            this.createAction.createItemRequest(this.createBody)
                .then(() => {
                    this._createDialog = false;
                    return this.reloadData();
                })
                .catch(error => {
                    this._actionError = errorUtils.shortMessage(error);
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

    public get showSelectCheckbox(): boolean {
        return this.deleteAllowed();
    }

    public deleteAllowed(): boolean {
        return this.deleteAction ? true : false;
    }

    get deleteDialog(): boolean {
        return this._deleteDialog;
    }

    public openDeleteDialogForItem(item: T) {
        if (this.deleteAction && this.deleteAllowed()) {
            this.selectedItems = [item];
            this._deleteDialog = true;
            this._actionError = null;
        }
    }

    public openDeleteDialog() {
        if (this.deleteAction) {
            this._deleteDialog = true;
            this._actionError = null;
        }
    }

    public closeDeleteDialog() {
        this._deleteDialog = false;
    }

    public submitDeleteForm() {
        if (this.deleteAction && this._selectedItems.length > 0) {
            this._loading = true;
            this._actionError = null;
            this.deleteAction.deleteItemRequest(this.selectedItems.map(i => i.id!))
                .then(() => {
                    this._deleteDialog = false;
                    return this.reloadData();
                })
                .catch((error: any) => {
                    this._actionError = errorUtils.shortMessage(error);
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
        this._selectedItems = [];
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

    public reloadHeaders() {
        this.initHeaders();
    }


    public filteredItems(): T[] {
        return this._filter.applyFilter(this._items);
    }

    public doInLoadingSection(action: () => Promise<any>) {
        this._error = null;
        this._loading = true;
        return action().catch(error => {
            this._error = errorUtils.shortMessage(error);
        })
            .finally(() => {
                this._loading = false;
            });
    }

}
