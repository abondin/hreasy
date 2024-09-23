<template>
  <v-card :loading="data.loading" height="684px">
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
      <v-list min-width="400px">
        <v-list-item-group :multiple="false" v-model="data.selectedWorkplace">
          <v-list-item two-line selectable v-for="(w, i) in data.workplaces" v-bind:key="w.id"
                       :value="data.workplaces[i]">
            <v-list-item-icon>
              <v-icon>🪑</v-icon>
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>{{ w.id ? w.name : $t('Новое место') }}</v-list-item-title>
              <v-list-item-subtitle>{{ w.description }}</v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action>
              <v-btn text icon
                     @click="data.createOrUpdateWorkplaceAction.openDialogForWorkplace(data.filter.officeLocationId, w)">
                <v-icon>mdi-pencil</v-icon>
              </v-btn>
            </v-list-item-action>
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


@Component({
  components: {}
})
export default class WorkplacesFilterComponent extends Vue {

  @Prop({required: true})
  private data!: WorkplacesDataContainer;

}
</script>

