<template>
  <div v-if="employee" class="d-flex flex-column flex-lg-row pa-5 info-card">
    <employee-avatar-uploader v-bind:employee="employee" :read-only="true" class="mr-5"></employee-avatar-uploader>
    <dl class="info-dl text--primary text-wrap">
      <dt>{{ $t('ФИО') }}:</dt>
      <dd>{{ employee.displayName }}</dd>

      <dt>{{ $t('Отдел') }}:</dt>
      <dd>{{ employee.department ? employee.department.name : $t("Не задан") }}</dd>

      <dt>{{ $t('Текущий проект') }}:</dt>
      <dd>{{ employee.currentProject ? employee.currentProject.name : $t("Не задан") }}</dd>

      <dt>{{ $t('Роль на текущем проекте') }}:</dt>
      <dd>{{
          (employee.currentProject && employee.currentProject.role) ? employee.currentProject.role : $t("Не задана")
        }}
      </dd>

      <dt>{{ $t('Бизнес Аккаунт') }}:</dt>
      <dd>{{ employee.ba ? employee.ba.name : $t("Не задан") }}</dd>

      <dt>{{ $t('Почтовый адрес') }}:</dt>
      <dd>{{ employee.email ? employee.email : $t("Не задан") }}</dd>

      <dt>{{ $t('Позиция') }}:</dt>
      <dd>{{ employee.position ? employee.position.name : $t("Не задана") }}</dd>

      <dt>{{ $t('Кабинет') }}:</dt>
      <dd>{{ employee.officeLocation ? employee.officeLocation.name : $t("Не задан") }}</dd>

      <slot name="additionalFields"></slot>
    </dl>
    <slot></slot>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import Component from "vue-class-component";
import {errorUtils} from "@/components/errors";
import employeeService, {Employee} from "@/components/empl/employee.service";
import EmployeeAvatarUploader from "@/components/empl/EmployeeAvatarUploader.vue";

@Component({
  components: {EmployeeAvatarUploader}
})
export default class EmployeeBaseInfoCard extends Vue {
  @Prop({required: true})
  private employeeId!: number;

  private employee: Employee | null = null;

  private loading = false;

  private error = '';

  /**
   * Lifecycle hook
   */
  created() {
    this.fetch();
  }

  private fetch() {
    this.loading = true;
    this.error = '';
    employeeService.find(this.employeeId)
        .then(data => {
          this.employee = data
        })
        .catch(error => this.error = errorUtils.shortMessage(error))
        .finally(() => this.loading = false);
  }
}
</script>

<style scoped lang="scss">
.info-dl {
  align-content: baseline;
  display: grid;
  grid-template-columns: max-content auto;

  > dt {
    font-weight: bolder;
    min-width: 150px;
    max-width: 300px;
    grid-column-start: 1;
  }

  > dd {
    grid-column-start: 2;
    margin-left: 10px;
    max-width: 400px;
  }
}
</style>
