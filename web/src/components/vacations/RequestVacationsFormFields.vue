<!-- Dialog to create or update my vacation requests for next year -->
<template>
  <v-form ref="vacationEditForm">
    <!-- start date -->
    <my-date-form-component
        ref="startDateRef"
        v-model="data.formData.startDate"
        :label="$t('Начало')+`*`"
        :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГ'))]"
    ></my-date-form-component>

    <!-- end date -->
    <my-date-form-component
        ref="endDateRef"
        v-model="data.formData.endDate"
        :label="$t('Окончание')+`*`"
        :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГ'))]"
    ></my-date-form-component>

    <v-slider
        :label="$t('Количество дней')"
        min="0"
        max="31"
        step="1"
        thumbLabel="always"
        v-model="data.formData.daysNumber">
    </v-slider>

    <v-textarea
        v-model="data.formData.notes"
        row-height="5"
        :counter="255"
        :rules="[v=>(!v ||  v.length <= 255 || $t('Не более N символов', {n:255}))]"
        :label="$t('Примечание')"
    ></v-textarea>
  </v-form>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import {Prop, Watch} from "vue-property-decorator";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import moment from "moment";
import {DateTimeUtils} from "@/components/datetimeutils";
import {RequestOrUpdateVacationActionDataContainer} from "@/components/vacations/request-vacation.data.container";


@Component(
    {components: {MyDateFormComponent}}
)

export default class VacationEditForm extends Vue {
  @Prop({required: true})
  private data!: RequestOrUpdateVacationActionDataContainer;

  @Prop({required: true})
  public daysNotIncludedInVacations!: Array<string>;

  public defaultNumberOrDays = 14;


  @Watch("data.formData.startDate")
  private watchStartDate() {
    const startDate = moment(this.data.formData?.startDate, moment.HTML5_FMT.DATE, true);
    if (startDate.isValid()) {
      if (this.data.formData && !this.data.formData.endDate) {
        this.data.formData.endDate = startDate.add(this.defaultNumberOrDays - 1, "days").format(moment.HTML5_FMT.DATE);
      }
      this.updateDaysNumber();
    }
  }

  @Watch("data.formData.endDate")
  private watchEndDate() {
    const endDate = moment(this.data.formData?.endDate, moment.HTML5_FMT.DATE, true);
    if (endDate.isValid()) {
      this.updateDaysNumber();
    }
  }

  private updateDaysNumber() {
    if (this.data.formData?.startDate && this.data.formData?.endDate) {
      const start = moment(this.data.formData.startDate, moment.HTML5_FMT.DATE, true);
      const end = moment(this.data.formData.endDate, moment.HTML5_FMT.DATE, true);
      if (start.isValid() && end.isValid()) {
        this.data.formData.daysNumber = DateTimeUtils.vacationDays(start, end, this.daysNotIncludedInVacations);
      }
    }
  }

  private validateDate(formattedDate: string, allowEmpty = true): boolean {
    return DateTimeUtils.validateFormattedDate(formattedDate, allowEmpty);
  }

}
</script>
