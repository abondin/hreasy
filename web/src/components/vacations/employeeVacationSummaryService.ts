import {Vacation} from "@/components/vacations/vacation.service";
import {DateTimeUtils} from "@/components/datetimeutils";
import {CurrentProjectDict} from "@/components/empl/employee.service";

export class EmployeeVacationSummary {
    public employee: number;
    public employeeDisplayName: string;
    public employeeCurrentProject: CurrentProjectDict;
    public year: number;

    public upcomingVacation: Vacation | null = null;
    public vacationsNumber = 0;
    public vacationsTotalDays = 0;

    public constructor(vacation: Vacation) {
        this.employee = vacation.employee;
        this.employeeDisplayName = vacation.employeeDisplayName;
        this.employeeCurrentProject = vacation.employeeCurrentProject;
        this.year = vacation.year;
    }

}


export interface EmployeeVacationSummaryMapper {
    map(input: Vacation[]): EmployeeVacationSummary[];
}

class EmployeeVacationSummaryMapperImpl implements EmployeeVacationSummaryMapper {
    public map(input: Vacation[]): EmployeeVacationSummary[] {
        const result: EmployeeVacationSummary[] = [];
        if (!input) {
            return result;
        }
        const groupedByEmployee: Map<number, Vacation[]> = new Map();
        // 1. Group all vacation by employee
        input.forEach(v => {
            // 1.1 Ignore canceled and rejected statuses
            if (v.status == "PLANNED" || v.status == "TAKEN" || v.status == "COMPENSATION" || v.status == "REQUESTED") {
                const collection = groupedByEmployee.get(v.employee);
                if (!collection) {
                    groupedByEmployee.set(v.employee, [v]);
                } else {
                    collection.push(v);
                }
            }
        });

        // 2. Calculate aggregates and find upcoming vacation
        groupedByEmployee.forEach((vacations, employee) => {
            result.push(this.mapForOneEmployee(vacations));
        });
        return result;
    }


    private mapForOneEmployee(vacations: Vacation[]): EmployeeVacationSummary {
        let result: EmployeeVacationSummary | null = null;
        vacations.forEach(v => {
            if (!result) {
                result = new EmployeeVacationSummary(v);
            }
            result.vacationsNumber++;
            result.vacationsTotalDays += v.daysNumber;
            result.upcomingVacation = this.upcomingVacation(result.upcomingVacation, v);
        });
        return result!;
    }

    /**
     * Choose nearest upcoming vacation. Upcoming also means a vacation that is going right now
     * @param current - a vacation that is considered upcoming  right now
     * @param newCandidate - new candidate
     * @private
     * @return newCandidate or current depends which one is nearest upcoming
     */
    private upcomingVacation(current: Vacation | null, newCandidate: Vacation): Vacation | null {
        const start = DateTimeUtils.dateFromIsoString(newCandidate.startDate);
        const end = DateTimeUtils.dateFromIsoString(newCandidate.endDate);
        const now = DateTimeUtils.now();
        //1. Check that candidate is upcoming
        if (now.isAfter(start) && now.isAfter(end)) {
            return current;
        }
        //2. Check that candidate starts before current
        if (!current || start.isBefore(DateTimeUtils.dateFromIsoString(current.startDate))) {
            return newCandidate;
        } else {
            return current;
        }
    }
}

export const employeeVacationSummaryMapper: EmployeeVacationSummaryMapper = new EmployeeVacationSummaryMapperImpl();
