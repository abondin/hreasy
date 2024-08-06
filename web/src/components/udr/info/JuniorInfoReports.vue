<!-- Detailed information for selected junior-->
<template>
  <v-card class="mt-5" v-if="data">
    <v-card-title>
      <span>{{ $t('Отчёты') }}</span>
      <v-spacer></v-spacer>
      <v-tooltip bottom>
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <v-btn @click="data.openAddReportDialog()"
                 v-bind="tattrs" v-on="ton" class="col-auto" color="primary"
                 :disabled="false" icon>
            <v-icon>mdi-plus</v-icon>
          </v-btn>
        </template>
        <span>{{ $t('Добавить отчёт') }}</span>
      </v-tooltip>
    </v-card-title>
    <!-- Reports -->
    <span v-if="data.item.reports.length">
      <v-card outlined v-for="(report) in data.item.reports" v-bind:key="report.id">
        <v-container>
          <v-row dense>
            <v-col cols="">{{ report.createdBy.name + ' (' + formatDateTime(report.createdAt) + ')' }}</v-col>
            <v-col cols="auto" align-self="end">
              <v-tooltip bottom>
                <template v-slot:activator="{ on: ton, attrs: tattrs}">
                  <v-btn v-bind="tattrs" v-on="ton" text icon @click="data.openDeleteReportDialog(report.id)">
                    <v-icon>mdi-delete</v-icon>
                  </v-btn>
                </template>
                {{ $t('Удалить') }}
              </v-tooltip>
            </v-col>
          </v-row>
          <v-row dense>
            <v-col cols="auto">
              <v-tooltip bottom>
                <template v-slot:activator="{ on: ton, attrs: tattrs}">
                  <v-icon :set="icon=getIcon(report.progress)" v-bind="tattrs" v-on="ton" x-large :color="icon.color">{{ icon.icon }}</v-icon>
                </template>
                {{ $t('JUNIOR_PROGRESS_TYPE.' + report.progress) }}
              </v-tooltip>
            </v-col>
            <v-col class="align-content-center">{{ report.comment }}</v-col>
          </v-row>
        </v-container>
      </v-card>
    </span>
    <v-card-text v-else class="text-center">
      {{ $t('Оставьте ваш первый отчёт о работе сотрудника!') }}
    </v-card-text>


    <!-- Dialogs -->
    <in-dialog-form v-if="data.addOrUpdateReportDialogAction" size="lg" form-ref="reportForm"
                    :data="data.addOrUpdateReportDialogAction"
                    :title="$t('Создать отчёт')"
                    :submit-button-text="$t('Создать')" v-on:submit="doOnSubmit">
      <template v-slot:fields>
        <v-autocomplete
            v-if="data.addOrUpdateReportDialogAction.formData"
            v-model="data.addOrUpdateReportDialogAction.formData.progress"
            :items="progressTypes"
            :label="$t('Прогресс')"
            :rules="[v => !!v || $t('Обязательное поле')]"
        ></v-autocomplete>
        <v-textarea
            v-if="data.addOrUpdateReportDialogAction.formData"
            v-model="data.addOrUpdateReportDialogAction.formData.comment"
            :rules="[v=>(v && v.length <= 1024 || $t('Обязательное поле. Не более N символов', {n:1024}))]"
            counter="1024"
            :label="$t('Комментарий')">
        </v-textarea>
        <p v-else>{{ $t('Вы уверены, что хотите отменить решение об окончании обучения?') }}</p>
      </template>
    </in-dialog-form>

    <in-dialog-form v-if="data.deleteReportDialogAction" size="md" form-ref="deleteReportForm"
                    :data="data.deleteReportDialogAction"
                    :title="$t('Удалить отчёт')"
                    :submit-button-text="$t('Удалить')" v-on:submit="doOnSubmit">
      <template v-slot:fields>
        <p>{{ $t('Вы уверены, что хотите удалить отчёт?') }}</p>
      </template>
    </in-dialog-form>

  </v-card>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {DateTimeUtils} from "@/components/datetimeutils";
import {Prop} from "vue-property-decorator";
import {juniorProgressTypes, JuniorReport} from "@/components/udr/udr.service";
import {JuniorDetailDataContainer} from "@/components/udr/junior-details.data.container";
import InDialogForm from "@/components/shared/forms/InDialogForm.vue";
import JuniorRegistryTable from "@/components/udr/JuniorRegistryTable.vue";
import permissionService from "@/store/modules/permission.service";


@Component({
  components: {InDialogForm}
})
export default class JuniorInfoReports extends Vue {

  @Prop({required: true})
  data!: JuniorDetailDataContainer;

  private progressTypes = juniorProgressTypes.map(v => {
    return {
      text: this.$t('JUNIOR_PROGRESS_TYPE.' + v),
      value: v
    }
  });

  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

  private formatDateTime(date: string): string | undefined {
    return DateTimeUtils.formatDateTimeFromIso(date);
  }

  getIcon = JuniorRegistryTable.getIcon;

  doOnSubmit() {
    this.$emit('submit');
  }

  canUpdateReport(report: JuniorReport) {
    return permissionService.canUpdateJuniorReport(report.createdBy.id);
  }

}
</script>

<style scoped lang="scss">
@import '~vuetify/src/styles/styles.sass';

.info-dl {
  display: grid;
  grid-template-columns: max-content auto;

  > dt {
    font-weight: bolder;
    min-width: 150px;
    max-width: 300px;
    grid-column-start: 1;
  }

  > dd {
    grid-column-start: 2;
    margin-left: 10px;
    max-width: 400px;
  }
}

.column-title {
  display: flex;
  justify-content: space-between;
}

.column-actions {
  display: flex;
  justify-content: start;
}

</style>
