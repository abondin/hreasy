import dictService, {OfficeOrOfficeLocationMap} from "@/store/modules/dict.service";
import {errorUtils} from "@/components/errors";


export default class MapsTableDataContainer {
    private _loading = false;
    private _error = '';
    private _maps: OfficeOrOfficeLocationMap[] = [];


    public reload() {
        dictService.getOfficeLocationMaps().then(result => {
            this._maps = result;
        }).catch(err => {
            this._error = errorUtils.shortMessage(err);
        }).finally(() => {
            this._loading = false;
        });
    }

    get maps() {
        return this._maps;
    }

    get loading(): boolean {
        return this._loading;
    }

    get error(): string {
        return this._error;
    }


}
