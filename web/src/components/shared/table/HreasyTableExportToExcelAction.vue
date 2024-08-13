<template>
  <v-col align-self="center" cols="auto" v-if="data.canExport()">
    <v-tooltip bottom>
      <template v-slot:activator="{ on: ton, attrs: tattrs}">
        <div v-bind="tattrs" v-on="ton" class="ma-0 at-0">
          <v-btn link :disabled="exportLoading" @click="doExport" icon>
            <v-icon>mdi-file-excel</v-icon>
          </v-btn>
        </div>
      </template>
      <p>{{ $t('Экспорт в Excel') }}</p>
    </v-tooltip>
    <v-snackbar
        :value="exportCompleted"
        timeout="5000"
    >
      {{ $t('Экспорт успешно завершён. Файл скачен.') }}
      <template v-slot:action="{ attrs }">
        <v-btn color="blue" icon v-bind="attrs" @click="exportCompleted=false">
          <v-icon>mdi-close-circle-outline</v-icon>
        </v-btn>
      </template>
    </v-snackbar>
    <v-snackbar
        :value="exportError"
        timeout="5000"
    >
      <span class="error--text">{{exportError}}</span>
      <template v-slot:action="{ attrs }">
        <v-btn color="blue" icon v-bind="attrs" @click="exportError=''">
          <v-icon>mdi-close-circle-outline</v-icon>
        </v-btn>
      </template>
    </v-snackbar>
  </v-col>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import {DataContainerWithExcelExportSupport} from "@/components/shared/table/DataContainerWithExcelExportSupport";
import {errorUtils} from "@/components/errors";

@Component
export default class HreasyTableExportToExcelAction<T extends DataContainerWithExcelExportSupport> extends Vue {
  @Prop({required: true})
  private data!: T;

  private exportLoading = false;
  private exportCompleted = false;
  private exportError = '';


  doExport() {
    this.exportError = '';
    this.exportLoading = true;
    this.exportCompleted = false;
    this.data.exportToExcel().then(() => {
      this.exportCompleted = true;
    }).catch(error => {
      this.exportError = errorUtils.shortMessage(error);
    }).finally(() => {
      this.exportLoading = false;
    })

  }

}
</script>

