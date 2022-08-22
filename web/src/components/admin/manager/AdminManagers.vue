<!-- Managers of departments, business accounts and projects-->
<template>
  <hreasy-table :data="data">
    <template v-slot:filters>
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
      <v-col>
        <v-autocomplete
            clearable
            class="mr-5"
            v-model="data.filter.bas"
            :items="allBas"
            item-text="name"
            item-value="id"
            :label="$t('Бизнес аккаунт')"
            multiple
        ></v-autocomplete>
      </v-col>
      <v-col>
        <v-autocomplete
            clearable
            class="mr-5"
            v-model="data.filter.departments"
            :items="allDepartments"
            item-text="name"
            item-value="id"
            :label="$t('Отдел')"
            multiple
        ></v-autocomplete>
      </v-col>
    </template>
    <template v-slot:item.responsibilityType="{ item }">
      {{ $t(`MANAGER_RESPONSIBILITY_TYPE.${item.responsibilityType}`) }}
    </template>
    <template v-slot:item.responsibilityObject.type="{ item }">
      {{ $t(`MANAGER_RESPONSIBILITY_OBJECT.${item.responsibilityObject.type}`) }}
    </template>
  </hreasy-table>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import logger from "@/logger";
import {DateTimeUtils} from "@/components/datetimeutils";
import permissionService from "@/store/modules/permission.service";
import employeeService, {Employee} from "@/components/empl/employee.service";
import adminManagerService, {
  CreateOrManagerBody,
  Manager,
  ManagerResponsibilityObjectType
} from "@/components/admin/manager/admin.manager.service";
import {SimpleDict} from "@/store/modules/dict";
import {Getter} from "vuex-class";
import HreasyTable from "@/components/shared/table/HreasyTable.vue";
import EditTableComponentDataContainer, {CreateOrUpdateAction} from "@/components/shared/table/EditTableComponentDataContainer";
import {Filter} from "@/components/shared/table/TableComponentDataContainer";
import HreasyTableDeleteConfimration from "@/components/shared/table/HreasyTableDeleteConfimration.vue";


class ManagerFilter extends Filter<Manager> {
  public search = '';
  public bas: number[] = [];
  public departments: number[] = [];
  public responsibilityObjectTypes: ManagerResponsibilityObjectType[]=[];

  applyFilter(items: Manager[]): Manager[] {
    return items.filter((item) => {
      let filtered: boolean = true;
      const search = this.search.toLowerCase().trim();
      if (search.length > 0) {
        let searchFilter: boolean = false;
        searchFilter = searchFilter || Boolean(item.employee && item.employee.name && item.employee.name.toLowerCase().indexOf(search) >= 0)
        searchFilter = searchFilter || Boolean(item.responsibilityObject && item.responsibilityObject.name && item.responsibilityObject.name.toLowerCase().indexOf(search) >= 0)
        filtered = filtered && searchFilter;
      }
      if (this.bas && this.bas.length > 0) {
        filtered = filtered && Boolean(
            item.responsibilityObject && item.responsibilityObject.baId && this.bas.indexOf(item.responsibilityObject.baId) >= 0);
      }
      if (this.departments && this.departments.length > 0) {
        filtered = filtered && Boolean(
            item.responsibilityObject && item.responsibilityObject.departmentId && this.departments.indexOf(item.responsibilityObject.departmentId) >= 0);
      }
      if (this.responsibilityObjectTypes) {
        filtered = filtered && Boolean(
            item.responsibilityObject && this.responsibilityObjectTypes.indexOf(item.responsibilityObject.type) >= 0);
      }
      return filtered;
    });
  }
}

const namespace_dict: string = 'dict';

@Component({components: {HreasyTableDeleteConfimration, HreasyTable}})
export default class AdminManagers extends Vue {
  newManagerBody(): CreateOrManagerBody {
    throw new Error("Method not implemented.");
  }

  private data = new EditTableComponentDataContainer<Manager, CreateOrManagerBody, ManagerFilter>(
      () => adminManagerService.findAll(),
      () =>
          [
            {text: this.$tc('Менеджер'), value: 'employee.name'},
            {text: this.$tc('Тип объекта'), value: 'responsibilityObject.type'},
            {text: this.$tc('Объект'), value: 'responsibilityObject.name'},
            {text: this.$tc('Основное направление'), value: 'responsibilityType'},
            {text: this.$tc('Примечание'), value: 'comment'}
          ],
      {
        updateItemRequest: (id, body) => (adminManagerService.update(id, body)),
        itemToUpdateBody: item =>
            ({
              comment: item.comment,
              employee: item.employee.id,
              responsibilityObject: item.responsibilityObject,
              responsibilityType: item.responsibilityType
            } as CreateOrManagerBody),
        createItemRequest: (body) => (adminManagerService.create(body)),
        defaultBody: () => this.newManagerBody()
      } as CreateOrUpdateAction<Manager, CreateOrManagerBody>,
      {
        deleteItemRequest: itemsToDelete => adminManagerService.delete(itemsToDelete)
      },
      new ManagerFilter(),
      permissionService.canAdminManagers()
  );
  private allEmployees: Employee[] = [];

  @Getter("projects", {namespace: namespace_dict})
  private allProjects!: Array<SimpleDict>;

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  @Getter("departments", {namespace: namespace_dict})
  private allDepartments!: Array<SimpleDict>;

  private allResponsibilityObjectTypes: Array<any> = [];
  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Managers component created');
    this.allResponsibilityObjectTypes = ['project','business_account','department']
        .map(i=>{
          return {value:i, text: this.$t(`MANAGER_RESPONSIBILITY_OBJECT.${i}`)};
        });
    this.data.filter.responsibilityObjectTypes = this.allResponsibilityObjectTypes.map(a=>a.value);
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


  private formatDate(date: string | undefined): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }


}
</script>

<style scoped lang="css">
.table-cursor >>> tbody tr :hover {
  cursor: pointer;
}
</style>
