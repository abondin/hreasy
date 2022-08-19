<template>
  <hreasy-table :data="data">
    <template v-slot:filters>
      <v-col>
        <v-text-field v-if="data.filter"
                      v-model="data.filter.search"
                      append-icon="mdi-magnify"
                      :label="$t('Поиск')"
                      single-line
                      hide-details
        ></v-text-field>
      </v-col>

      <v-col cols="auto">
        <v-select
            v-model="data.filter.onlyNotArchived"
            :label="$t('Скрыть архивные')"
            :items="[{value:false, text:'Нет'}, {value:true, text:'Да'}]">
        </v-select>
      </v-col>
    </template>


    <template
        v-slot:item.archived="{ item }">
      {{ item.archived ? $t('Да') : $t('Нет') }}
    </template>

    <template v-slot:updateFormFields>
      <!-- name -->
      <v-text-field id="dict-form-name"
                    v-model="data.updateBody.name"
                    :counter="255"
                    :rules="[v=>(v && v.length <= 255 || $t('Обязательное поле. Не более N символов', {n:255}))]"
                    :label="$t('Наименование')"
                    required>
        >
      </v-text-field>

      <!-- Additional fields -->
      <slot name="additionalFields"></slot>

      <v-select
          v-model="data.updateBody.archived"
          :label="$t('Архив')"
          :items="[{value:false, text:'Нет'}, {value:true, text:'Да'}]">
      </v-select>
    </template>

    <template v-slot:createFormFields>
      <!-- name -->
      <v-text-field id="dict-form-name"
                    v-model="data.createBody.name"
                    :counter="255"
                    :rules="[v=>(v && v.length <= 255 || $t('Обязательное поле. Не более N символов', {n:255}))]"
                    :label="$t('Наименование')"
                    required>
        >
      </v-text-field>

      <!-- Additional fields -->
      <slot name="additionalFields"></slot>

      <v-select
          v-model="data.createBody.archived"
          :label="$t('Архив')"
          :items="[{value:false, text:'Нет'}, {value:true, text:'Да'}]">
      </v-select>
    </template>

  </hreasy-table>
</template>

<script lang="ts">
import Component from "vue-class-component";
import {CreateBody, UpdateBody} from "@/components/shared/table/TableComponentDataContainer";
import HreasyTable from "@/components/shared/table/HreasyTable.vue";
import {BasicDict, BasicDictFilter} from "@/components/admin/dict/DictTableComponentDataContainer";


@Component({
  components: {HreasyTable}
})
export default class DictAdminTable<T extends BasicDict, M extends UpdateBody, C extends CreateBody, F extends BasicDictFilter<T>> extends HreasyTable<T, M, C, F> {

}
</script>

