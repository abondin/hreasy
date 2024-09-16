<template>
  <v-card :loading="data.loading">
    <v-card-title>
      {{$t('Рабочие места')}}
    </v-card-title>
    <v-card-text>
      <v-list>
        <v-list-item selectable v-for="w in data.workplaces" v-bind:key="w.id">
          {{ w.name }}
        </v-list-item>
      </v-list>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import WorkplacesDataContainer from "@/components/admin/dict/office/workplace/workplaces.data.container";


@Component({
  components: {}
})
export default class WorkplacesFilterComponent extends Vue {
  @Prop({required: true})
  private data!: WorkplacesDataContainer;


}
</script>

