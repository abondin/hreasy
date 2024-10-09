import dictService from "@/store/modules/dict.service";
import employeeService from "@/components/empl/employee.service";
import {errorUtils} from "@/components/errors";

export interface EmployeeOnWorkplace {
    employeeId: number,
    employeeDisplayName: string;
    workplaceName: string;
    selected?: boolean;
}

export default class MapPreviewDataContainer {
    private _fullscreen = false;
    private _loading = false;
    private _error = '';
    private _img: string | null = null;
    private _mapReadyListener: (() => any) | null = null;
    private _employees: EmployeeOnWorkplace[] = [];


    public show(officeLocationId: number, selectedEmployeeId?: number) {
        this._img = null;
        this._employees = [];
        this._loading = true;
        this._error = '';
        this._fullscreen = true;
        dictService.getOfficeLocationMap(officeLocationId).then(img => {
            if (img) {
                this._img = img;
                return employeeService.findAll().then(allEmployees => {
                    this._employees = allEmployees.filter(e => e.officeLocation?.id === officeLocationId).map(e => {
                        return {
                            employeeId: e.id,
                            employeeDisplayName: e.displayName,
                            workplaceName: e.officeWorkplace,
                            selected: e.id === selectedEmployeeId
                        }
                    });
                    this._loading = false;
                    if (this._mapReadyListener) {
                        this._mapReadyListener();
                    }
                    return this._employees;
                });
            } else {
                throw new Error('Map not found');
            }
        }).catch(err => {
            this._error = errorUtils.shortMessage(err);
            this._loading = false;
        });
    }

    set mapReadyListener(listener: () => any | null) {
        this._mapReadyListener = listener;
    }

    public hide() {
        this._fullscreen = false;
        this._img = null;
    }

    get img(): string | null {
        return this._img;
    }

    get employees(): EmployeeOnWorkplace[] {
        return this._employees;
    }

    get fullscreen(): boolean {
        return this._fullscreen;
    }

    get loading(): boolean {
        return this._loading;
    }

    get error(): string {
        return this._error;
    }
}
