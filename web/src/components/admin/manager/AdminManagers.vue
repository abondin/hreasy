<!-- Managers of departments, business accounts and projects-->
<template>
  <hreasy-table ref="managersTable" :data="data"
                :title="title"
                :update-title="()=>data.selectedItems.length == 0 ? null : data.selectedItems[0].employee.name"
                :sort-by="['responsibilityObject.type', 'employee.displayName']"
  >

    <!-- Hide refresh button in compact mode-->
    <template v-if="mode=='compact'" v-slot:refreshButton>
      <span/>
    </template>

    <!--<editor-fold desc="Filters">-->
    <template v-slot:filters v-if="mode=='full'">
      <v-col>
        <v-text-field v-model="data.filter.search"
                      append-icon="mdi-magnify"
                      :label="$t('Поиск')"
                      single-line
                      hide-details
        ></v-text-field>
      </v-col>
      <v-col>
        <v-autocomplete
            clearable
            class="mr-5"
            v-model="data.filter.responsibilityObjectTypes"
            :items="allResponsibilityObjectTypes"
            :label="$t('Тип объекта')"
            multiple
        ></v-autocomplete>
      </v-col>
    </template>
    <!-- </editor-fold> -->

    <!--<editor-fold desc="Columns">-->
    <template v-slot:[`item.responsibilityType`]="{ item }">
      {{ $t(`MANAGER_RESPONSIBILITY_TYPE.${item.responsibilityType}`) }}
    </template>
    <template v-slot:[`item.responsibilityObject.type`]="{ item }">
      {{ $t(`MANAGER_RESPONSIBILITY_OBJECT.${item.responsibilityObject.type}`) }}
    </template>
    <!-- </editor-fold> -->


    <!--<editor-fold desc="Update form">-->
    <template v-slot:updateFormFields>
      <v-autocomplete
          v-model="data.updateBody.responsibilityType"
          :items="allResponsibilityTypes"
          :label="$t('Основное направление')"
          :rules="[v => !!v || $t('Обязательное поле')]"
      ></v-autocomplete>

      <v-textarea
          v-model="data.updateBody.comment"
          :counter="256"
          :rules="[v=>(!v || v.length <= 256 || $t('Не более N символов', {n:256}))]"
          :label="$t('Примечание')"
          required>
        >
      </v-textarea>

    </template>
    <!-- </editor-fold> -->

    <!--<editor-fold desc="Create form">-->
    <template v-slot:createFormFields>

      <v-autocomplete
          v-model="data.createBody.employee"
          :items="allEmployees"
          item-value="id" item-text="displayName"
          :label="$t('Сотрудник')"
          :rules="[v => !!v || $t('Обязательное поле')]"
      ></v-autocomplete>

      <span v-if="selectedObject==null">
      <v-autocomplete
          v-model="data.createBody.responsibilityObjectType"
          :items="allResponsibilityObjectTypes"
          :label="$t('Тип объекта')"
          :rules="[v => !!v || $t('Обязательное поле')]"
          @change="data.createBody.responsibilityObjectId=null"
      ></v-autocomplete>

        <!--<editor-fold desc="Responsibility Object">-->
      <v-autocomplete
          v-if="data.createBody.responsibilityObjectType==='project'"
          v-model="data.createBody.responsibilityObjectId"
          item-value="id" item-text="name"
          :items="allProjects.filter(p=>p.active)"
          :label="$t('Проект')"
          :rules="[v => !!v || $t('Обязательное поле')]"
      ></v-autocomplete>
      <v-autocomplete
          v-if="data.createBody.responsibilityObjectType==='business_account'"
          v-model="data.createBody.responsibilityObjectId"
          item-value="id" item-text="name"
          :items="allBas"
          :label="$t('Бизнес аккаунт')"
          :rules="[v => !!v || $t('Обязательное поле')]"
      ></v-autocomplete>
      <v-autocomplete
          v-if="data.createBody.responsibilityObjectType==='department'"
          v-model="data.createBody.responsibilityObjectId"
          item-value="id" item-text="name"
          :items="allDepartments"
          :label="$t('Отдел')"
          :rules="[v => !!v || $t('Обязательное поле')]"
      ></v-autocomplete>
        <!-- </editor-fold> -->
      </span>

      <v-autocomplete
          v-model="data.createBody.responsibilityType"
          :items="allResponsibilityTypes"
          :label="$t('Основное направление')"
          :rules="[v => !!v || $t('Обязательное поле')]"
      ></v-autocomplete>

      <v-textarea
          v-model="data.createBody.comment"
          :counter="256"
          :rules="[v=>(!v || v.length <= 256 || $t('Не более N символов', {n:256}))]"
          :label="$t('Примечание')"
          required>
        >
      </v-textarea>

    </template>
    <!-- </editor-fold> -->
  </hreasy-table>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import logger from "@/logger";
import permissionService from "@/store/modules/permission.service";
import employeeService, {Employee} from "@/components/empl/employee.service";
import adminManagerService, {
  CreateManagerBody,
  Manager,
  ManagerResponsibilityObjectId,
  ManagerResponsibilityObjectType,
  managerResponsibilityObjectTypes,
  managerResponsibilityTypes,
  UpdateManagerBody
} from "@/components/admin/manager/admin.manager.service";
import {SimpleDict} from "@/store/modules/dict";
import {Getter} from "vuex-class";
import HreasyTable from "@/components/shared/table/HreasyTable.vue";
import {CreateOrUpdateAction} from "@/components/shared/table/EditTableComponentDataContainer";
import TableComponentDataContainer, {Filter, UpdateAction} from "@/components/shared/table/TableComponentDataContainer";
import HreasyTableDeleteConfimration from "@/components/shared/table/HreasyTableDeleteConfimration.vue";
import {Prop} from "vue-property-decorator";
import {DataTableHeader} from "vuetify";


