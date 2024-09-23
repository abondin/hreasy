import {errorUtils} from "@/components/errors";
import logger from "@/logger";

/**
 * Wrapper for item action in dialog.
 * Use to update, create, update item
 */
export class InDialogActionDataContainer<ID,T> {
    public dialog = false;
    public loading = false;
    public error: string | null = null;

    protected _formData: T | null = null;
    protected _itemId: ID | null = null;

    constructor(private submitAction: (itemId: ID | null, item: T | null) => Promise<any>) {
    }

    get formData() {
        return this._formData;
    }

    public openDialog(itemId: ID | null, formData: T | null) {
        this._formData = formData;
        this._itemId = itemId;
        this.dialog = true;
        this.error = null;
        this.loading = false;
    }

    public submit(successHandler?: () => any): Promise<any> {
        this.loading = true;
        this.error = null;
        return this.submitAction(this._itemId, this._formData).then(() => {
            this.close()
            if (successHandler) {
                successHandler();
            }
        }).catch((error: any) => {
            this.error = errorUtils.shortMessage(error);
        }).finally(()=>{
            this.loading = false;
        })
    }

    public cancel() {
        this.close();
    }


    private close() {
        this.dialog = false;
        this.error = null;
        this.loading = false;
        this._formData = null;
        this._itemId = null;
    }
}
