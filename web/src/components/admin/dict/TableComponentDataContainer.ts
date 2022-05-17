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
 * Data container for table component
 */
export default class TableComponentDataContainer<T extends WithId, M extends UpdateBody, F extends Filter<T>> {
    private _loading = false;
    private _error: string | null = null;
    private _editDialog = false;
    private _editError: string | null = null;

    private _items: T[] = [];
    private _updateBody: M | null = null;
    private _updateItemId: number | null | undefined = null;

    private _headers: DataTableHeader[] = [];

    private _defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;

    private _initialized = false;

    constructor(private dataLoader: () => Promise<Array<T>>,
                private headerLoader: () => DataTableHeader[],
                private updateItemRequest: (id: number | null | undefined, body: M) => Promise<T>,
                private itemToUpdateBody: (item: T) => M,
                private newUpdateBody: () => M,
                private _filter: F) {
    }

    public init() {
        this.initHeaders();
        this._initialized= true;
        return this.reloadData();
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

    public openEditDialog(item: T | null) {
        this._updateBody = item == null ? this.newUpdateBody() : this.itemToUpdateBody(item);
        this._updateItemId = (item == null ? null : item.id);
        this._editDialog = true;
        this._editError = null;
    }

    public closeEditDialog() {
        this._updateBody = null;
        this._editDialog = false;
    }

    public isEditItemNew(): boolean {
        return !this._updateItemId;
    }

    get loading(): boolean {
        return this._loading;
    }

    get error(): string | null {
        return this._error;
    }

    get editError(): string | null {
        return this._editError;
    }

    get editDialog(): boolean {
        return this._editDialog;
    }

    get items(): T[] {
        return this._items;
    }

    get updateBody(): M | null {
        return this._updateBody;
    }

    get headers(): DataTableHeader[] {
        return this._headers;
    }

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

    public submitEditForm() {
        if (this._updateBody) {
            this._loading = true;
            this._editError = null;
            this.updateItemRequest(this._updateItemId, this._updateBody)
                .then(() => {
                    this._editDialog = false;
                    return this.reloadData();
                })
                .catch(error => {
                    this._editError = errorUtils.shortMessage(error);
                })
                .finally(() => {
                    this._loading = false;
                });
        } else {
            logger.error("Unable to submit edit form without selected item to edit");
        }
    }

    public filteredItems(): T[] {
        return this._filter.applyFilter(this._items);
    }

}
