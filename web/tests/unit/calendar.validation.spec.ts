import {DateTimeUtils} from "@/components/datetimeutils";

/**
 * Get from production backend
 */
const daysNotIncludedInVacation = ["2022-01-03", "2022-01-04", "2022-01-05", "2022-01-06", "2022-01-07", "2022-02-23", "2022-03-08", "2022-05-09", "2022-11-04", "2023-01-02"
    , "2023-01-03", "2023-01-04", "2023-01-05", "2023-01-06", "2023-02-23", "2023-03-08", "2023-05-01", "2023-05-09", "2023-06-12"];

/**
 * Expected days got from production backend
 * ```
 * c = [];
 * vacationResponseJson.sort((x, y) => x.startDate > y.startDate).forEach(i => {
 *      if (c.map(q => `${q[0]}:${q[1]}`).indexOf(`${i.startDate}:${i.endDate}`) == -1) {
 *           c.push([i.startDate, i.endDate, i.daysNumber])
 *      }});
 * ```
 */
describe("Vacation days validation", () => {
        it.each(
            [
                ["2021-12-17", "2021-12-30", 14], ["2022-01-18", "2022-01-21", 4], ["2022-01-10", "2022-01-22", 13],
                ["2022-02-16", "2022-02-18", 3], ["2022-02-11", "2022-02-24", 14], ["2022-02-24", "2022-02-25", 2],
                ["2022-02-21", "2022-02-27", 7], ["2022-02-14", "2022-02-28", 15], ["2022-02-21", "2022-02-28", 7],
                ["2022-02-24", "2022-03-03", 8], ["2022-02-28", "2022-03-05", 6], ["2022-02-28", "2022-03-06", 7],
                ["2022-03-01", "2022-03-07", 7], ["2022-03-09", "2022-03-10", 2], ["2022-03-09", "2022-03-11", 3],
                ["2022-02-28", "2022-03-14", 14], ["2022-03-09", "2022-03-15", 7], ["2022-03-15", "2022-03-17", 3],
                ["2022-03-14", "2022-03-20", 7], ["2022-03-16", "2022-03-22", 7], ["2022-03-21", "2022-03-27", 7],
                ["2022-03-14", "2022-03-27", 14], ["2022-03-18", "2022-03-31", 14], ["2022-03-31", "2022-04-01", 2],
                ["2022-03-28", "2022-04-03", 7], ["2022-03-21", "2022-04-03", 14], ["2022-03-29", "2022-04-04", 7],
                ["2022-04-04", "2022-04-10", 7], ["2022-03-28", "2022-04-10", 14], ["2022-04-11", "2022-04-17", 7],
                ["2022-04-04", "2022-04-17", 14], ["2022-04-11", "2022-04-24", 14], ["2022-04-18", "2022-04-24", 7],
                ["2022-04-21", "2022-04-27", 7], ["2022-04-25", "2022-04-30", 6], ["2022-04-18", "2022-05-01", 14],
                ["2022-04-18", "2022-05-02", 14], ["2022-04-25", "2022-05-02", 7], ["2022-04-26", "2022-05-03", 7],
                ["2022-05-04", "2022-05-06", 3], ["2022-05-06", "2022-05-06", 1], ["2022-05-04", "2022-05-11", 7],
                ["2022-05-05", "2022-05-12", 7], ["2022-05-11", "2022-05-13", 3], ["2022-05-11", "2022-05-17", 7],
                ["2022-05-11", "2022-05-18", 8], ["2022-05-04", "2022-05-18", 14], ["2022-05-16", "2022-05-22", 7],
                ["2022-05-11", "2022-05-24", 14], ["2022-05-15", "2022-05-28", 14], ["2022-05-16", "2022-05-29", 14],
                ["2022-05-23", "2022-05-29", 7], ["2022-05-30", "2022-06-05", 7], ["2022-06-02", "2022-06-08", 7],
                ["2022-06-01", "2022-06-10", 10], ["2022-06-03", "2022-06-10", 8], ["2022-06-06", "2022-06-12", 7],
                ["2022-06-06", "2022-06-13", 7], ["2022-05-30", "2022-06-13", 14], ["2022-05-31", "2022-06-14", 14],
                ["2022-06-06", "2022-06-19", 14], ["2022-06-14", "2022-06-19", 6], ["2022-06-13", "2022-06-19", 7],
                ["2022-06-06", "2022-06-20", 14], ["2022-06-14", "2022-06-20", 7], ["2022-06-20", "2022-06-26", 7],
                ["2022-06-13", "2022-06-26", 14], ["2022-06-14", "2022-06-27", 14], ["2022-06-13", "2022-06-27", 15],
                ["2022-06-15", "2022-06-28", 14], ["2022-06-14", "2022-06-28", 15], ["2022-06-15", "2022-06-30", 16],
                ["2022-06-27", "2022-07-01", 5], ["2022-06-27", "2022-07-03", 7], ["2022-06-20", "2022-07-03", 14],
                ["2022-07-04", "2022-07-04", 1], ["2022-07-01", "2022-07-04", 4], ["2022-06-30", "2022-07-08", 9],
                ["2022-06-20", "2022-07-08", 19], ["2022-07-04", "2022-07-08", 5], ["2022-06-27", "2022-07-10", 14],
                ["2022-07-04", "2022-07-10", 7], ["2022-07-01", "2022-07-14", 14], ["2022-07-04", "2022-07-17", 14],
                ["2022-07-11", "2022-07-17", 7], ["2022-06-27", "2022-07-17", 21], ["2022-07-06", "2022-07-18", 13],
                ["2022-07-06", "2022-07-19", 14], ["2022-07-14", "2022-07-20", 7], ["2022-07-14", "2022-07-21", 8],
                ["2022-07-18", "2022-07-21", 4], ["2022-07-11", "2022-07-24", 14], ["2022-07-04", "2022-07-24", 21],
                ["2022-07-18", "2022-07-24", 7], ["2022-07-18", "2022-07-28", 11], ["2022-07-15", "2022-07-28", 14],
                ["2022-07-18", "2022-07-31", 14], ["2022-07-25", "2022-07-31", 7], ["2022-07-04", "2022-07-31", 28],
                ["2022-07-22", "2022-08-05", 15], ["2022-07-27", "2022-08-05", 10], ["2022-08-01", "2022-08-07", 7],
                ["2022-07-25", "2022-08-07", 14], ["2022-07-11", "2022-08-07", 28], ["2022-07-18", "2022-08-07", 21],
                ["2022-07-25", "2022-08-10", 17], ["2022-08-01", "2022-08-14", 14], ["2022-08-08", "2022-08-14", 7],
                ["2022-08-19", "2022-08-19", 1], ["2022-08-08", "2022-08-21", 14], ["2022-08-15", "2022-08-21", 7],
                ["2022-08-22", "2022-08-22", 1], ["2022-08-15", "2022-08-24", 10], ["2022-08-15", "2022-08-28", 14],
                ["2022-08-08", "2022-08-28", 21], ["2022-08-22", "2022-08-28", 7], ["2022-08-22", "2022-08-30", 9],
                ["2022-08-08", "2022-08-30", 23], ["2022-08-15", "2022-08-30", 16], ["2022-08-15", "2022-08-31", 17],
                ["2022-08-18", "2022-08-31", 14], ["2022-08-19", "2022-09-01", 14], ["2022-08-30", "2022-09-01", 3],
                ["2022-09-01", "2022-09-02", 2], ["2022-09-02", "2022-09-02", 1], ["2022-08-29", "2022-09-04", 7],
                ["2022-08-22", "2022-09-04", 14], ["2022-09-05", "2022-09-05", 1], ["2022-09-08", "2022-09-09", 2],
                ["2022-08-29", "2022-09-11", 14], ["2022-09-05", "2022-09-11", 7], ["2022-08-22", "2022-09-11", 21]
                , ["2022-09-05", "2022-09-12", 8], ["2022-09-05", "2022-09-13", 9], ["2022-09-01", "2022-09-14", 14],
                ["2022-09-01", "2022-09-16", 16], ["2022-09-05", "2022-09-18", 14], ["2022-09-12", "2022-09-18", 7],
                ["2022-09-14", "2022-09-20", 7], ["2022-09-19", "2022-09-21", 3], ["2022-09-23", "2022-09-23", 1],
                ["2022-09-19", "2022-09-23", 5], ["2022-09-19", "2022-09-25", 7], ["2022-09-12", "2022-09-25", 14]
                , ["2022-09-20", "2022-09-26", 7], ["2022-09-14", "2022-09-27", 14], ["2022-09-22", "2022-09-28", 7],
                ["2022-09-15", "2022-09-28", 14], ["2022-09-30", "2022-09-30", 1], ["2022-09-19", "2022-10-02", 14],
                ["2022-09-26", "2022-10-02", 7], ["2022-09-05", "2022-10-02", 28], ["2022-09-27", "2022-10-03", 7],
                ["2022-10-03", "2022-10-09", 7], ["2022-09-26", "2022-10-09", 14], ["2022-10-03", "2022-10-13", 11],
                ["2022-10-07", "2022-10-13", 7], ["2022-10-10", "2022-10-16", 7], ["2022-10-03", "2022-10-16", 14],
                ["2022-10-03", "2022-10-17", 15], ["2022-10-05", "2022-10-18", 14], ["2022-10-18", "2022-10-20", 3],
                ["2022-10-17", "2022-10-21", 5], ["2022-10-10", "2022-10-22", 13], ["2022-10-17", "2022-10-23", 7],
                ["2022-10-10", "2022-10-23", 14], ["2022-10-24", "2022-10-27", 4], ["2022-10-17", "2022-10-29", 13],
                ["2022-10-24", "2022-10-30", 7], ["2022-10-17", "2022-10-30", 14], ["2022-10-03", "2022-10-30", 28],
                ["2022-10-18", "2022-10-31", 14], ["2022-10-31", "2022-11-02", 3], ["2022-10-31", "2022-11-03", 4],
                ["2022-10-28", "2022-11-03", 7], ["2022-11-02", "2022-11-03", 2], ["2022-10-24", "2022-11-03", 11],
                ["2022-11-04", "2022-11-04", 1], ["2022-10-31", "2022-11-05", 5], ["2022-10-31", "2022-11-06", 7],
                ["2022-10-24", "2022-11-07", 14], ["2022-10-31", "2022-11-07", 7], ["2022-11-07", "2022-11-10", 4],
                ["2022-11-07", "2022-11-11", 5], ["2022-11-05", "2022-11-11", 7], ["2022-11-01", "2022-11-12", 11],
                ["2022-11-07", "2022-11-13", 7], ["2022-10-31", "2022-11-14", 14], ["2022-11-01", "2022-11-15", 14],
                ["2022-11-07", "2022-11-16", 10], ["2022-11-03", "2022-11-18", 15], ["2022-11-14", "2022-11-20", 7],
                ["2022-11-07", "2022-11-20", 14], ["2022-11-09", "2022-11-22", 14], ["2022-11-10", "2022-11-23", 14],
                ["2022-11-07", "2022-11-23", 17], ["2022-11-17", "2022-11-23", 7], ["2022-11-21", "2022-11-25", 5],
                ["2022-11-14", "2022-11-26", 13], ["2022-11-21", "2022-11-26", 6], ["2022-11-14", "2022-11-27", 14],
                ["2022-11-21", "2022-11-27", 7], ["2022-11-22", "2022-11-28", 7], ["2022-11-28", "2022-12-04", 7],
                ["2022-11-21", "2022-12-04", 14], ["2022-11-14", "2022-12-04", 21], ["2022-11-29", "2022-12-05", 7],
                ["2022-12-05", "2022-12-06", 2], ["2022-12-01", "2022-12-07", 7], ["2022-11-25", "2022-12-08", 14],
                ["2022-11-28", "2022-12-11", 14], ["2022-12-05", "2022-12-11", 7], ["2022-12-01", "2022-12-14", 14],
                ["2022-12-05", "2022-12-15", 11], ["2022-12-05", "2022-12-18", 14], ["2022-12-12", "2022-12-18", 7],
                ["2022-12-06", "2022-12-19", 14], ["2022-12-13", "2022-12-19", 7], ["2022-12-21", "2022-12-23", 3],
                ["2022-12-20", "2022-12-23", 4], ["2022-12-19", "2022-12-24", 6], ["2022-12-19", "2022-12-25", 7],
                ["2022-12-12", "2022-12-25", 14], ["2022-12-23", "2022-12-29", 7], ["2022-12-16", "2022-12-29", 14],
                ["2022-12-16", "2022-12-30", 15], ["2022-12-19", "2022-12-30", 12], ["2022-12-23", "2022-12-30", 8],
                ["2022-12-17", "2022-12-30", 14], ["2022-12-27", "2022-12-30", 4], ["2022-12-28", "2022-12-30", 3],
                ["2022-12-21", "2022-12-30", 10], ["2022-12-26", "2022-12-30", 5], ["2022-12-22", "2022-12-30", 9],
                ["2022-12-24", "2022-12-30", 7], ["2022-12-29", "2022-12-30", 2], ["2022-12-21", "2022-12-31", 11],
                ["2022-12-25", "2022-12-31", 7], ["2022-12-26", "2022-12-31", 6], ["2022-12-29", "2022-12-31", 3]
            ]
        )('Verify vacation dates %#:  [%s : %s] - %s', (startDate: string, endDate: string, expectedDays: number) =>
            expect(DateTimeUtils.vacationDays(
                DateTimeUtils.dateFromIsoString(startDate),
                DateTimeUtils.dateFromIsoString(endDate),
                daysNotIncludedInVacation
            )).toBe(expectedDays)
        );
    }
);