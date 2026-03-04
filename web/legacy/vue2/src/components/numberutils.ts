import moment, {Moment} from "moment";
import {DataTableCompareFunction} from "vuetify";

export class NumberUtils {

    static formatMoney(value: string|number|null|undefined): string {
        if (!value){
            return "";
        }
        return Number(value).toLocaleString();
    }

}


