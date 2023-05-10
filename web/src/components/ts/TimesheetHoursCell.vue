<template>
  <div class="d-flex flex-row align-center justify-start" v-if="value && value.record"
       :style="'width: 100%; height:100%; '+((value&&!value.workingDay)?'background:rgba(100, 0, 0, 0.1)':'')">
    <v-text-field v-if="editMode" v-model="value.record.hoursSpent"></v-text-field>
    <span v-else>{{ value.record.hoursSpent || '-' }}</span>
  </div>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import {EmployeeOneDayTimesheet} from "@/components/ts/timesheetUiDto";

@Component({components: {}})
export default class TimesheetHoursCell extends Vue {
  @Prop({required: true})
  private value: EmployeeOneDayTimesheet | undefined;
  @Prop({required: true})
  private editMode!: boolean;

  private edit() {
    this.$nextTick(function () {
      this.$emit('edit', this.value);
    })
  }
}
</script>

<style lang="scss">
@import "~vuetify/src/styles/settings/_colors.scss";

.v-data-table > .v-data-table__wrapper > table > tbody > tr > td {
  padding-right: 2px;
  padding-left: 2px;
}

</style>

