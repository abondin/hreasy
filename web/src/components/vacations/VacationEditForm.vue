<!-- Dialog to create or update vacation entry -->
<template>
  <v-form ref="vacationEditForm">
    <v-card>
      <v-card-title>
        <span v-if="vacationForm.isNew">{{ $t('Добавление отпуска') }}</span>
        <span v-else>{{ $t('Изменение отпуска') }}</span>
        <v-spacer></v-spacer>
        <v-btn icon @click="closeDialog()">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-card-title>
      <v-card-text>

        <!-- employee -->
        <v-autocomplete
            :disabled="!vacationForm.isNew"
            v-model="vacationForm.employeeId"
            :items="allEmployees"
            item-value="id"
            item-text="name"
            :label="$t('Сотрудник')+`*`"
            :rules="[v=>(v ? true:false || $t('Обязательное поле'))]"
        ></v-autocomplete>


        <v-select
            v-model="vacationForm.year"
            :items="allYears"
            :label="$t('Год')"
        ></v-select>

        <!-- start date -->
        <my-date-form-component
            ref="startDateRef"
            v-model="vacationForm.startDate"
            :label="$t('Начало')+`*`"
            :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГ'))]"
        ></my-date-form-component>

        <!-- end date -->
        <my-date-form-component
            ref="endDateRef"
            v-model="vacationForm.endDate"
            :label="$t('Окончание')+`*`"
            :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГ'))]"
        ></my-date-form-component>

        <!-- status -->
        <v-select
            v-model="vacationForm.status"
            :items="allStatuses"
            :label="$t('Статус')"
        ></v-select>

        <v-slider
            :label="$t('Количество дней')"
            min="0"
            max="31"
            step="1"
            thumbLabel="always"
            v-model="vacationForm.daysNumber">
        </v-slider>

        <v-text-field
            v-model="vacationForm.documents"
            :label="$t('Документ')"
            :counter="255"
            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
        ></v-text-field>

        <v-textarea
            v-model="vacationForm.notes"
            row-height="5"
            :counter="255"
            :rules="[v=>(!v ||  v.length <= 255 || $t('Не более N символов', {n:255}))]"
            :label="$t('Примечание')"
        ></v-textarea>

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
import vacationService, {
  CreateOrUpdateVacation,
  Vacation,
  VacationStatus
} from "@/components/vacations/vacation.service";
import {SimpleDict} from "@/store/modules/dict";
import moment from "moment";
import {DateTimeUtils} from "@/components/datetimeutils";


class VacationForm {
  public isNew = true;
  public id?: number;
  public year = new Date().getFullYear();
  public employeeId?: number;
  public startDate = '';
  public endDate = '';
  public plannedStartDate = '';
  public plannedEndDate = '';
  public status: VacationStatus = 'PLANNED';
  public notes = '';
  public documents = '';
  public daysNumber?: number;
}

@Component(
    {components: {MyDateFormComponent}}
)

export default class VacationEditForm extends Vue {
  loading = false;

  @Prop({required: false})
  private input: Vacation | undefined;


  private vacationForm = new VacationForm();

  private error: string | null = null;

  @Prop({required: true})
  public allStatuses!: Array<any>;

  @Prop({required: true})
  public allYears!: Array<any>;

  @Prop({required: true})
  public allEmployees!: Array<SimpleDict>;

  @Prop({required: true})
  public daysNotIncludedInVacations!: Array<string>;

  @Prop({required: false})
  public defaultYear?: number;

  public defaultNumberOrDays = 14;

  private created() {
    this.reset();
  }

  /**
   * Resets only on first creation. To reset data after creation call this method from parent component
   */
  public reset() {
    const defaultStartDate = (this.defaultYear && this.defaultYear != new Date().getFullYear()) ?
        moment({year: this.defaultYear, month: 1, day: 1}) : undefined;
    this.error = null;
    this.vacationForm.isNew = true;
    this.vacationForm.id = undefined;
    this.vacationForm.year = this.defaultYear ? this.defaultYear : new Date().getFullYear();
    this.vacationForm.employeeId = undefined;
    this.vacationForm.startDate = defaultStartDate ? DateTimeUtils.formatToIsoDate(defaultStartDate)! : '';
    this.vacationForm.endDate = '';
    this.vacationForm.plannedStartDate = '';
    this.vacationForm.plannedEndDate = '';
    this.vacationForm.status = 'PLANNED';
    this.vacationForm.notes = '';
    this.vacationForm.documents = '';
    this.vacationForm.daysNumber = 0;

    if (this.input) {
      this.vacationForm.isNew = false;
      this.vacationForm.id = this.input.id;
      this.vacationForm.year = this.input.year;
      this.vacationForm.employeeId = this.input.employee;
      this.vacationForm.startDate = this.input.startDate;
      this.vacationForm.endDate = this.input.endDate;
      this.vacationForm.plannedStartDate = this.input.plannedStartDate ? this.input.plannedStartDate : '';
      this.vacationForm.plannedEndDate = this.input.plannedEndDate ? this.input.plannedEndDate : '';
      this.vacationForm.status = this.input.status;
      this.vacationForm.notes = this.input.notes;
      this.vacationForm.documents = this.input.documents;
      this.vacationForm.daysNumber = this.input.daysNumber;
    }
    if (this.$refs.startDateRef && this.$refs.endDateRef) {
      (this.$refs.startDateRef as MyDateFormComponent).reset();
      (this.$refs.endDateRef as MyDateFormComponent).reset();
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
        year: this.vacationForm.year,
        startDate: this.vacationForm.startDate,
        endDate: this.vacationForm.endDate,
        plannedStartDate: this.vacationForm.plannedStartDate,
        plannedEndDate: this.vacationForm.plannedStartDate,
        status: this.vacationForm.status,
        notes: this.vacationForm.notes,
        documents: this.vacationForm.documents,
        daysNumber: this.vacationForm.daysNumber
      } as CreateOrUpdateVacation;
      let serverRequest;
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


  @Watch("vacationForm.startDate")
  private watchStartDate() {
    const startDate = moment(this.vacationForm.startDate, moment.HTML5_FMT.DATE, true);
    if (startDate.isValid()) {
      if (!this.vacationForm.endDate) {
        this.vacationForm.endDate = startDate.add(this.defaultNumberOrDays - 1, "days").format(moment.HTML5_FMT.DATE);
      }
      this.updateDaysNumber();
    }
  }

  @Watch("vacationForm.endDate")
  private watchEndDate() {
    const endDate = moment(this.vacationForm.endDate, moment.HTML5_FMT.DATE, true);
    if (endDate.isValid()) {
      this.updateDaysNumber();
    }
  }

  private updateDaysNumber() {
    if (this.vacationForm.startDate && this.vacationForm.endDate) {
      const start = moment(this.vacationForm.startDate, moment.HTML5_FMT.DATE, true);
      const end = moment(this.vacationForm.endDate, moment.HTML5_FMT.DATE, true);
      if (start.isValid() && end.isValid()) {
        this.vacationForm.daysNumber = DateTimeUtils.vacationDays(start, end, this.daysNotIncludedInVacations);
      }
    }
  }

  private validateDate(formattedDate: string, allowEmpty = true): boolean {
    return DateTimeUtils.validateFormattedDate(formattedDate, allowEmpty);
  }

}
</script>
