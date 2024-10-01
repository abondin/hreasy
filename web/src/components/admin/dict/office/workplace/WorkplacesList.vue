<template>
  <v-card :loading="data.loading">
    <v-card-title>
      {{ $t('Рабочие места') }}
      <v-spacer></v-spacer>
      <v-tooltip bottom>
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <div v-bind="tattrs" v-on="ton" class="mt-0 pt-0">
            <v-btn color="primary" text icon
                   @click="data.createOrUpdateWorkplaceAction.openDialogForWorkplace(data.filter.officeLocationId, null)">
              <v-icon>mdi-plus</v-icon>
            </v-btn>
          </div>
        </template>
        <span>{{ $t('Создать рабочее место') }}</span>
      </v-tooltip>
    </v-card-title>
    <v-card-text>
      <v-list min-width="400px" dense class="overflow-y-auto" style="max-height: 536px">
        <v-list-item-group :multiple="false" v-model="data.selectedWorkplace">
          <v-list-item two-line selectable v-for="(w, i) in data.workplaces" v-bind:key="w.id"
                       :value="data.workplaces[i]">
            <v-list-item-icon>
              <v-tooltip bottom>
                <template v-slot:activator="{ on: ton, attrs: tattrs}">
                  <v-icon dense v-bind="tattrs" v-on="ton">{{getIcon(w.type)}}</v-icon>
                </template>
                <span>{{ $t('WORKPLACE_TYPE.' +w.type) }}</span>
              </v-tooltip>
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>{{ w.id ? w.name : $t('Новое место') }}</v-list-item-title>
              <v-list-item-subtitle>{{ w.description }}</v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action>
              <v-tooltip bottom v-if="!w.mapX||!w.mapY">
                <template v-slot:activator="{ on: ton, attrs: tattrs}">
                  <v-icon dense v-bind="tattrs" v-on="ton">⚪️</v-icon>
                </template>
                <span>{{ $t('Место не привязано к плану офиса') }}</span>
              </v-tooltip>
            </v-list-item-action>
          </v-list-item>
        </v-list-item-group>
      </v-list>
    </v-card-text>
    <v-card-actions>
      <v-btn color="primary"
             :disabled="!data.selectedWorkplace"
             text
             @click="data.createOrUpdateWorkplaceAction.openDialogForWorkplace(data.filter.officeLocationId, data.selectedWorkplace)">
        {{ $t('Редактировать') }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import WorkplacesDataContainer from "@/components/admin/dict/office/workplace/workplaces.data.container";
import WorkplaceOnMapUtils from "@/components/admin/dict/office/workplace/workplace-on-map-utils";


@Component({
  components: {}
})
export default class WorkplacesFilterComponent extends Vue {

  @Prop({required: true})
  private data!: WorkplacesDataContainer;

  private getIcon = WorkplaceOnMapUtils.getWorkplaceIcon;

}
</script>

