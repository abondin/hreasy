<template>
  <div>
    <div ref="timeline" class="timeline"></div>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import {Vacation, VacationStatus} from "@/components/vacations/vacation.service";
import {Prop, Watch} from "vue-property-decorator";
import {DataSet, Timeline, TimelineOptions} from 'vis-timeline/standalone';
import moment from 'moment';
import {DateTimeUtils} from "@/components/datetimeutils";
import colors from 'vuetify/lib/util/colors'

@Component
export default class VacationsTimeline extends Vue {
  @Prop({required: true})
  vacations!: Vacation[];

  @Prop({required: true})
  year!: number;

  private timeline: Timeline | null = null;
  private items = new DataSet<any>();
  private groups = new DataSet<any>();

  private mounted() {
    this.initTimeline();
  }

  @Watch('vacations', {immediate: true, deep: true})
  @Watch('year')
  private onDataChange() {
    this.updateTimelineData();
  }

  private initTimeline() {
    const container = this.$refs.timeline as HTMLElement;
    const options: TimelineOptions = this.getTimelineOptions();
    this.timeline = new Timeline(container, this.items, this.groups, options);
    // Add listener for navigation changes
    this.timeline.on('rangechanged', this.handleRangeChange);
    this.updateTimelineData();
  }

  private getTimelineOptions(): TimelineOptions {
    return {
      stack: false,
      groupOrder: (a, b) => a.content.localeCompare(b.content),
      start: DateTimeUtils.firstDayOfYear(this.year).toDate(),
      end: DateTimeUtils.lastDayOfYear(this.year).toDate(),
      editable: false,
      zoomable: true,
      zoomKey: 'ctrlKey',
      orientation: 'both',
      multiselect: false,
      selectable: false,
      tooltip: {followMouse: true},
      locale: 'ru',
    };
  }

  private updateTimelineData() {
    if (!this.timeline) return;
    this.items.clear();
    this.groups.clear();
    this.groups.add(this.createEmployeeGroups());
    this.items.add(this.createVacationItems());
    this.setInitialTimelineWindow();
  }

  private createEmployeeGroups() {
    const employeeIds = [...new Set(this.vacations.map(v => v.employee))];
    return employeeIds.map(employeeId => {
      const vacation = this.vacations.find(v => v.employee === employeeId);
      return {
        id: employeeId,
        content: vacation?.employeeDisplayName || this.$t('unknown_employee'),
      };
    });
  }

  private createVacationItems() {
    return this.vacations.map((vacation, index) => ({
      id: index,
      group: vacation.employee,
      content: '',
      start: moment(vacation.startDate).toDate(),
      end: vacation.endDate ? moment(vacation.endDate).add(1, 'day').toDate() : undefined,
      title: this.vacationTitle(vacation),
      style: `background-color: ${this.getStatusColor(vacation.status)}`
    }));
  }

  private vacationTitle(vacation: Vacation) {
    return vacation.employeeDisplayName + '<br>' +
        `${this.formatDateRange(vacation.startDate, vacation.endDate)}<br>`
        + this.$t(`VACATION_STATUS_ENUM.${vacation.status}`)
  }

  private setInitialTimelineWindow() {
    this.timeline?.setWindow(
        DateTimeUtils.firstDayOfYear(this.year).toDate(),
        DateTimeUtils.lastDayOfYear(this.year).toDate(),
        {animation: false}
    );
  }

  private handleRangeChange() {
    if (!this.timeline) return;

    // Get the current visible range
    const {start, end} = this.timeline.getWindow();

    // Determine the middle date of the visible range
    const middleDate = moment(start).add(moment(end).diff(start) / 2, 'milliseconds');
    const middleYear = middleDate.year();

    // Emit event if the middle of the range is in a different year
    if (middleYear !== this.year) {
      this.$emit('year-navigation', middleYear);
    }
  }

  private formatDateRange(startDate: string, endDate?: string): string {
    const start = DateTimeUtils.formatFromIso(startDate);
    const end = DateTimeUtils.formatFromIso(endDate);
    return end ? `${start} - ${end}` : start;
  }

  private getStatusColor(status: VacationStatus): string {
    switch (status) {
      case 'PLANNED':
        return colors.cyan.darken3;
      case 'TAKEN':
        return colors.green.darken1;
      case 'COMPENSATION':
        return colors.green.darken4;
      case 'CANCELED':
        return colors.red.base;
      case 'REJECTED':
        return colors.grey.darken2;
      case 'REQUESTED':
        return colors.amber.base;
      default:
        return '';
    }
  }

  private beforeDestroy() {
    this.timeline?.destroy();
    this.timeline = null;
  }
}
</script>

<style scoped lang="css">
@import '~vis-timeline/styles/vis-timeline-graph2d.min.css';

.timeline {
  width: 100%;
  /* Let the timeline height grow naturally */
}

.vacation-status-planned .vis-item-content { background-color: #FFDD57 !important; }      /* Yellow */
.vacation-status-taken .vis-item-content { background-color: #4CAF50 !important; }        /* Green */
.vacation-status-compensation .vis-item-content { background-color: #2196F3 !important; } /* Blue */
.vacation-status-canceled .vis-item-content { background-color: #F44336 !important; }     /* Red */
.vacation-status-rejected .vis-item-content { background-color: #9E9E9E !important; }     /* Gray */
.vacation-status-requested .vis-item-content { background-color: #FF9800 !important; }    /* Orange */


</style>
