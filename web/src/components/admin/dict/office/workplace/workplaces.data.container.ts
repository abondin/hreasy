import {WorkplacesFilterContainer} from "@/components/admin/dict/office/workplace/WorkplacesFilterComponent.vue";
import dictAdminService, {DictOfficeWorkplace} from "@/components/admin/dict/dict.admin.service";
import dictService from "@/store/modules/dict.service";
import {errorUtils} from "@/components/errors";
import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";

export default class WorkplacesDataContainer {
    private readonly _filter: WorkplacesFilterContainer;
    private _officeLocationMap: string | null = null;
    private _workplaces: DictOfficeWorkplace[] = [];

    private _uploadSvgMapDialog = false;

    private _loading = false;
    private _error: string | null = null;

    private readonly _deleteMapAction: InDialogActionDataContainer<number, void>;

    constructor() {
        this._filter = new WorkplacesFilterContainer();
        this._deleteMapAction = new InDialogActionDataContainer(this.submitDeleteMap);
    }

    private submitDeleteMap(officeLocationId: number | null): Promise<any> {
        if (officeLocationId) {
            return dictAdminService.deleteOfficeLocationMap(officeLocationId);
        } else {
            return Promise.resolve();
        }
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
        return this.filter.officeLocationId ? this._workplaces.filter(w => w.officeLocation?.id == this.filter.officeLocationId) : [];
    }

    get officeLocationMap() {
        return this._officeLocationMap;
    }

    get deleteMapAction(){
        return this._deleteMapAction;
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
