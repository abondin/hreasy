<template>
  <a v-if="value" href="#" @click.prevent="alert('a')">
    <span :class="(value.billable ? 'billable':'non-billable')">{{ value.hoursSpent }}</span>
    /
    <span>{{ value.hoursPlanned }}</span>
  </a>
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

<style lang="scss">
@import "~vuetify/src/styles/settings/_colors.scss";

.billable {
  color: map-get($green, 'base');
}

.non-billable {
  color: map-get($red, 'base');
}
</style>