class ManagerFilter extends Filter<Manager> {
  public search = '';
  public responsibilityObjectTypes: ManagerResponsibilityObjectType[] =
      managerResponsibilityObjectTypes;

  applyFilter(items: Manager[]): Manager[] {
    return items.filter((item) => {
      let filtered = true;
      const search = this.search.toLowerCase().trim();
      if (search.length > 0) {
        let searchFilter = false;
        searchFilter = searchFilter || Boolean(item.employee && item.employee.name && item.employee.name.toLowerCase().indexOf(search) >= 0)
        searchFilter = searchFilter || Boolean(item.responsibilityObject && item.responsibilityObject.name && item.responsibilityObject.name.toLowerCase().indexOf(search) >= 0)
        filtered = filtered && searchFilter;
      }
      if (this.responsibilityObjectTypes) {
        filtered = filtered && Boolean(
            item.responsibilityObject && this.responsibilityObjectTypes.indexOf(item.responsibilityObject.type) >= 0);
      }
      return filtered;
    });
  }
}

const namespace_dict = 'dict';

@Component({components: {HreasyTableDeleteConfimration, HreasyTable}})
export default class AdminManagers extends Vue {

  @Prop({required: false, default: null})
  private selectedObject!: ManagerResponsibilityObjectId | null;

  @Prop({required: false, default: 'full'})
  private mode!: 'full' | 'compact';

  @Prop({required: false, default: null})
  private title!: string | null;

  private headers(): DataTableHeader[] {
    const headers = [];
    headers.push({text: this.$tc('Менеджер'), value: 'employee.name'});
    if (this.mode == "full") {
      headers.push({text: this.$tc('Тип объекта'), value: 'responsibilityObject.type'});
      headers.push({text: this.$tc('Объект'), value: 'responsibilityObject.name'});
    }
    headers.push({text: this.$tc('Основное направление'), value: 'responsibilityType'});
    headers.push({text: this.$tc('Примечание'), value: 'comment'});
    return headers;
  }

  private updateAction(): UpdateAction<Manager, UpdateManagerBody> | null {
    return {
      updateItemRequest: (id, body) => (adminManagerService.update(id, body)),
      itemToUpdateBody: item =>
          ({
            responsibilityType: item.responsibilityType,
            comment: item.comment
          })
    };
  }

  private dataLoader(){
    return this.selectedObject ? adminManagerService.findByObject(this.selectedObject) : adminManagerService.findAll();
  }

  private data = new TableComponentDataContainer<Manager, UpdateManagerBody, CreateManagerBody, ManagerFilter>(
      this.dataLoader,
      this.headers,
      this.updateAction(),
      {
        createItemRequest: (body) => (adminManagerService.create(body)),
        defaultBody: this.newManagerBody
      } as CreateOrUpdateAction<Manager, CreateManagerBody>,
      {
        deleteItemRequest: itemsToDelete => adminManagerService.delete(itemsToDelete)
      },
      new ManagerFilter(),
      this.editable
  );
  private allEmployees: Employee[] = [];

  @Getter("projects", {namespace: namespace_dict})
  private allProjects!: Array<SimpleDict>;

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  @Getter("departments", {namespace: namespace_dict})
  private allDepartments!: Array<SimpleDict>;

  private allResponsibilityObjectTypes: Array<any> = [];
  private allResponsibilityTypes: Array<any> = [];

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Managers component created');
    this.allResponsibilityObjectTypes = managerResponsibilityObjectTypes
        .map(i => {
          return {value: i, text: this.$t(`MANAGER_RESPONSIBILITY_OBJECT.${i}`)};
        });
    this.allResponsibilityTypes = managerResponsibilityTypes
        .map(i => {
          return {value: i, text: this.$t(`MANAGER_RESPONSIBILITY_TYPE.${i}`)};
        });
    return this.$nextTick()
        .then(() => this.$store.dispatch('dict/reloadProjects'))
        .then(() => this.$store.dispatch('dict/reloadBusinessAccounts'))
        .then(() => this.$store.dispatch('dict/reloadDepartments'))
        .then(() =>
            employeeService.findAll().then(employees => {
                  this.allEmployees = employees;
                }
            )
        );
  }

  public refresh() {
    if (this.$refs.managersTable) {
      (this.$refs.managersTable as HreasyTable<any, any, any, any>).reloadData();
    }
  }

  private newManagerBody(): CreateManagerBody {
    const body = {
      employee: null,
      responsibilityObjectType: this.selectedObject ? this.selectedObject.type : 'project',
      responsibilityObjectId: this.selectedObject ? this.selectedObject.id : null,
      responsibilityType: 'organization'
    } as CreateManagerBody;
    return body;
  }

  private editable(): boolean {
    if (this.selectedObject && this.selectedObject.type == 'project') {
      return permissionService.canAdminProjects();
    } else {
      return permissionService.canAdminManagers();
    }
  }

}
</script>

<style scoped lang="css">
.table-cursor >>> tbody tr :hover {
  cursor: pointer;
}
</style>
