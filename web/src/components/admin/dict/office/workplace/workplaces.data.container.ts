import dictAdminService, {DictOfficeWorkplace} from "@/components/admin/dict/dict.admin.service";
import dictService from "@/store/modules/dict.service";
import {errorUtils} from "@/components/errors";
import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import WorkplaceCreateOrUpdateAction from "@/components/admin/dict/office/workplace/workplace.create-update.actions";
import {WorkplacesFilterContainer} from "@/components/admin/dict/office/workplace/WorkplacesFilterComponent.vue";

export interface WorkplacesOnMapContainer {
    get officeLocationMap(): string | null;

    get workplaces(): DictOfficeWorkplace[];

    get selectedWorkplace(): DictOfficeWorkplace | null;

    set selectedWorkplace(workplace: DictOfficeWorkplace | null);

    get selectOnMapModeEnabled(): boolean;
}

export interface SingleWorkplaceDataContainer {
    get selectedWorkplace(): DictOfficeWorkplace | null;

    get selectOnMapModeEnabled(): boolean;

    set selectOnMapModeEnabled(value: boolean);
}


export default class WorkplacesDataContainer implements WorkplacesOnMapContainer, SingleWorkplaceDataContainer {
    private readonly _filter: WorkplacesFilterContainer;
    private _officeLocationMap: string | null = null;
    private _workplaces: DictOfficeWorkplace[] = [];
    private _selectedWorkplace: DictOfficeWorkplace | null = null;
    private _selectOnMapModeEnabled=false;

    private _uploadSvgMapDialog = false;

    private _loading = false;
    private _error: string | null = null;


    private readonly _deleteMapAction: InDialogActionDataContainer<number, void>;
    private readonly _createOrUpdateWorkplaceAction: WorkplaceCreateOrUpdateAction;

    constructor() {
        this._filter = new WorkplacesFilterContainer();
        this._deleteMapAction = new InDialogActionDataContainer((officeLocationId: number | null) => {
            if (officeLocationId) {
                return dictAdminService.deleteOfficeLocationMap(officeLocationId);
            } else {
                return Promise.resolve();
            }
        });
        this._createOrUpdateWorkplaceAction = new WorkplaceCreateOrUpdateAction();
    }

    get error() {
        return this._error;
    }

    get loading() {
        return this._loading;
    }

    get filter() {
        return this._filter;
    }

    get workplaces() {
        return this.filter.officeLocationId ? this._workplaces.filter(w => w.officeLocation?.id == this.filter.officeLocationId && !w.archived) : [];
    }

    get officeLocationMap() {
        return this._officeLocationMap;
    }

    get createOrUpdateWorkplaceAction() {
        return this._createOrUpdateWorkplaceAction;
    }

    get deleteMapAction() {
        return this._deleteMapAction;
    }

    get selectedWorkplace(): DictOfficeWorkplace | null {
        return this._selectedWorkplace;
    }

    set selectedWorkplace(workplace: DictOfficeWorkplace | null) {
        this._selectOnMapModeEnabled = false;
        this._selectedWorkplace = workplace;
    }

    get selectOnMapModeEnabled(): boolean {
        return this._selectOnMapModeEnabled;
    }

    set selectOnMapModeEnabled(value){
        this._selectOnMapModeEnabled = value;
    }

    reloadData() {
        this.loadWorkplaces().then(() => this.loadOfficeLocationMap());
    }

    loadWorkplaces() {
        this._error = null;
        this._loading = true;
        return dictAdminService.loadOfficeWorkplaces().then(data => {
            this._workplaces = data;
        }).catch(e => {
            this._error = errorUtils.shortMessage(e);
        }).finally(() => {
            this._loading = false
        })
    }

    loadOfficeLocationMap() {
        this._error = null;
        if (this._filter.officeLocationId == null) {
            this._officeLocationMap = null;
            return Promise.resolve();
        } else {
            this._loading = true;
            return dictService.getOfficeLocationMap(this._filter.officeLocationId)
                .then(map => {
                    this._officeLocationMap = map;
                }).catch(e => {
                    this._error = errorUtils.shortMessage(e);
                }).finally(() => {
                    this._loading = false
                })
        }
    }

    openSvgMapDialog() {
        this._uploadSvgMapDialog = true;
    }

    closeSvgMapDialog() {
        this._uploadSvgMapDialog = false;
    }

    get uploadSvgMapDialog() {
        return this._uploadSvgMapDialog;
    }

    getSvgMapUploadPath() {
        return dictAdminService.getUploadOfficeLocationMapPath(this._filter.officeLocationId!);
    }


}
