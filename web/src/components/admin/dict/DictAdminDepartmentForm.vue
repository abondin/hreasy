<template>
  <v-dialog v-model="data.editDialog" :disabled="data.loading">
    <v-form ref="depAdminForm" v-if="data.updateBody">
      <v-card>
        <v-card-title v-if="data.isEditItemNew()">{{ $t('Создание отдела') }}</v-card-title>
        <v-card-title v-else>{{ $t('Изменение отдела') }}</v-card-title>
        <v-card-text>
          <!-- name -->
          <v-text-field
              v-model="data.updateBody.name"
              :counter="255"
              :rules="[v=>(v && v.length <= 255 || $t('Обязательное поле. Не более N символов', {n:255}))]"
              :label="$t('Наименование')"
              required>
            >
          </v-text-field>

          <v-select
              v-model="data.updateBody.archived"
              :label="$t('Архив')"
              :items="[{value:false, text:'Нет'}, {value:true, text:'Да'}]">
          </v-select>

          <!-- Error block -->
          <v-alert v-if="data.editError" type="error">
            {{ data.editError }}
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-progress-circular class="mr-2" v-if="data.loading" indeterminate></v-progress-circular>
          <v-btn @click="data.closeEditDialog()">{{ $t('Закрыть') }}</v-btn>
          <v-btn @click="()=>data.submitEditForm()" color="primary">{{
              data.isEditItemNew() ? $t('Создать') : $t('Изменить')
            }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-form>
  </v-dialog>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import TableComponentDataContainer from "@/components/admin/dict/TableComponentDataContainer";
import {DictDepartment, DictDepartmentUpdateBody} from "@/components/admin/dict/dict.admin.service";

@Component
export default class DictAdminDepartmentForm extends Vue {

  @Prop({required: true})
  private data!: TableComponentDataContainer<DictDepartment, DictDepartmentUpdateBody>;

}
</script>

