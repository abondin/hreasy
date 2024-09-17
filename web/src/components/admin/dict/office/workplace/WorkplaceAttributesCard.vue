<!--
  Add and update workplace attributes
-->
<template>
  <v-card v-if="data.selectedWorkplace">
    <v-card-title>
      {{ data.selectedWorkplace.id ? $t('Обновить данные по рабочему месту') : $t('Создать новое рабочее место') }}
    </v-card-title>
    <v-card-text>
      <v-form>
        <v-text-field
            v-model="data.selectedWorkplace.name"
            counter="256"
            :rules="[v=>(v && v.length <= 256 || $t('Обязательное поле. Не более N символов', {n:256}))]"
            :label="$t('Наименование')">
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


}
</script>

