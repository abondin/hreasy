export interface EmployeeOnWorkplace {
    employeeId: number,
    employeeDisplayName: string;
    workplaceName: string;
}

export default class MapPreviewDataContainer {
    private _fullscreen = false;
    private _img: string | null = null;
    private _mapSelectedListener: (() => any) | null = null;
    private _employees: EmployeeOnWorkplace[] = [];


    public show(svg: string, employees?: EmployeeOnWorkplace[]) {
        this._img = svg;
        this._employees = employees || [];
        if (svg) {
            this._fullscreen = true;
            if (this._mapSelectedListener) {
                this._mapSelectedListener();
            }
        }
    }

    set mapSelectedListener(listener: () => any | null) {
        this._mapSelectedListener = listener;
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

}
