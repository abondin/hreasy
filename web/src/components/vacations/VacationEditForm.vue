<!-- Dialog to create or update vacation entry -->
<template>
  <v-form ref="vacationEditForm">
    <v-card>
      <v-card-title v-if="vacationForm.isNew">{{ $t('Добавление отпуска') }}</v-card-title>
      <v-card-title v-else>{{ $t('Изменение отпуска') }}</v-card-title>
      <v-card-text>


        <!-- start date -->
        <my-date-form-component
            v-model="vacationForm.startDate"
            :label="$t('Начало')+`*`"
            :rules="[v=>(v && Date.parse(v) > 0 || $t('Дата в формате ГГГГ-ММ-ДД'))]"
        ></my-date-form-component>

        <!-- end date -->
        <my-date-form-component
            v-model="vacationForm.endDate"
            :label="$t('Окончание')+`*`"
            :rules="[v=>(v && Date.parse(v) > 0 || $t('Дата в формате ГГГГ-ММ-ДД'))]"
        ></my-date-form-component>

        <!-- planned start date -->
        <my-date-form-component
            v-model="vacationForm.plannedStartDate"
            :label="$t('Планируемое начало')"
            :rules="[v=>(!v || Date.parse(v) > 0 || $t('Дата в формате ГГГГ-ММ-ДД'))]"
        ></my-date-form-component>

        <!-- planned end date -->
        <my-date-form-component
            v-model="vacationForm.plannedEndDate"
            :label="$t('Планируемое окончание')"
            :rules="[v=>(!v || Date.parse(v) > 0 || $t('Дата в формате ГГГГ-ММ-ДД'))]"
        ></my-date-form-component>

        <!-- status -->
        <v-select
            v-model="vacationForm.status"
            :items="allStatuses"
            :label="$t('Статус')"
        ></v-select>

        <!-- Error block -->
        <v-alert v-if="error" type="error">
          {{ error }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="closeDialog()">{{ $t('Закрыть') }}</v-btn>
        <v-btn @click="submit()" color="primary">{{ vacationForm.isNew ? $t('Создать') : $t('Изменить') }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-form>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import {Prop, Watch} from "vue-property-decorator";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import logger from "@/logger";
import {errorUtils} from "@/components/errors";
import vacationService, {CreateOrUpdateVacation, Vacation} from "@/components/vacations/vacation.service";


class VacationForm {
  public isNew = true;
  public id?: number;
  public employeeId: number = -1;
  public employeeDisplayName = '';
  public startDate = '';
  public endDate = '';
  public plannedStartDate = '';
  public plannedEndDate = '';
  public status: 'PLANNED' | 'TAKEN' | 'COMPENSATION' | 'CANCELED' = 'PLANNED'
  public notes = '';
  public documents = '';
  public daysNumber?: number;
}

@Component(
    {components: {MyDateFormComponent}}
)

export default class VacationEditForm extends Vue {
  loading: boolean = false;

  @Prop({required: false})
  private input: Vacation | undefined;


  private vacationForm = new VacationForm();

  private error: String | null = null;

  @Prop({required: true})
  public allStatuses!: Array<any>;


  @Watch("input")
  private watch() {
    this.reset();
  }

  private created() {
    this.reset();
  }

  private reset() {
    this.vacationForm.isNew = true;
    this.vacationForm.id = undefined;
    this.vacationForm.employeeDisplayName = '';
    this.vacationForm.startDate = '';
    this.vacationForm.endDate = '';
    this.vacationForm.plannedStartDate = '';
    this.vacationForm.plannedEndDate = '';
    this.vacationForm.status = 'PLANNED';
    this.vacationForm.notes = '';
    this.vacationForm.documents = '';
    this.vacationForm.daysNumber=undefined;

    if (this.input) {
      this.vacationForm.isNew = false;
      this.vacationForm.id = this.input.id;
      this.vacationForm.employeeDisplayName = this.input.employeeDisplayName;
      this.vacationForm.startDate = this.input.startDate;
      this.vacationForm.endDate = this.input.endDate;
      this.vacationForm.plannedStartDate = this.input.plannedStartDate ? this.input.plannedStartDate: '';
      this.vacationForm.plannedEndDate = this.input.plannedEndDate ? this.input.plannedEndDate: '';
      this.vacationForm.status = this.input.status;
      this.vacationForm.notes = this.input.notes;
      this.vacationForm.documents = this.input.documents;
      this.vacationForm.daysNumber = this.input.daysNumber;
    }
  }

  private closeDialog() {
    this.reset();
    this.$nextTick(function () {
      this.$emit('close');
    })
  }

  private submit() {
    const form: any = this.$refs.vacationEditForm;
    if (form.validate()) {
      const body = {
        startDate: this.vacationForm.startDate,
        endDate: this.vacationForm.endDate,
        plannedStartDate: this.vacationForm.plannedStartDate,
        plannedEndDate: this.vacationForm.plannedStartDate,
        status: this.vacationForm.status,
        notes: this.vacationForm.notes,
        documents: this.vacationForm.documents,
        daysNumber: this.vacationForm.daysNumber
      } as CreateOrUpdateVacation;
      var serverRequest;
      if (this.vacationForm.isNew) {
        serverRequest = vacationService.create(this.vacationForm.employeeId as number, body)
      } else {
        serverRequest = vacationService.update(this.vacationForm.employeeId as number, this.vacationForm.id!, body);
      }
      return serverRequest
          .then((result) => {
            logger.log(`Vacation updated/created: ${result}`);
            this.$emit('close');
          })
          .catch(error => {
            this.error = errorUtils.shortMessage(error);
          });
    }
  }

}
</script>
