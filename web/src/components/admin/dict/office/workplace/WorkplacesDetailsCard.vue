<template>
  <v-card :loading="data.selectedWorkplace">
    <v-card-title>
      {{ $t('Выбранное рабочие место') }}
    </v-card-title>
    <v-card-text>
      <p class="text-h3">{{data.selectedWorkplace.name}}</p>
      <p>{{data.selectedWorkplace.description}}</p>
      <v-btn color="primary" text @click="data.createOrUpdateWorkplaceAction.openDialogForWorkplace(data.filter.officeLocationId, data.selectedWorkplace)">
        {{$t('Редактировать')}}
      </v-btn>
      <v-btn-toggle color="primary" v-model="data.selectOnMapModeEnabled">
        <v-btn>
        {{$t('Выбрать координаты на карте')}}
        </v-btn>
      </v-btn-toggle>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import WorkplacesDataContainer, {
  SingleWorkplaceDataContainer
} from "@/components/admin/dict/office/workplace/workplaces.data.container";


@Component({
  components: {}
})
export default class WorkplacesDetailsCard extends Vue {

  @Prop({required: true})
  private data!: WorkplacesDataContainer;

}
</script>

