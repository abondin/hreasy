import {DateTimeUtils} from "@/components/datetimeutils";
import moment, {Moment} from "moment";
import logger from "@/logger";

describe("isSameDate", () => {
        it.each([
                {
                    d1: moment("20230501", "YYYYMMDD"),
                    d2: moment("20230501", "YYYYMMDD"),
                    theSame: true
                },
                {
                    d1: moment("20230503", "YYYYMMDD"),
                    d2: moment("20230503", "YYYYMMDD"),
                    theSame: true
                },
                {
                    d1: moment("20230503", "YYYYMMDD"),
                    d2: moment("20230510", "YYYYMMDD"),
                    theSame: false
                }
            ]
        )('should be the same %p ', (input: { d1: Moment, d2: Moment, theSame: boolean }) =>
            expect(DateTimeUtils.isSameDate(input.d1, input.d2)).toBe(input.theSame)
        );
    }
);

describe("daysBetweenDates", () => {
        test('test days between 01.05.2023-14.05.2023', (() => {
                const start = moment("20230501", "YYYYMMDD");
                const end = moment("20230514", "YYYYMMDD");
                const dates = DateTimeUtils.daysBetweenDates(start, end);
                expect(dates.length).toBe(14);
                expect(dates[2].date()).toBe(3);
                expect(dates[6].date()).toBe(7);
            }
        ));
    }
);
