<!-- Dialog to create or update my vacation requests for next year -->
<template>
  <v-form ref="vacationEditForm">
    <!-- start date -->
    <my-date-form-component
        ref="startDateRef"
        v-model="data.formData.startDate"
        :label="$t('Начало')+`*`"
        :rules="[v=>(v && validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГ'))]"
    ></my-date-form-component>

    <!-- end date -->
    <my-date-form-component
        ref="endDateRef"
        :picker-date="data.formData.startDate"
        v-model="data.formData.endDate"
        :label="$t('Окончание')+`*`"
        :rules="[v=>(v && validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГ'))]"
    ></my-date-form-component>

    {{$t('Количество дней отпуска')}}: <span>{{data.formData.daysNumber}}</span>

    <v-checkbox
        v-model="data.daysNumberSetManually"
        :label="$t('Скорректировать количество дней вручную')"></v-checkbox>

    <v-slider
        v-if="data.daysNumberSetManually"
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


  @Watch("data.formData.startDate")
  private watchStartDate() {
    this.data.startDateUpdated();
  }

  @Watch("data.formData.endDate")
  private watchEndDate() {
    this.data.endDateUpdated();
  }

  private validateDate(formattedDate: string, allowEmpty = true): boolean {
    return DateTimeUtils.validateFormattedDate(formattedDate, allowEmpty);
  }

}
</script>
