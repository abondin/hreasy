import {DateTimeUtils} from "@/components/datetimeutils";
import moment, {Moment} from "moment";

/**
 * Get from production backend
 */
const daysNotIncludedInVacation = ["2022-01-03", "2022-01-04", "2022-01-05", "2022-01-06", "2022-01-07", "2022-02-23", "2022-03-08", "2022-05-09", "2022-11-04", "2023-01-02"
    , "2023-01-03", "2023-01-04", "2023-01-05", "2023-01-06", "2023-02-23", "2023-03-08", "2023-05-01", "2023-05-09", "2023-06-12"];

const testDataRaw = require('./vacations2022-from-erp.json') as Array<string>;

/**
 * Read expected days got from ERP in csv format.
 * <ul>
 *     <li>Converted to json with simple regexp
 * \n
 * to
 * "],\n["
 * </li>
 * <li>update date format to ISO string
 *  "(\d\d)\.(\d\d)\.(\d\d\d\d)"
 *  to "$3-$2-$1"
 * </ul>
 *-------------------------------
 * <p>
 *     <ul>
 *         <li>
 * Read the array of the strings "startDate- ISO Date;endDate - ISO Date;expectedDays - Number"
 * </li>
 *         <li>
 * Convert  ISO date string to moment object + add the dates in string format to pretty print
 * </li>
 *         <li>
 * Calculate actual vacations days
 * </li>
 *         <li>
 * Order by start date and then by end date
 * </li>
 *         <li>
 * Remove duplicates by start and end dates
 * </li>
 * </ul>
 * </p>
 */
const testData = testDataRaw.map((line, index) => {
    const args = line.split(";");
    const start = DateTimeUtils.dateFromIsoString(args[0]);
    const end = DateTimeUtils.dateFromIsoString(args[1]);
    const expected = Number.parseInt(args[2]);
    return {
        row : index+1,
        startStr: start.format("DD.MM.YYYY"),
        endStr: end.format("DD.MM.YYYY"),
        expected: expected,
        actual: DateTimeUtils.vacationDays(
            start,
            end,
            daysNotIncludedInVacation
        ),
        start: DateTimeUtils.dateFromIsoString(args[0]),
        end: DateTimeUtils.dateFromIsoString(args[1])
    };
}).sort((a, b) => {
        if (a.start > b.start) {
            return 1;
        } else if (a.start < b.start) {
            return -1;
        } else if (a.end > b.end) {
            return 1;
        } else if (a.end < b.end) {
            return -1;
        } else {
            return 0;
        }

    }
).filter((e, i, array)=>(i==0 || e.start.diff(array[i-1].start, 'days')!=0 || e.end.diff(array[i-1].end, 'days')!=0))
    .map(d=>[d.row, d.startStr, d.endStr, d.expected, d.actual]);


describe("Vacation days validation", () => {
        it.each(testData
        )('Row %s - [%s : %s] - expected: %s days; actual:%s days', (...args) =>
            expect(args[3]).toBe(args[4] as number)
        );
    }
);
