import moment, {Moment} from "moment";
import {DataTableCompareFunction} from "vuetify";
import en from "vuetify/src/locale/en";

export class DateTimeUtils {
    public static DEFAULT_DATE_PATTERN = "DD.MM.YYYY";
    public static DEFAULT_DATE_TIME_PATTERN = "DD.MM.YYYY HH:mm";


    public static dateFromIsoString(isoDate: string): Moment {
        return moment(isoDate, moment.HTML5_FMT.DATE, true);
    }

    public static dateTimeFromIsoString(isoDate: string): Moment {
        return moment(isoDate, moment.ISO_8601, true);
    }

    public static dateFromFormattedString(formatedDate: string): Moment {
        return moment(formatedDate, this.DEFAULT_DATE_PATTERN, true);
    }

    public static formatToIsoDate(date?: Moment): string | undefined {
        return date ? date.format(moment.HTML5_FMT.DATE) : undefined;
    }

    public static validateFormattedDate(formattedDate: string, allowEmpty = true) {
        if (!formattedDate) {
            return allowEmpty;
        }
        return moment(formattedDate, this.DEFAULT_DATE_PATTERN, true).isValid();
    }

    public static formatFromIso(isoDateString: string | undefined): string {
        if (!isoDateString) {
            return '';
        }
        const d = this.dateFromIsoString(isoDateString);
        if (d.isValid()) {
            return d.format(this.DEFAULT_DATE_PATTERN);
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
            return d.format(this.DEFAULT_DATE_TIME_PATTERN);
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

    public static now(): Moment {
        return moment();
    }

    public static nowDateIso(): string|undefined {
        return this.formatToIsoDate(this.now());
    }

    static vacationDays(start: moment.Moment, end: moment.Moment, notVacationDaysStr:Array<string>) {
        const notVacationDays = notVacationDaysStr.map(str=>this.dateFromIsoString(str));
        let cnt = 0;
        for (let day = moment(start); day.diff(end, 'days') <= 0; day.add(1,'day')){
            if (notVacationDays.filter(d=>d.isSame(day)).length == 0){
                cnt = cnt + 1;
            }
        }
        return cnt;
    }
}


