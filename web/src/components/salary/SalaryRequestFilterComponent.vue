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
          :label="$t('Бюджет из бизнес аккаунта')"
          :multiple="true"
          :items="allBas">
      </v-select>
    </v-col>
    <v-col cols="auto">
      <v-select
          v-model="filter.impl"
          :label="$t('Завершён')"
          :multiple="true"
          :items="[{value:false, text:$t('Нет')}, {value:true, text:$t('Да')}]">
      </v-select>
    </v-col>
    <!--
      <v-col cols="auto">
        <v-select
            v-model="filter.implState"
            :label="$t('Результат')"
            :multiple="true"
            :items="salaryStats">
        </v-select>
      </v-col>
      -->
  </v-row>
</template>

<script lang="ts">
import {Prop, Vue} from "vue-property-decorator";
import Component from "vue-class-component";
import {SalaryIncreaseRequest, SalaryRequestType} from "@/components/salary/salary.service";
import {Filter} from "@/components/shared/table/TableComponentDataContainer";
import {searchUtils, TextFilterBuilder} from "@/components/searchutils";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";
import {salaryRequestImplementationStates} from "@/components/admin/salary/admin.salary.service";


export class SalaryRequestFilter extends Filter<SalaryIncreaseRequest> {
  public search = '';
  public implState: number[] = [];
  public impl: boolean[] = [];
  public type: number = SalaryRequestType.SALARY_INCREASE;
  public ba: number[] = [];

  applyFilter(items: SalaryIncreaseRequest[]): SalaryIncreaseRequest[] {
    return items.filter((item) => {
      let filtered = true;
      const search = this.search.toLowerCase().trim();
      const textFilters = TextFilterBuilder.of()
          .splitWords(item.employee?.name)
          .splitWords(item.createdBy?.name)
          .ignoreCase(item?.budgetBusinessAccount.name)
          .ignoreCase(item?.req?.reason);

      filtered = filtered && searchUtils.textFilter(this.search, textFilters);
      filtered = filtered && searchUtils.array(this.implState, item.impl?.state);
      filtered = filtered && this.type === item.type;
      filtered = filtered && searchUtils.array(this.ba, item.budgetBusinessAccount?.id);
      filtered = filtered && searchUtils.array(this.impl, Boolean(item.impl?.implementedAt));
      return filtered;
    });
  }
}

const namespace_dict = 'dict';
@Component({
  components: {}
})
export default class SalaryRequestFilterComponent extends Vue {

  @Prop({required: true})
  private filter!: SalaryRequestFilter;

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  private salaryStats = salaryRequestImplementationStates.map(v => {
    return {text: this.$tc(`SALARY_REQUEST_STAT.${v}`), value: v};
  });

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
