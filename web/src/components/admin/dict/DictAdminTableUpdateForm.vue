<template>
  <v-dialog v-bind:value="data.updateDialog" :disabled="data.loading" persistent>
    <v-form ref="dictAdminUpdateForm" v-if="data.updateBody">
      <v-card>
        <v-card-title>{{ $t('Изменение') }}</v-card-title>
        <v-card-text>
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

          <!-- Error block -->
          <v-alert v-if="data.updateError" type="error">
            {{ data.updateError }}
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-progress-circular class="mr-2" v-if="data.loading" indeterminate></v-progress-circular>
          <v-btn @click="data.closeUpdateDialog()">{{ $t('Закрыть') }}</v-btn>
          <v-btn @click="()=>data.submitUpdateForm()" color="primary" :disabled="data.loading">{{
              $t('Изменить')
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
import TableComponentDataContainer, {
  CreateBody,
  Filter,
  UpdateBody,
  WithId
} from "@/components/admin/dict/TableComponentDataContainer";

@Component
export default class DictAdminTableForm<T extends WithId, M extends UpdateBody, C extends CreateBody, F extends Filter<T>> extends Vue {

  @Prop({required: true})
  private data!: TableComponentDataContainer<T, M, C, F>;

}
</script>

