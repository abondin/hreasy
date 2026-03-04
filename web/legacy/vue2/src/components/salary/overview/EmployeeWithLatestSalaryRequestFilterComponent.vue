<template>
  <v-row>
    <v-col>
      <v-text-field v-if="filter"
                    v-model="filter.search"
                    append-icon="mdi-magnify"
                    :label="$t('Поиск')"
                    single-line
                    hide-details
      ></v-text-field>
    </v-col>
    <v-col cols="auto">
      <v-select
          v-model="filter.ba"
          item-text="name"
          item-value="id"
          :label="$t('Бизнес аккаунт')"
          :multiple="true"
          :items="allBas">
      </v-select>
    </v-col>
    <v-col cols="auto">
      <v-select
          v-model="filter.onlyWithRequests"
          :label="$t('Только с запросами')"
          :multiple="false"
          :items="[{value:false, text:$t('Нет')}, {value:true, text:$t('Да')}]">
      </v-select>
    </v-col>
  </v-row>
</template>

<script lang="ts">
import {Prop, Vue} from "vue-property-decorator";
import Component from "vue-class-component";
import {EmployeeWithLatestSalaryRequest} from "@/components/salary/salary.service";
import {Filter} from "@/components/shared/table/TableComponentDataContainer";
import {searchUtils, TextFilterBuilder} from "@/components/searchutils";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";


export class EmployeeWithLatestSalaryRequestFilter extends Filter<EmployeeWithLatestSalaryRequest> {
  public search = '';
  public ba: number[] = [];
  public onlyWithRequests = true;

  applyFilter(items: EmployeeWithLatestSalaryRequest[]): EmployeeWithLatestSalaryRequest[] {
    return items.filter((item) => {
      let filtered = true;
      const search = this.search.toLowerCase().trim();
      const textFilters = TextFilterBuilder.of()
          .splitWords(item.employeeDisplayName)
          .ignoreCase(item.employeeEmail)
          .ignoreCase(item.employeeCurrentProject?.name)
          .ignoreCase(item.employeeCurrentProject?.role)

      filtered = filtered && searchUtils.textFilter(this.search, textFilters);
      filtered = filtered && searchUtils.array(this.ba, item.employeeBusinessAccount?.id);
      if (this.onlyWithRequests) {
        filtered = filtered && Boolean(item.requestId);
      }
      return filtered;
    });
  }
}

const namespace_dict = 'dict';
@Component({
  components: {}
})
export default class EmployeeWithLatestSalaryRequestFilterComponent extends Vue {

  @Prop({required: true})
  private filter!: EmployeeWithLatestSalaryRequestFilter;

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  /**
   * Lifecycle hook
   */
  created() {
    return this.$nextTick()
        .then(() => this.$store.dispatch('dict/reloadBusinessAccounts'));
  }

}

</script>

<style scoped>

</style>
