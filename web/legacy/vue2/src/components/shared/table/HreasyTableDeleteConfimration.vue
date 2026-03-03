<template>
    <v-card>
      <v-card-title>{{ $t('Удаление записей') }}</v-card-title>
      <v-card-text>
        {{ $t('Вы уверены, что хотите удалить') }} {{$tc('records', data.selectedItems.length)}}?
      </v-card-text>
      <!-- Error block -->
      <v-alert v-if="data.actionError" type="error">
        {{ data.actionError }}
      </v-alert>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-progress-circular class="mr-2" v-if="data.loading" indeterminate></v-progress-circular>
        <v-btn @click="data.closeDeleteDialog()">{{ $t('Закрыть') }}</v-btn>
        <v-btn @click="()=>data.submitDeleteForm()" color="primary" :disabled="data.loading">{{
            $t('Удалить')
          }}
        </v-btn>
      </v-card-actions>
    </v-card>
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
} from "@/components/shared/table/TableComponentDataContainer";

@Component
export default class HreasyTableDeleteConfimration<T extends WithId, M extends UpdateBody, C extends CreateBody, F extends Filter<T>> extends Vue {

  @Prop({required: true})
  private data!: TableComponentDataContainer<T, M, C, F>;

}
</script>

