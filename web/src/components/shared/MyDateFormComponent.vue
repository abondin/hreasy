<template>
  <div>
    <v-menu
        ref="menu"
        v-model="menu"
        :close-on-content-click="false"
        transition="scale-transition"
        offset-y
        min-width="auto"
        max-width="290px">
      <template v-slot:activator="{ on, attrs }">
        <v-text-field
            clearable
            ref="dateField"
            :label="label"
            :value="formattedValue"
            @input="textFieldUpdated"
            :rules="rules">
          <template v-slot:append>
            <v-btn x-small icon v-bind="attrs" v-on="on">
              <v-icon>mdi-calendar</v-icon>
            </v-btn>
          </template>
        </v-text-field>
      </template>
      <v-date-picker
          :first-day-of-week="1"
          :value="date"
          @input="calendarSelected">
      </v-date-picker>
    </v-menu>
  </div>
</template>

<script lang="ts">

import Component from "vue-class-component";
import Vue from 'vue'
import {Prop, Watch} from "vue-property-decorator";
import moment, {Moment} from "moment";
import {DateTimeUtils} from "@/components/datetimeutils";
import logger from "@/logger";


@Component
export default class MyDateFormComponent extends Vue {

  @Prop()
  private value!: string;

  @Prop({required: true})
  private label!: string;

  @Prop({required: false, type: Array})
  private rules: any;

  private menu = false;

  private date = this.value ? this.value : '';

  private formattedValue = '';

  created() {
    this.date = this.value ? this.value : '';
    this.updateFormattedValue(this.date);
  }

  @Watch("value")
  watchValue() {
    this.reset();
  }

  public reset() {
    this.date = this.value ? this.value : '';
    this.updateFormattedValue(this.date);
  }


  private updateFormattedValue(value: string) {
    this.formattedValue = '';
    const d = DateTimeUtils.dateFromIsoString(value);
    if (d.isValid()) {
      this.formattedValue = d.format(DateTimeUtils.DEFAULT_DATE_PATTERN);
    }
  }

  private calendarSelected(isoDate: any) {
    const d = DateTimeUtils.dateFromIsoString(isoDate);
    this.updateFormattedValue(isoDate);
    this.emit(d)
  }

  private textFieldUpdated(formattedDate: string) {
    const d = formattedDate ? DateTimeUtils.dateFromFormattedString(formattedDate) : null;
    this.emit(d);
  }

  private emit(d: Moment | null) {
    if (d === null) {
      this.menu = false;
      this.date = '';
      this.$emit('input', this.date);
    } else if (d.isValid()) {
      this.menu = false;
      this.date = d.format(moment.HTML5_FMT.DATE);
      this.$emit('input', this.date);
    }
  }

}

</script>
