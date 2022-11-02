package ru.abondin.hreasy.platform.service.dict.dto;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

/**
 * http://xmlcalendar.ru/
 */
@Data
public class DictWorkingDaysCalendarDto {

    @RequiredArgsConstructor
    @Getter
    public enum WorkingDayType {
        /**
         * Any holiday like New Year
         */
        HOLIDAY(1),
        /**
         * Working day in weekend
         */
        WORKING_DAY(2),
        /**
         * <p>
         * For example 2022-01-01 is saturday.
         * And the holiday moved to 2022-05-03.
         * So, 2022-05-03 will have type 4
         * </p>
         * <p>
         * In http://xmlcalendar.ru/ it marks as t=1 with additional f="01.01" field:
         * </p>
         * <pre>
         * {@code
         *     <day d="05.03" t="1" f="01.01"/>
         * }
         * </pre>
         */
        MOVED_HOLIDAY(4);
        private final int type;

        public static boolean isNotWorking(int type) {
            return type == HOLIDAY.type || type == MOVED_HOLIDAY.type;
        }
    }

    private LocalDate day;
    /**
     * <ul>
     *   <li>1 - holiday</li>
     *   <li>2 - working day (can be weekend)</li>
     *   <li>3 - working day (weekend)</li>
     *   <li>no day found -
     *      <ul>
     *          <li>monday-friday - working days</li>
     *          <li>saturday,sunday - weekend days</li>
     *      </ul>
     *   </li>
     * </ul>
     */
    private int type;
}
