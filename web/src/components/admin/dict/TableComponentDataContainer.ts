import {UiConstants} from "@/components/uiconstants";
import {DataTableHeader} from "vuetify";
import {errorUtils} from "@/components/errors";


/**
 * Data container for table component
 */
export default class TableComponentDataContainer<T> {
    private _loading = false;
    private _error: string | null = null;
    private _deleteDialog = false;
    private _editDialog = false;

    private _items: T[] = [];
    private _itemToEdit: T | null = null;
    private _itemToDelete: T | null = null;

    private _headers: DataTableHeader[] = [];

    private _defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;


    constructor(private dataLoader: () => Promise<Array<T>>,
                private headerLoader: () => DataTableHeader[]) {
    }

    get defaultItemsPerTablePage(): number {
        return this._defaultItemsPerTablePage;
    }

    public init() {
        this.reloadHeaders();
        this.reloadData();
    }

    public openEditDialog(item: T) {
        this._itemToEdit = item;
        this._editDialog = true;
    }

    public openDeleteDialog(item: T) {
        this._itemToDelete = item;
        this._deleteDialog = true;
    }

    public reloadHeaders() {
        this._headers.length = 0;
        this._headers = this.headerLoader();
    }

    public reloadData() {
        this._error = null;
        this._loading = true;
        this._items.length = 0;
        this.dataLoader()
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

    get loading(): boolean {
        return this._loading;
    }

    get error(): string | null {
        return this._error;
    }

    get deleteDialog(): boolean {
        return this._deleteDialog;
    }

    get editDialog(): boolean {
        return this._editDialog;
    }

    get items(): T[] {
        return this._items;
    }

    get itemToEdit(): T | null {
        return this._itemToEdit;
    }

    get itemToDelete(): T | null {
        return this._itemToDelete;
    }

    get headers(): DataTableHeader[] {
        return this._headers;
    }

}
