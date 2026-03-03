<template>
  <v-combobox
      :label="$t('Роль')"
      :items="roles()"
      hide-selected
      :search-input.sync="roleSearch"
      persistent-hint
      small-chips
      clearable
      :multiple="false"
      :rules="[v=>(!v || v.length <= 64 || $t('Не более N символов', {n:64}))]"
      :value="value"
      @input="valueUpdate">
    <template v-slot:no-data>
      <v-list-item>
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('Элемент не найден') }} "<strong>{{ roleSearch }}</strong>"
            {{ $t('Нажмите') }} <kbd>enter</kbd> {{ $t('чтобы создать') }}.
          </v-list-item-title>
        </v-list-item-content>
      </v-list-item>
    </template>
  </v-combobox>
</template>

<script lang="ts">

import Component from "vue-class-component";
import Vue from 'vue'
import {Prop} from "vue-property-decorator";
import {CurrentProjectRole} from "@/store/modules/dict";


@Component
export default class RoleCombobox extends Vue {

  @Prop({required: true})
  private value!: string;

  @Prop({required: true})
  private allCurrentProjectRoles!: CurrentProjectRole[];

  private roleSearch='';

  private roles() {
    return this.allCurrentProjectRoles.map(r => r.value);
  }

  private valueUpdate(newValue: string) {
    this.$emit('input', newValue);
  }

}

</script>
