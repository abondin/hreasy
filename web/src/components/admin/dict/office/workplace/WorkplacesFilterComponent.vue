<template>
  <v-row v-if="data">
    <v-col>
      <v-autocomplete
          clearable
          class="mr-5"
          v-model="data.filter.officeId"
          :items="allOffices.filter(p=>p.active)"
          item-value="id"
          item-text="name"
          :label="$t('Офис')"
      ></v-autocomplete>
    </v-col>
    <v-col>
      <v-autocomplete
          clearable
          class="mr-5"
          v-model="data.filter.officeLocationId"
          :items="allOfficeLocations.filter(p=>p.active && p.officeId==data.filter.officeId)"
          item-value="id"
          item-text="name"
          :label="$t('Кабинет')"
      ></v-autocomplete>
    </v-col>
  </v-row>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {DictOfficeWorkplace} from "@/components/admin/dict/dict.admin.service";
import {Getter} from "vuex-class";
import {OfficeLocationDict, SimpleDict} from "@/store/modules/dict";
import {Prop, Watch} from "vue-property-decorator";
import {Filter} from "@/components/shared/table/TableComponentDataContainer";
import WorkplacesDataContainer from "@/components/admin/dict/office/workplace/workplaces.data.container";

export class WorkplacesFilterContainer extends Filter<DictOfficeWorkplace> {
  officeId: number | null = null;
  officeLocationId: number | null = null;
  onlyNotArchived = true;

  applyFilter(items: DictOfficeWorkplace[]): DictOfficeWorkplace[] {
    return items.filter((item) => {
      let filtered = true;
      filtered = filtered && (this.officeId == item.office?.id);
      filtered = filtered && (!this.officeLocationId || this.officeLocationId == item.officeLocation?.id);
      if (this.onlyNotArchived) {
        filtered = filtered && Boolean(!item.archived);
      }
      return filtered;
    });
  }
}

const namespace_dict = 'dict';
@Component({
  components: {}
})
export default class WorkplacesFilterComponent extends Vue {

  @Getter("offices", {namespace: namespace_dict})
  private allOffices!: Array<SimpleDict>;

  @Getter("officeLocations", {namespace: namespace_dict})
  private allOfficeLocations!: Array<OfficeLocationDict>;

  @Prop({required: true})
  private data!: WorkplacesDataContainer;

  created() {
    this.$store.dispatch('dict/reloadOffices').then(() => {
      return this.$store.dispatch('dict/reloadOfficeLocations')
          .then(() => {
            const officeId = Number(this.$route.query['officeId']) || null;
            if (officeId && this.allOffices.map(o => o.id).indexOf(officeId) >= 0) {
              this.data.filter.officeId = officeId;
            }
            const officeLocationId = Number(this.$route.query['officeLocationId']) || null;
            if (officeLocationId && this.allOfficeLocations.map(o => o.id).indexOf(officeLocationId) >= 0) {
              this.data.filter.officeLocationId = officeLocationId;
            }
          })
    });
  }

  @Watch("data.filter.officeId")
  officeChanged() {
    this.data.filter.officeLocationId = null;
  }
}
</script>

