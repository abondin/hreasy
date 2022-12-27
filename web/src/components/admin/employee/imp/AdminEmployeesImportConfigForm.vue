<!-- Employees short info table-->
<template>
  <v-card>
    <v-card-text>
      <v-form name="workflowConfigForm">
        <v-subheader>{{ $t('Общие настройки') }}</v-subheader>
        <v-container>
          <v-row>
            <v-col>
              <v-text-field
                  :label="$t('Порядковый номер листа в документе')"
                  v-model="config.sheetNumber"
                  :rules="[v=>(!v || v.length <= 3 || $t('Не более N символов', {n:3}))]"
                  dense>
              </v-text-field>
              <v-text-field
                  :label="$t('Порядковый номер первой строки с данными')"
                  v-model="config.tableStartRow"
                  :rules="[v=>(!v || v.length <= 3 || $t('Не более N символов', {n:3}))]"
                  dense>
              </v-text-field>
            </v-col>
          </v-row>
        </v-container>
        <v-subheader>{{ $t('Названия (анлгийские буквы) или порядковые номера столбцов') }}</v-subheader>
        <v-container>
          <v-row>
            <v-col cols="6" lg="4" v-for="(value, propertyName) in config.columns"
                   v-bind:key="propertyName">
              <v-text-field
                  :label="$t('IMPORT_CONFIG.'+propertyName)"
                  v-model="config.columns[propertyName]"
                  :rules="[v=>(!v || v.length <= 3 || $t('Не более N символов', {n:3}))]"
                  dense>
              </v-text-field>
            </v-col>
          </v-row>
        </v-container>
      </v-form>
    </v-card-text>
    <v-card-actions>
      <v-btn @click="$emit('back')">{{ $t('Выбрать другой файл') }}</v-btn>
      <v-btn @click="$emit('apply')">{{ $t('Применить настройки') }}</v-btn>
    </v-card-actions>
  </v-card>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {EmployeeImportConfig} from "@/components/admin/employee/imp/admin.employee.import.service";
import {Prop} from "vue-property-decorator";


@Component({
  components: {}
})
export default class AdminEmployeesImportConfigForm extends Vue {
  @Prop({required: true})
  config!: EmployeeImportConfig;
}
</script>

<style scoped>

</style>
