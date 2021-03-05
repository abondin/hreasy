import moment, {Moment} from "moment";

export class DateTimeUtils {
    public static DEFAULT_DATE_PATERN = "DD.MM.YY";

    public static dateFromIsoString(isoDate: string): Moment {
        return moment(isoDate, moment.HTML5_FMT.DATE, true);
    }

    public static dateFromFormattedString(formatedDate: string): Moment {
        return moment(formatedDate, this.DEFAULT_DATE_PATERN, true);
    }

    public static validateFormattedDate(formattedDate: string, allowEmpty = true){
        if (!formattedDate){
            return allowEmpty;
        }
        return moment(formattedDate, this.DEFAULT_DATE_PATERN, true).isValid();
    }

    public static formatFromIso(isoDateString: string|undefined):string{
        if (!isoDateString){
            return '';
        }
        const d = this.dateFromIsoString(isoDateString);
        if (d.isValid()){
            return d.format(this.DEFAULT_DATE_PATERN);
        } else {
            return '';
        }
    }
}


