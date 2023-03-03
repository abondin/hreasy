<template>
  <v-hover v-slot="{ hover }">
    <div class="d-flex flex-row align-center justify-start" v-if="value"
         :style="'width: 100%; height:100%; '+((value&&!value.workingDay)?'background:rgba(100, 0, 0, 0.1)':'')">
      <div v-if="value.hoursPlanned || value.hoursSpent">
        <span :class="(value.billable ? 'billable':'non-billable')">{{ value.hoursSpent }}</span>
        /
        <span>{{ value.hoursPlanned }}</span>
      </div>
      <v-btn x-small v-if="hover" icon @click.prevent="edit()">
        <v-icon>mdi-pencil</v-icon>
      </v-btn>
    </div>
  </v-hover>
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

  private edit() {
    this.$nextTick(function () {
      this.$emit('edit', this.value);
    })
  }
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

.v-data-table > .v-data-table__wrapper > table > tbody > tr > td {
  padding-right: 2px;
  padding-left: 2px;
}

</style>

