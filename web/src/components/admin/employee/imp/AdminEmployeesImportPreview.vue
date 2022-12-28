<!-- Employees admin table -->
<template>
  <v-container>
    <v-card>
      <v-card-title>
        <v-text-field
            v-model="filter.search"
            append-icon="mdi-magnify"
            :label="$t('Поиск')"
            single-line
            hide-details
        ></v-text-field>
      </v-card-title>
      <v-card-text>
        <v-data-table
            dense
            :headers="headers"
            :items-per-page="defaultItemsPerTablePage"
            :items="data"
            :search="filter.search"
            sort-by="displayName"
            class="text-truncate">
          <template v-for="header in headers.filter(h=>!h.noformat)"
                    v-slot:[`item.${header.value}`]="{value}">
            <span :key="header.value">
              <span v-if="value && value.error">{{ value.error }}</span>
            </span>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import {DataTableHeader} from "vuetify";
import Component from "vue-class-component";
import {DateTimeUtils} from "@/components/datetimeutils";
import {UiConstants} from "@/components/uiconstants";
import {Prop} from "vue-property-decorator";
import {ImportEmployeeExcelRows} from "@/components/admin/employee/imp/admin.employee.import.service";

const namespace_dict = 'dict';

class Filter {
  public search = '';
}

interface ImportPreviewDataHeader<T = any> extends DataTableHeader<T> {
  noformat?: boolean
}

@Component({components: {}})
export default class AdminEmployeesImportPreview extends Vue {
  headers: ImportPreviewDataHeader[] = [];

  @Prop({required: true})
  data!: ImportEmployeeExcelRows[];

  private defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;

  private filter = new Filter();


  created() {
    this.reloadHeaders();
  }


  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('ФИО'), value: 'displayName', width: 280});
    this.headers.push({text: this.$tc('Email'), value: 'email', width: 280, noformat: true});
    this.headers.push({text: this.$tc('Телефон'), value: 'phone', width: 150});
    this.headers.push({
      text: this.$tc('Дата трудоустройства'),
      value: 'dateOfEmployment',
      width: 150,
      sort: DateTimeUtils.dateComparatorNullLast
    });
    this.headers.push({
      text: this.$tc('День рождения'),
      value: 'birthday',
      width: 150,
      sort: DateTimeUtils.dateComparatorNullLast
    });
    this.headers.push({text: this.$tc('Позиция'), value: 'positionId', width: 280});
    this.headers.push({text: this.$tc('Документ УЛ'), value: 'documentFull', width: 280});
    this.headers.push({text: this.$tc('Адрес по регистрации'), value: 'registrationAddress', width: 500});
    this.headers.push({text: this.$tc('Пол'), value: 'sex', width: 100});
    this.headers.push({text: this.$tc('Подразделение'), value: 'departmentId', width: 300});
    this.headers.push({
      text: this.$tc('Дата увольнения'),
      value: 'dateOfDismissal',
      width: 150,
      sort: DateTimeUtils.dateComparatorNullLast
    });
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
