<!--
  Add and update workplace attributes
-->
<template>
  <v-card v-if="data" height="684px">
    <v-card-title>
      {{ (!data.selectedWorkplace || data.selectedWorkplace?.id) ? $t('Параметры рабочего места') : $t('Создать новое рабочее место') }}
    </v-card-title>
    <v-card-text v-if="data.selectedWorkplace">
      <v-form>
        <v-text-field
            v-model="data.selectedWorkplace.name"
            counter="256"
            :rules="[v=>(v && v.length <= 256 || $t('Обязательное поле. Не более N символов', {n:256}))]"
            :label="$t('Наименование')">
        </v-text-field>
        <v-text-field
            v-model="data.selectedWorkplace.mapX"
            type="number"
            :label="$t('Позиция X')">
        </v-text-field>
        <v-text-field
            v-model="data.selectedWorkplace.mapY"
            type="number"
            :label="$t('Позиция Y')">
        </v-text-field>
        <v-textarea
            v-model="data.selectedWorkplace.description"
            :rules="[v=>(!v || v.length <= 4096 || $t('Не более N символов', {n:4096}))]"
            :label="$t('Примечание')">
        </v-textarea>
        <v-select
            v-model="data.selectedWorkplace.archived"
            :items="[{value:false, text:$t('Нет')}, {value:true, text:$t('Да')}]"
            :label="$t('Архив')"></v-select>
      </v-form>
    </v-card-text>
    <v-card-text v-else>
      <p>{{$t('Необходимо выбрать рабочее место')}}</p>
    </v-card-text>
    <v-card-actions v-if="data.selectedWorkplace">
      <v-spacer></v-spacer>
      <v-btn @click="submit()" color="primary">{{ data.selectedWorkplace.id ? $t('Изменить') : $t('Создать') }}</v-btn>
    </v-card-actions>
  </v-card>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import {SingleWorkplaceDataContainer} from "@/components/admin/dict/office/workplace/workplaces.data.container";


@Component({
  components: {}
})
export default class WorkplaceAttributesCard extends Vue {
  @Prop({required: true})
  private data!: SingleWorkplaceDataContainer;

  private submit() {
    this.data.submitSelectedWorkplaceOnBackend().then(() => {
      this.$emit('submit');
    });
  }

}
</script>

