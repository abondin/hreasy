<template>
  <v-card :loading="data.loading">
    <v-card-title>
      {{ $t('Рабочие места') }}
    </v-card-title>
    <v-card-text>
      <v-list min-width="400px">
        <v-list-item-group :multiple="false" v-model="data.selectedWorkplace">
          <v-list-item selectable v-for="(w, i) in data.workplaces" v-bind:key="w.id"
                       :value="data.workplaces[i]">
            <v-list-item-icon v-if="w.id" draggable @dragstart="startDrag($event, w)">
              🪑
            </v-list-item-icon>
            <v-list-item-title>{{ w.id ? w.name : $t('Новое место') }}</v-list-item-title>
            <v-list-item-subtitle>{{ w.description }}</v-list-item-subtitle>
          </v-list-item>
        </v-list-item-group>
      </v-list>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import WorkplacesDataContainer from "@/components/admin/dict/office/workplace/workplaces.data.container";
import {DictOfficeWorkplace} from "@/components/admin/dict/dict.admin.service";
import logger from "@/logger";


@Component({
  components: {}
})
export default class WorkplacesFilterComponent extends Vue {
  private listIndex: number | null = null;

  @Prop({required: true})
  private data!: WorkplacesDataContainer;


  private startDrag(event: any, w: DictOfficeWorkplace) {
    logger.log(`Start drag ${w.name} : ${event}`);
    this.$nextTick(() => this.data.selectedWorkplace = w);
  }


}
</script>

