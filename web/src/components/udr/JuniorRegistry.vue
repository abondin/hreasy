<template>
  <hreasy-table :data="data" :create-new-title="$t('Добавление в реестр')"
    :update-title="$t('Обновление записи в реестре')">
    <template v-slot:additionalActions>
      <hreasy-table-export-to-excel-action :data=data></hreasy-table-export-to-excel-action>
    </template>
    <template v-slot:createFormFields>
      <junior-add-update-form-fields :create-or-update-body="data.createBody" mode="add"></junior-add-update-form-fields>
    </template>
    <template v-slot:updateFormFields>
      <junior-add-update-form-fields :create-or-update-body="data.updateBody" mode="update"></junior-add-update-form-fields>
    </template>
  </hreasy-table>
</template>

<script lang="ts">
import Component from "vue-class-component";
import {Vue} from "vue-property-decorator";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import logger from "@/logger";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import HreasyTableCreateForm from "@/components/shared/table/HreasyTableCreateForm.vue";
import {DateTimeUtils} from "@/components/datetimeutils";
import HreasyTableDeleteConfimration from "@/components/shared/table/HreasyTableDeleteConfimration.vue";
import {DataTableHeader} from "vuetify";
import {JuniorRegistryDataContainer} from "@/components/udr/junior_registry.data.container";
import JuniorAddUpdateFormFields from "@/components/udr/JuniorAddUpdateFormFields.vue";
import HreasyTable from "@/components/shared/table/HreasyTable.vue";
import HreasyTableExportToExcelAction from "@/components/shared/table/HreasyTableExportToExcelAction.vue";


@Component({
  components: {
    HreasyTableExportToExcelAction,
    HreasyTable,
    JuniorAddUpdateFormFields,
    HreasyTableDeleteConfimration,
    MyDateFormComponent,
    HreasyTableCreateForm
  }
})
export default class JuniorRegistry extends Vue {
  private data = new JuniorRegistryDataContainer(() => {
    const headers: DataTableHeader[] = [
      {text: this.$tc('Молодой специалист'), value: 'juniorEmpl.name'}
    ];
    return headers;
  });

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Junior registry component created');
    this.data.init();
  }

  formatDateTime = (v: string | undefined) => DateTimeUtils.formatDateTimeFromIso(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => v ? Number(v).toLocaleString() : '';

  fromPeriodId = (v: number | null | undefined) => v && !isNaN(v) ? ReportPeriod.fromPeriodId(v) : null;
}
</script>

