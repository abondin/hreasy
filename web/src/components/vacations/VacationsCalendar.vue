<template>
  <v-container>
    <v-row>
      <v-col cols="7" lg="6">
        <v-card>
          <v-data-table
              dense
              v-model="selectedEmployees"
              :items="employees()"
              selectable-key="employee"
              :items-per-page="pageSize"
              sort-by="employeeDisplayName"
              show-select
              :headers="[{text: this.$tc('ФИО'), value: 'employeeDisplayName'}]"
          >
            <template
                v-slot:item.employeeDisplayName="{ item }">
              {{ item.employeeDisplayName }}<span
                v-if="item.employeeProjectRole"> ({{ item.employeeProjectRole }})</span>
            </template>
          </v-data-table>
        </v-card>
      </v-col>
      <v-col class="calendar_col">
        <full-calendar :options="calendarOptions" ref="calendar"></full-calendar>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">

import Vue from "vue";
import Component from "vue-class-component";
import FullCalendar from "@fullcalendar/vue";
import multiMonthPlugin from '@fullcalendar/multimonth'
import {Vacation} from "@/components/vacations/vacation.service";
import {Prop, Watch} from "vue-property-decorator";
import {CalendarOptions} from "@fullcalendar/core";
import {DateTimeUtils} from "@/components/datetimeutils";
import ruLocale from '@fullcalendar/core/locales/ru';

@Component({components: {FullCalendar}})
export default class VacationsCalendar extends Vue {
  private calendarOptions: CalendarOptions = {
    locale: ruLocale,
    plugins: [multiMonthPlugin],
    initialView: 'multiMonthYear',
    events: [],
    headerToolbar: false,
    multiMonthMaxColumns: 1
  }
  private pageSize = 15;

  @Prop({required: true})
  vacations!: Vacation[];

  @Prop({required: true})
  year!: number;

  private selectedEmployees: any[] = [];
  private selectionWarning: string | null = null;


  @Watch('selectedEmployees')
  private onEmployeeSelected() {
    this.refreshCalendar();
  }

  @Watch('year')
  private onYearUpdated() {
    const now = new Date();
    this.selectedEmployees = [];
    if (this.$refs.calendar) {
      const c = this.$refs.calendar as any;
      if (this.year == now.getFullYear()) {
        c.getApi().gotoDate(now);
      } else {
        c.getApi().gotoDate(new Date(this.year, 0, 1));
      }
    }
  }

  private employees() {
    return this.vacations.map(m => {
      return {
        id: m.employee,
        employeeDisplayName: m.employeeDisplayName,
        employeeProjectRole: m.employeeCurrentProject?.role
      };
    }).filter((v, i, a) => a.findIndex(t => (t.id === v.id)) === i);
  }

  private refreshCalendar() {
    let selected = this.selectedEmployees.map(e => e.id);
    if (selected.length > this.pageSize) {
      this.selectionWarning = this.$tc('Пожалуйста, выберите не более 30 сотрудников');
      selected = selected.slice(0, this.pageSize);
    } else {
      this.selectionWarning = null;
    }
    const filteredVacations = this.vacations.filter(v =>
        selected.indexOf(v.employee) >= 0);
    this.calendarOptions.events = filteredVacations.map(v => ({
      title: v.employeeDisplayName + ' (' + DateTimeUtils.formatDateFromIsoDateTime(v.startDate) + ' - ' + DateTimeUtils.formatDateFromIsoDateTime(v.endDate) + ')',
      start: v.startDate,
      end: DateTimeUtils.endOfDay(v.endDate)
    }));
  }

}
</script>

<style scoped lang="scss">
.calendar_col {
  max-width: 800px;
}
</style>
