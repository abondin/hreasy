<template>
  <span>
    <span v-if="value">
      <span :class="value.billable ? 'text-green':'text-red'">{{ value.hoursPlanned }}</span>
    </span>
  </span>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {TimesheetHours} from "@/components/ts/timesheet.service";
import {Prop} from "vue-property-decorator";

@Component({components: {}})
export default class TimesheetHoursCell extends Vue {
  @Prop({required: true})
  private value: TimesheetHours | undefined;
}
</script>

