<template>
  <div>
    <v-menu ref="menu" v-model="menu" :close-on-content-click="false"
            :return-value.sync="selectedDates"
            transition="scale-transition" offset-y>
      <template v-slot:activator="{ on, attrs }">
        <v-combobox v-model="selectedDates"
                    :label="label"
                    prepend-icon="mdi-calendar" readonly v-bind="attrs" v-on="on" class="mr-5">
          <template v-slot:selection="{ item }">
            <span v-on="on" v-bind="attrs">{{ parseDateRange(item) }}</span>
          </template>
        </v-combobox>
      </template>
      <v-date-picker v-model="selectedDates"
                     :pickerDate.sync="pickerDate"
                     show-current
                     show-week
                     scrollable
                     no-title
                     range
                     width="500px">
        <v-btn-toggle rounded>
          <v-btn x-small @click="today()" v-if="allowedShortCut && allowedShortCut.indexOf('todayPlus5Days')>=0">
            {{ $t('Текущая дата +5 дней') }}
          </v-btn>
          <v-btn x-small @click="currentMonth()" v-if="allowedShortCut && allowedShortCut.indexOf('month')>=0">
            {{ $t('Текущий месяц') }}
          </v-btn>
          <v-btn x-small @click="currentYear()" v-if="allowedShortCut && allowedShortCut.indexOf('year')>=0">
            {{ $t('Год') }}
          </v-btn>
        </v-btn-toggle>
        <v-spacer></v-spacer>
        <v-btn text @click="menu = false">
          {{ $t('Закрыть') }}
        </v-btn>
        <v-btn text color="primary" @click="apply()">
          {{ $t('Применить') }}
        </v-btn>
      </v-date-picker>
    </v-menu>
  </div>
</template>

<script lang="ts">

import Component from "vue-class-component";
import Vue from 'vue'
import {Prop} from "vue-property-decorator";
import moment, {HTML5_FMT} from "moment";
import {DateTimeUtils} from "@/components/datetimeutils";


export type MyDateRangeComponentAllowedTypes='year'|'month'|'todayPlus5Days';

@Component
export default class MyDateRangeComponent extends Vue {

  @Prop()
  private value!: string[];

  @Prop({required: true})
  private label!: string;

  @Prop({required: false, type: Array})
  private rules: any;

  @Prop({type: Array, default:()=>['year', 'month', 'todayPlus5Days']})
  private allowedShortCut!: Array<MyDateRangeComponentAllowedTypes>;

  private menu = false;

  private pickerDate: string | null = null;

  public get selectedDates() {
    return this.value;
  }

  public set selectedDates(dates: string[]) {
    this.pickerDate = dates && dates.length > 0 ? dates[0] : null;
    this.$emit('input', dates);
  }

  private parseDateRange(datesIso: Array<string>) {
    if (!datesIso || datesIso.length == 0) {
      return this.$t('Не выбран');
    }
    const from = datesIso[0];
    let result = `${this.$i18n.t('с')} ${DateTimeUtils.formatFromIso(from)}`;
    if (datesIso.length > 1) {
      const to = datesIso[1];
      result += ` ${this.$i18n.t('по')} ${DateTimeUtils.formatFromIso(to)}`;
    }
    return result;
  }

  public today() {
    this.selectedDates = [moment().format(HTML5_FMT.DATE), moment().add(5, 'day').format(HTML5_FMT.DATE)];
  }

  public currentMonth() {
    this.selectedDates = [moment().startOf('month').format(HTML5_FMT.DATE),
      moment().endOf('month').format(HTML5_FMT.DATE)];
  }

  public currentYear() {
    this.selectedDates = [moment().startOf('year').format(HTML5_FMT.DATE),
      moment().endOf('year').format(HTML5_FMT.DATE)];
  }

  private apply() {
    (this.$refs.menu as any).save(this.selectedDates);
  }


}

</script>
