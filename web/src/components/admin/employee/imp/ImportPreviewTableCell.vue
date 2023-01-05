<!-- One cell in import preview table-->
<template>
  <span>
    <!-- Error -->
    <v-tooltip right v-if="cell.error">
      <template v-slot:activator="{ on }">
        <span v-on="on" class="error--text">
          <v-icon class="error--text" x-small>mdi-alert</v-icon>
          {{ $t('Ошибка') }}
        </span>
      </template>
      {{ cell.error }}.
      <span v-if="cell.raw"><br>{{ $t('Необработанное значение', {value: cell.raw}) }}</span>
    </v-tooltip>
    <!-- Current value -->
    <span v-if="cell.currentValue && cell.updated" class="old">
      {{ formatValue(cell.currentValue) }}
    </span>
    <!-- Imported value -->
    <span v-if="cell.importedValue" :class="cell.updated?'new':'same'">
      {{ formatValue(cell.importedValue) }}
    </span>
  </span>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {ExcelRowDataProperty} from "@/components/admin/employee/imp/admin.employee.import.service";
import {Prop} from "vue-property-decorator";
import {DateTimeUtils} from "@/components/datetimeutils";
import {SimpleDict} from "@/store/modules/dict";


@Component({
  components: {}
})
export default class ImportPreviewTableCell<T> extends Vue {
  @Prop({required: true})
  cell!: ExcelRowDataProperty<T>;

  @Prop({required: true})
  format!: 'string' | 'date' | 'dict';

  private formatValue(value: T) {
    switch (this.format) {
      case "string":
        return value;
      case "date":
        return DateTimeUtils.formatFromIso(value as unknown as string);
      case "dict":
        return (value as unknown as SimpleDict)?.name;
    }
  }


}
</script>

<style lang="scss">
@import 'import.scss';

</style>
