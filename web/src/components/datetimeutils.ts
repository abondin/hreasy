import moment, {Moment} from "moment";
import {DataTableCompareFunction} from "vuetify";

export class DateTimeUtils {
    public static DEFAULT_DATE_PATERN = "DD.MM.YY";
    public static DEFAULT_DATE_TIME_PATERN = "DD.MM.YY HH:mm";

    public static dateFromIsoString(isoDate: string): Moment {
        return moment(isoDate, moment.HTML5_FMT.DATE, true);
    }

    public static dateTimeFromIsoString(isoDate: string): Moment {
        return moment(isoDate, moment.ISO_8601, true);
    }

    public static dateFromFormattedString(formatedDate: string): Moment {
        return moment(formatedDate, this.DEFAULT_DATE_PATERN, true);
    }

    public static validateFormattedDate(formattedDate: string, allowEmpty = true) {
        if (!formattedDate) {
            return allowEmpty;
        }
        return moment(formattedDate, this.DEFAULT_DATE_PATERN, true).isValid();
    }

    public static formatFromIso(isoDateString: string | undefined): string {
        if (!isoDateString) {
            return '';
        }
        const d = this.dateFromIsoString(isoDateString);
        if (d.isValid()) {
            return d.format(this.DEFAULT_DATE_PATERN);
        } else {
            return '';
        }
    }

    public static formatDateTimeFromIso(isoDateTimeString: string | undefined): string {
        if (!isoDateTimeString) {
            return '';
        }
        const d = this.dateTimeFromIsoString(isoDateTimeString);
        if (d.isValid()) {
            return d.format(this.DEFAULT_DATE_TIME_PATERN);
        } else {
            return '';
        }
    }

    public static dateComparatorNullLast: DataTableCompareFunction<string> = (a, b) => {
        if (!a && !b) {
            return 0;
        }
        if (!a) {
            return 1;
        }
        if (!b) {
            return -1;
        }
        const d1 = DateTimeUtils.dateFromIsoString(a);
        const d2 = DateTimeUtils.dateFromIsoString(b);
        return d1 == d2 ?
            0 : (d1 < d2 ? 1 : -1);
    }
}


