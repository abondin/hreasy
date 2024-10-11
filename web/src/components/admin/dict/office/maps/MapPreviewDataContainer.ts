import dictService from "@/store/modules/dict.service";
import employeeService, {Employee} from "@/components/empl/employee.service";
import {errorUtils} from "@/components/errors";

export default class MapPreviewDataContainer {
    private _opened = false;
    private _loading = false;
    private _error = '';
    private _img: string | null = null;
    private _filename: string | null = null;
    private _mapReadyListener: (() => any) | null = null;
    private _employees: Employee[] = [];
    private _highlightedWorkplace: string[] = [];
    private _title: string | null = null;
    private _employeeDetailedOpened = false;
    private _selectedEmployee: Employee | null = null;


    public show(filename: string, title?: string,highlightedWorkplace?: string[]) {
        this._img = null;
        this._employees = [];
        this._highlightedWorkplace = highlightedWorkplace || [];
        this._filename = filename;
        this._loading = true;
        this._error = '';
        this._opened = true;
        this._title = title || null;
        this._employeeDetailedOpened = false;
        this._selectedEmployee = null;
        return dictService.getOfficeLocationMapFile(filename).then(img => {
            if (img) {
                this._img = img;
                return employeeService.findAll().then(allEmployees => {
                    this._employees = allEmployees.filter(e => e.officeLocation?.mapName === filename);
                    return this._employees;
                });
            } else {
                throw new Error('Map not found');
            }
        }).catch(err => {
            this._error = errorUtils.shortMessage(err);
        }).finally(() => {
            this._loading = false;
        }).then(() => {
            if (this._mapReadyListener) {
                return this._mapReadyListener();
            }
        });
    }

    public set mapReadyListener(listener: () => any | null) {
        this._mapReadyListener = listener;
    }

    public hide() {
        this._opened = false;
        this._img = null;
    }

    public openEmployeeDetails(employee: Employee) {
        this._selectedEmployee = employee;
        this._employeeDetailedOpened = true;
    }

    public get img(): string | null {
        return this._img;
    }

    public get employees(): Employee[] {
        return this._employees;
    }

    public get highlightedWorkplace(): string[] {
        return this._highlightedWorkplace;
    }

    public get opened(): boolean {
        return this._opened;
    }

    public set opened(opened: boolean) {
        this._opened = opened;
    }

    public get title(): string | null {
        return this._title;
    }

    public get selectedEmployee(): Employee | null {
        return this._selectedEmployee;
    }

    public get employeeDetailedOpened(): boolean {
        return this._employeeDetailedOpened;
    }

    public set employeeDetailedOpened(employeeDetailedOpened: boolean) {
        this._employeeDetailedOpened = employeeDetailedOpened;
    }


    public get loading(): boolean {
        return this._loading;
    }

    public get error(): string {
        return this._error;
    }

    public get filename() {
        return this._filename;
    }
}
