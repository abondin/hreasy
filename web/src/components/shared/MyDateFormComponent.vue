<template>
  <div>
    <v-menu
        ref="menu"
        v-model="menu"
        :close-on-content-click="false"
        :nudge-right="40"
        transition="scale-transition"
        offset-y
        min-width="290px">
      <template v-slot:activator="{ on, attrs }">
        <v-text-field
            ref="dateField"
            :label="label"
            @input="emitValue"
            :value="date" :rules="rules">
          <template v-slot:prepend>
            <v-btn x-small icon v-bind="attrs" v-on="on">
              <v-icon>mdi-calendar</v-icon>
            </v-btn>
          </template>
        </v-text-field>
      </template>
      <v-date-picker
          :value="date"
          @input="emitValue">
      </v-date-picker>
    </v-menu>
  </div>
</template>

<script lang="ts">

import Component from "vue-class-component";
import Vue from 'vue'
import {Prop, Watch} from "vue-property-decorator";
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

  @Watch("value")
  watchValue() {
    this.date = this.value ? this.value : '';
  }


  private emitValue(event: any) {
    this.menu=false;
    this.date = event;
    return this.$emit('input', this.date);
  }

}

</script>
