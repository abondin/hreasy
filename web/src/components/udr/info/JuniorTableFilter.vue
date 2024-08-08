<!-- Detailed information for selected junior-->
<template>
  <v-row v-if="data">
    <v-col>
      <v-text-field
          v-model="data.filter.search"
          append-icon="mdi-magnify"
          :label="$t('Поиск')"
          single-line
          hide-details
      ></v-text-field>
    </v-col>
    <v-col>
      <v-autocomplete
          clearable
          class="mr-5"
          v-model="data.filter.selectedBas"
          :items="allBas.filter(p=>p.active)"
          item-value="id"
          item-text="name"
          :label="$t('Бизнес Аккаунт')"
          multiple
      ></v-autocomplete>
    </v-col>
    <v-col>
      <v-autocomplete
          clearable
          class="mr-5"
          v-model="data.filter.selectedRoles"
          :items="allRoles()"
          :label="$t('Роль')"
          multiple
      ></v-autocomplete>
    </v-col>
    <v-col cols="auto">
      <v-select
          v-model="data.filter.onlyNotGraduated"
          :label="$t('Скрыть закончивших обучение')"
          :items="[{value:false, text:$t('Нет')}, {value:true, text:$t('Да')}]">
      </v-select>
    </v-col>
  </v-row>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {Prop} from "vue-property-decorator";
import {JuniorRegistryDataContainer} from "@/components/udr/junior-registry.data.container";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";


const namespace_dict = 'dict';
@Component({
  components: {}
})
export default class JuniorTableFilter extends Vue {

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  @Prop({required: true})
  data!: JuniorRegistryDataContainer;

  /**
   * Lifecycle hook
   */
  created() {
    this.$store.dispatch('dict/reloadBusinessAccounts')
  }


  private allRoles() {
    if (this.data?.items) {
      return [...new Set(this.data.filteredItems().map(j => j.role))].sort();
    }
    return [];
  }

}
</script>


