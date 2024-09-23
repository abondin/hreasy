<!--
  Add and update workplace attributes
-->
<template>
  <span v-if="formData">
    <v-text-field
        v-model="formData.name"
        counter="256"
        :rules="[v=>(v && v.length <= 256 || $t('Обязательное поле. Не более N символов', {n:256}))]"
        :label="$t('Наименование')">
    </v-text-field>
    <v-text-field
        v-model="formData.mapX"
        type="number"
        :label="$t('Позиция X')">
    </v-text-field>
    <v-text-field
        v-model="formData.mapY"
        type="number"
        :label="$t('Позиция Y')">
    </v-text-field>
    <v-textarea
        v-model="formData.description"
        :rules="[v=>(!v || v.length <= 4096 || $t('Не более N символов', {n:4096}))]"
        :label="$t('Примечание')">
    </v-textarea>
    <v-select
        v-model="formData.archived"
        :items="[{value:false, text:$t('Нет')}, {value:true, text:$t('Да')}]"
        :label="$t('Архив')"></v-select>
  </span>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import {DictOfficeWorkplaceUpdateBody} from "@/components/admin/dict/dict.admin.service";


@Component({
  components: {}
})
export default class WorkplaceAttributesFormData extends Vue {
  @Prop({required: true})
  private formData!: DictOfficeWorkplaceUpdateBody;
}
</script>

