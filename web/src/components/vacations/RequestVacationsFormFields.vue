<!-- Dialog to create or update my vacation requests for next year -->
<template>
  <span>
  <v-date-picker
      full-width
      first-day-of-week="1"
      landscape
      no-title
      v-model="data.formData.dates"
      range
  ></v-date-picker>
  <p :set="formatted=data.formattedDates()">
    <span v-if="formatted">{{ formatted }}</span>
    <span v-else class="error--text">{{ $t('Выберите даты') }}</span>
  </p>
  <p>{{ $t('Количество дней отпуска') }}: <span :class="{'warning--text':data.formData.daysNumber>28}">{{ data.formData.daysNumber }}</span></p>

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
    </span>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import {Prop, Watch} from "vue-property-decorator";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import {DateTimeUtils} from "@/components/datetimeutils";
import {RequestOrUpdateVacationActionDataContainer} from "@/components/vacations/request-vacation.data.container";


@Component(
    {components: {MyDateFormComponent}}
)
export default class VacationEditForm extends Vue {
  @Prop({required: true})
  private data!: RequestOrUpdateVacationActionDataContainer;


  @Watch("data.formData.dates")
  private watchStartDate() {
    this.data.datesUpdated();
  }

  private validateDate(formattedDate: string, allowEmpty = true): boolean {
    return DateTimeUtils.validateFormattedDate(formattedDate, allowEmpty);
  }

}
</script>
