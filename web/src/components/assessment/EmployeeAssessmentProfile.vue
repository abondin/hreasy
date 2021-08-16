<!-- Employees own profile page-->
<template>
  <v-container v-if="employee">
    <v-card-title>
      <router-link to="/assessments">
        <v-icon class="mr-5">mdi-arrow-left</v-icon>
      </router-link>
      {{ $t('Ассессменты сотрудника') }}
    </v-card-title>
    <v-card class="d-flex flex-column flex-lg-row pa-5">
      <employee-avatar v-bind:employee="employee"></employee-avatar>
      <v-list-item>
        <v-list-item-content>
          <v-list-item-title class="title">{{ employee.displayName }}</v-list-item-title>
          <v-list-item-subtitle>{{ $t('Отдел') }} :
            {{ employee.department ? employee.department.name : $t("Не задан") }}
          </v-list-item-subtitle>
          <v-list-item-subtitle>{{ $t('Текущий проект') }} :
            {{ employee.currentProject ? employee.currentProject.name : $t("Не задан") }}
          </v-list-item-subtitle>
          <v-list-item-subtitle>
            {{ $t('Почтовый адрес') }} : {{ employee.email ? employee.email : $t("Не задан") }}
          </v-list-item-subtitle>
          <v-list-item-subtitle>
            {{ $t('Позиция') }} : {{ employee.position ? employee.position.name : $t("Не задана") }}
          </v-list-item-subtitle>
          <v-list-item-subtitle>
            {{ $t('Кабинет') }} : {{ employee.officeLocation ? employee.officeLocation.name : $t("Не задан") }}
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-card>

    <v-card>
      <v-card-text>
        <v-data-table
            dense
            :loading="loading"
            :loading-text="$t('Загрузка_данных')"
            :headers="headers"
            :items="filteredItems()"
            multi-sort
            hide-default-footer
            :sort-by="['plannedDate']"
            sort-desc
            disable-pagination>
          <template
              v-slot:item.plannedDate="{ item }">
            {{ formatDate(item.plannedDate) }}
          </template>
          <template
              v-slot:item.createdAt="{ item }">
            {{ item.createdBy ? (formatDateTime(item.createdAt) + ' (' + item.createdBy.name + ')') : '' }}
          </template>
          <template
              v-slot:item.completedAt="{ item }">
            {{ item.completedBy ? (formatDateTime(item.completedAt) + ' (' + item.completedBy.name + ')') : '' }}
          </template>
          <template
              v-slot:item.canceledAt="{ item }">
            {{ item.canceledBy ? (formatDateTime(item.canceledAt) + ' (' + item.canceledBy.name + ')') : '' }}
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import employeeService, {Employee} from "@/components/empl/employee.service";
import EmployeeAvatarUploader from "@/components/empl/EmployeeAvatarUploader.vue";
import {Prop} from "vue-property-decorator";
import {DataTableHeader} from "vuetify";
import assessmentService, {AssessmentBase} from "@/components/assessment/assessment.service";
import {DateTimeUtils} from "@/components/datetimeutils";

const namespace: string = 'auth';

@Component({
  components: {
    "employee-avatar": EmployeeAvatarUploader
  }
})
export default class EmployeeAssessmentProfile extends Vue {
  loading: boolean = false;
  headers: DataTableHeader[] = [];
  assessments: AssessmentBase[] = [];

  private employee: Employee | null = null;

  @Prop({required: true})
  employeeId!: number;

  /**
   * Lifecycle hook
   */
  created() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('Дата ассессмента'), value: 'plannedDate'});
    this.headers.push({text: this.$tc('Создано'), value: 'createdAt'});
    this.headers.push({text: this.$tc('Завершено'), value: 'completedAt'});
    this.headers.push({text: this.$tc('Отменено'), value: 'canceledAt'});
    this.fetchData(this.employeeId);
  }

  private fetchData(employeeId: number) {
    this.loading = true;
    return employeeService.find(employeeId)
        .then(data => {
              this.employee = data;
              return this.employee;
            }
        ).then(() => assessmentService.employeeAssessments(employeeId))
        .then((assessments) => {
          this.assessments = assessments
        })
        .finally(() => {
          this.loading = false
        });
  }

  private filteredItems() {
    return this.assessments;
  }

  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

  private formatDateTime(date: string): string | undefined {
    return DateTimeUtils.formatDateTimeFromIso(date);
  }

}
</script>

<style scoped>

</style>
