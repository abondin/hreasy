<template>
  <div v-if="value"
       :class="{'notWorkingDay':(value&&!value.workingDay)}">
    <v-text-field v-if="editMode" v-model="value.hoursSpent"
                  type="number" hide-spin-buttons
                  :autofocus="autofocus"
                  @focus="$event.target.select()"></v-text-field>
    <span v-else>{{ value.hoursSpent || '-' }}</span>
  </div>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import {TimesheetAggregatedByEmployeeDay} from "@/components/ts/timesheetUiDto";

@Component({components: {}})
export default class TimesheetHoursCell extends Vue {
  @Prop({required: true})
  private value: TimesheetAggregatedByEmployeeDay | undefined;
  @Prop({required: true})
  private editMode!: boolean;
  @Prop({required: true})
  private autofocus!: boolean;
}
</script>

<style lang="scss">
@import "~vuetify/src/styles/settings/_colors.scss";

.v-data-table > .v-data-table__wrapper > table > tbody > tr > td {
  padding-right: 2px;
  padding-left: 2px;
}

.notWorkingDay {
  background-color: #FCE4EC;
}

</style>

