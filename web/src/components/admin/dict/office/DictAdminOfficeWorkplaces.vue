<template>
  <v-container v-if="data">
    <v-row v-if="data.error">
      <v-col>
        <v-alert type="error">{{ data.error }}</v-alert>
      </v-col>
    </v-row>
    <v-row dense>
      <!-- Refresh button -->
      <v-col align-self="center" cols="auto">
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="mt-0 pt-0">
              <v-btn text icon @click="data.reloadData()">
                <v-icon>refresh</v-icon>
              </v-btn>
            </div>
          </template>
          <span>{{ $t('Обновить данные') }}</span>
        </v-tooltip>
      </v-col>
      <!-- Filters -->
      <workplaces-filter-component :data="data"></workplaces-filter-component>
    </v-row>
    <v-row v-if="data.filter.officeLocationId" fill-height>
      <!-- Workplaces to select -->
      <v-col cols="auto">
        <workplaces-list :data="data"></workplaces-list>
      </v-col>
      <!-- Interactive Map -->
      <v-col>
        <office-location-map-component :data="data"></office-location-map-component>
      </v-col>
    </v-row>
    <v-row v-else justify="center">
      <v-col cols="auto">
        <v-alert type="info">{{ $t('Для начала работы необходимо выбрать кабинет') }}</v-alert>
      </v-col>
    </v-row>

    <in-dialog-form size="lg" form-ref="createOrUpdateMapForm" :data="data.createOrUpdateWorkplaceAction"
                    :title="data.createOrUpdateWorkplaceAction.isNew() ? $t('Добавить рабочее место') : $t('Обновить рабочее место')"
                    v-on:submit="data.reloadData()">
      <template v-slot:fields>
        <workplace-attributes-form-data :form-data="data.createOrUpdateWorkplaceAction.formData"/>
      </template>
    </in-dialog-form>

  </v-container>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";
import WorkplacesFilterComponent from "@/components/admin/dict/office/workplace/WorkplacesFilterComponent.vue";
import {Prop, Watch} from "vue-property-decorator";
import WorkplacesDataContainer from "@/components/admin/dict/office/workplace/workplaces.data.container";
import WorkplacesList from "@/components/admin/dict/office/workplace/WorkplacesList.vue";
import OfficeLocationMapComponent from "@/components/admin/dict/office/workplace/OfficeLocationMapComponent.vue";
import WorkplaceAttributesFormData from "@/components/admin/dict/office/workplace/WorkplaceAttributesFormData.vue";
import WorkplacesOnMap from "@/components/admin/dict/office/workplace/WorkplacesOnMap.vue";
import InDialogForm from "@/components/shared/forms/InDialogForm.vue";

const namespace_dict = 'dict';
@Component({
  components: {
    InDialogForm,
    WorkplaceAttributesFormData,
    OfficeLocationMapComponent,
    WorkplacesOnMap,
    WorkplaceAttributesCard: WorkplaceAttributesFormData,
    WorkspacesOnMap: OfficeLocationMapComponent, WorkplacesList, WorkplacesFilterComponent
  }
})
export default class DictAdminOfficeWorkplaces extends Vue {

  @Getter("officeLocations", {namespace: namespace_dict})
  private allOfficeLocations!: Array<SimpleDict>;

  @Prop({required: false})
  private queryParamOfficeId?: number;
  @Prop({required: false})
  private queryParamOfficeLocationId?: number;

  private data = new WorkplacesDataContainer();


  created() {
    this.$store.dispatch('dict/reloadOfficeLocations')
        .then(() => this.data.loadWorkplaces());
  }


  @Watch("data.filter.officeLocationId")
  officeLocationChanged() {
    this.data.loadOfficeLocationMap();
  }

}
</script>

