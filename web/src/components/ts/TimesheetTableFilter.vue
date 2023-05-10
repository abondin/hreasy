<template>
  <v-container fluid class="pb-0">
    <v-row>
      <v-col lg="2" sm="6" xs="6" class="pb-0">
        <!-- Selected year -->
        <v-select
            v-model="input.year"
            :items="allYears"
            :label="$t('Год')"></v-select>
      </v-col>

      <v-col lg="4" sm="6" xs="6" class="pb-0">
        <!-- Dates selection filter -->
        <my-date-range-component ref="dateSelector" v-model="input.selectedDates"
                                 :allowed-short-cut="['month']"
                                 :label="$t('Период')"></my-date-range-component>
      </v-col>

      <v-col lg="3" sm="6" xs="6" class="pb-0">
        <v-select
            clearable
            v-model="input.ba"
            :items="allBas.filter(p=>p.active)"
            item-value="id"
            item-text="name"
            :label="$t('Бизнес аккаунт')"
            multiple
        ></v-select>
      </v-col>


      <v-col lg="3" sm="6" xs="6" class="pb-0">
        <v-select
            v-model="input.project"
            :items="allowedProjects"
            :label="$t('Проект')"
        ></v-select>
      </v-col>
    </v-row>
  </v-container>

</template>

<script lang="ts">
import Component from "vue-class-component";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import Vue from "vue";
import {Prop, Watch} from "vue-property-decorator";
import {DateTimeUtils} from "@/components/datetimeutils";
import MyDateRangeComponent from "@/components/shared/MyDateRangeComponent.vue";
import {ProjectDictDto, SimpleDict} from "@/store/modules/dict";
import {TimesheetTableFilterData} from "@/components/ts/timesheetUiDto";


@Component(
    {components: {MyDateRangeComponent, MyDateFormComponent}}
)
export default class TimesheetTableFilter extends Vue {
  @Prop({required: true})
  private input!: TimesheetTableFilterData;

  @Prop({required: true})
  private allBas!: Array<SimpleDict>;

  @Prop({required: true})
  private allProjects!: Array<ProjectDictDto>;

  private allowedProjects = new Array<{ value?: number | null; text?: string | null; divider?: boolean }>();

  private allYears = DateTimeUtils.defaultYears();


  @Watch("input.selectedDates")
  private watchSelectedDates() {
    this.emitUpdated();
  }

  @Watch("input.project")
  private watchProject() {
    this.emitUpdated();
  }

  public emitUpdated() {
    this.$nextTick(function () {
      this.$emit('updated');
    })
  }

  @Watch("input.ba")
  private baChanged() {
    this.input.project = null;
    this.allowedProjects.length = 0;
    this.allowedProjects.push({value: null, text: this.$tc('Без проекта')});
    this.allowedProjects.push({divider: true});
    if (this.input.ba) {
      this.allowedProjects = this.allowedProjects.concat(this.allProjects.filter(p => p.active && p.baId == this.input.ba).map(p => {
        return {
          value: p.id,
          text: p.name
        }
      }));
    }
  }

}
</script>

<style scoped>

</style>
