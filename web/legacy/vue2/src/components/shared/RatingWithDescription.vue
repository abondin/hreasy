<template>
  <v-container class="pa-0 ma-0">
    <v-row no-gutters align="center" justify="center">
      <v-col no-gutters>
        <div class="v-label pr-5">{{ name }}
          <v-tooltip bottom max-width="500px">
            <template v-slot:activator="{ on: ton, attrs: tattrs}">
              <v-icon small v-bind="tattrs" v-on="ton">mdi-help-circle</v-icon>
            </template>
            <slot name="description"></slot>
          </v-tooltip>
        </div>
      </v-col>
      <v-col cols="auto" no-gutters>
        <v-tooltip bottom v-if="prev && prev != value">
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <v-chip v-bind="tattrs" v-on="ton" :set="diff=value-prev"
                    small :color="diff<0?'error':'success'">
              {{ diff > 0 ? '+' + diff : diff }}
            </v-chip>
          </template>
          {{ $t('По сравнению с последним результатом') }}
        </v-tooltip>
      </v-col>
      <v-col cols="auto" no-gutters>
        <v-rating :readonly="readonly"
                  :value="value"
                  :clearable="!required"
                  x-large
                  @input="handleInput"></v-rating>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">

import Component from "vue-class-component";
import Vue from 'vue'
import {Prop} from "vue-property-decorator";


@Component({})
export default class RatingWithDescription extends Vue {

  @Prop({required: true})
  private value!: number;

  @Prop({required: false})
  private prev?: number;

  @Prop({required: true})
  private name!: number;

  @Prop({required: false, default: false})
  private required!: boolean;

  @Prop({required: false, default: 5})
  private length!: number;

  @Prop({required: false, default: false})
  private readonly!: boolean;

  handleInput(value: number) {
    this.$emit('input', value);
  }

}

</script>

