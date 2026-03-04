<template>
  <v-row dense>
    <v-col>
      <v-text-field
          v-model="model.filter.search"
          append-icon="mdi-magnify"
          :label="$t('Поиск')"
          single-line
          hide-details
      ></v-text-field>
    </v-col>
    <v-col cols="auto">
      <v-checkbox
          class="mr-5"
          v-model="model.filter.onlySelectedProject"
          :label="$t('Только сотрудники выбранного проекта')"
      ></v-checkbox>
    </v-col>
  </v-row>

</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";

class TimesheetTableFilter {
  public search = '';
  public onlySelectedProject = true;
}

export class TimesheetTableToolbarData {
  public filter = new TimesheetTableFilter();
}

@Component(
    {components: {}}
)
export default class TimesheetTableToolbar extends Vue {
  @Prop({required: true})
  private model!: TimesheetTableToolbarData;

}
</script>

<style scoped>

</style>
