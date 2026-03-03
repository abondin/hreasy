<!-- All assessments for selected employee -->
<template>
  <v-container>
    <router-link to="/assessments">{{ $t('Ассессменты') }}</router-link>
    <v-card>
      <v-card-text>
        <employee-base-info-card :employee-id="employeeId"/>
      </v-card-text>
    </v-card>

    <v-card class="mt-5">
      <v-card-title>
        <v-select
            class="mr-5"
            v-model="filter.includeCanceled"
            :items="[{value:false, text:$t('Нет')}, {value:true, text:$t('Да')}]"
            :label="$t('Учитывать отмененные')"></v-select>
        <v-spacer></v-spacer>
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <v-btn @click="openNewAssessmentDialog"
                   v-bind="tattrs" v-on="ton" class="col-auto" color="primary" :disabled="loading" icon>
              <v-icon>mdi-plus</v-icon>
            </v-btn>
          </template>
          <span>{{ $t('Добавить ассессмент') }}</span>
        </v-tooltip>
      </v-card-title>
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
            <router-link :to="`/assessments/${employeeId}/${item.id}`">{{ formatDate(item.plannedDate) }}</router-link>
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

    <v-dialog v-model="newAssessmentDialog">
      <v-form ref="assessmentForm">
        <v-card>
          <v-card-title>
            {{ $t('Планирование нового ассессмента') }}
            <v-spacer></v-spacer>
            <v-btn icon @click="newAssessmentDialog=false">
              <v-icon>mdi-close</v-icon>
            </v-btn>
          </v-card-title>
          <v-card-text>
            <!-- planned date -->
            <my-date-form-component
                v-model="newAssessmentFormModel.plannedDate"
                :label="$t('Дата проведения')+`*`"
                :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГ'))]"></my-date-form-component>

            <v-alert v-if="newAssessmentFormError" class="error">{{ newAssessmentFormError }}</v-alert>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn @click="newAssessmentDialog=false">{{ $t('Закрыть') }}</v-btn>
            <v-btn @click="createNewAssessment()" color="primary">{{ $t('Запланировать') }}</v-btn>
          </v-card-actions>
        </v-card>
      </v-form>
    </v-dialog>

  </v-container>

</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {Prop} from "vue-property-decorator";
import {DataTableHeader} from "vuetify";
import assessmentService, {AssessmentBase, NewAssessmentBody} from "@/components/assessment/assessment.service";
import {DateTimeUtils} from "@/components/datetimeutils";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import logger from "@/logger";
import {errorUtils} from "@/components/errors";
import EmployeeBaseInfoCard from "@/components/shared/EmployeeBaseInfoCard.vue";

class Filter {
  public includeCanceled = false;
}

class NewAssessmentForm {
  plannedDate: string | undefined = '';
}

@Component({
  components: {
    EmployeeBaseInfoCard,
    MyDateFormComponent
  }
})
export default class EmployeeAssessmentProfile extends Vue {
  loading = false;
  headers: DataTableHeader[] = [];
  assessments: AssessmentBase[] = [];

  newAssessmentFormModel: NewAssessmentForm = new NewAssessmentForm();
  newAssessmentFormError = '';

  private newAssessmentDialog = false;

  @Prop({required: true})
  employeeId!: number;

  private filter: Filter = new Filter();

  /**
   * Lifecycle hook
   */
  created() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('Дата ассессмента'), value: 'plannedDate'});
    this.headers.push({text: this.$tc('Создано'), value: 'createdAt'});
    this.headers.push({text: this.$tc('Завершено'), value: 'completedAt'});
    this.headers.push({text: this.$tc('Отменено'), value: 'canceledAt'});
    this.resetNewAssessmentForm();
    this.fetchData(this.employeeId);
  }

  private fetchData(employeeId: number) {
    this.loading = true;
    return assessmentService.employeeAssessments(employeeId)
        .then((assessments) => {
          this.assessments = assessments
        })
        .finally(() => {
          this.loading = false
        });
  }

  private filteredItems() {
    return this.assessments.filter((item) => {
      let filtered = true;
      if (!this.filter.includeCanceled) {
        filtered = filtered && !item.canceledAt;
      }
      return filtered;
    });
  }

  private openNewAssessmentDialog() {
    this.newAssessmentDialog = true;
    this.resetNewAssessmentForm();
  }

  private resetNewAssessmentForm() {
    this.newAssessmentFormModel.plannedDate = DateTimeUtils.nowDateIso();
  }

  private createNewAssessment() {
    this.newAssessmentFormError = '';
    const form: any = this.$refs.assessmentForm;
    if (form.validate()) {
      const body = {
        plannedDate: this.newAssessmentFormModel.plannedDate,
        // TODO Add managers
        managers: []
      } as NewAssessmentBody;
      logger.log(`Schedule new assessment for ${this.employeeId} : ${body}`);
      return assessmentService.scheduleNewAssessment(this.employeeId, body)
          .then((assessmentId: number) => {
            this.newAssessmentDialog = false;
            this.$router.push(`/assessments/${this.employeeId}/${assessmentId}`);
          }).catch((err: any) => {
            this.newAssessmentFormError = errorUtils.shortMessage(err);
          });
    }
  }

  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

  private formatDateTime(date: string): string | undefined {
    return DateTimeUtils.formatDateTimeFromIso(date);
  }

  private validateDate(formattedDate: string, allowEmpty = true): boolean {
    return DateTimeUtils.validateFormattedDate(formattedDate, allowEmpty);
  }

}
</script>

<style lang="css">
</style>
