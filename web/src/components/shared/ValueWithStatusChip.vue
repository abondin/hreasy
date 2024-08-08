<template>
  <span>
    <span v-if="isOk()"><slot>{{ value.value }}</slot></span>
    <v-chip small v-else-if="isWaring()" color="warning"><slot>{{ value.value }}</slot></v-chip>
    <v-chip small v-else-if="isError()" color="error"><slot>{{ value.value }}</slot></v-chip>
    <span v-else>n/a</span>
  </span>
</template>

<script lang="ts">

import Component from "vue-class-component";
import Vue from 'vue'
import {Prop} from "vue-property-decorator";
import {ValueWithStatus, ValueWithStatusEnum} from "@/store/modules/dict";


@Component({})
export default class ValueWithStatusChip<T> extends Vue {

  @Prop({required: true})
  private value!: ValueWithStatus<T>;


  private isOk() {
    return this.value && this.value.status === ValueWithStatusEnum.OK;
  }

  private isWaring() {
    return this.value && this.value.status === ValueWithStatusEnum.WARNING;
  }

  private isError() {
    return this.value && this.value.status === ValueWithStatusEnum.ERROR;
  }
}

</script>
