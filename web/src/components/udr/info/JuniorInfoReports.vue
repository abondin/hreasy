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

    <!-- Dialogs -->
    <in-dialog-form v-if="data.addOrUpdateReportDialogAction" size="lg" form-ref="graduateForm"
                    :data="data.addOrUpdateReportDialogAction"
                    :title="()=>data.item.juniorEmpl.name + ': ' +$t('Создать отчёт')"
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
            :rules="[v=>(!v || v.length <= 1024 || $t('Обязательное поле. Не более N символов', {n:1024}))]"
            :label="$t('Комментарий')">
        </v-textarea>
        <p v-else>{{ $t('Вы уверены, что хотите отменить решение об окончании обучения?') }}</p>
      </template>
    </in-dialog-form>
  </v-card>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {DateTimeUtils} from "@/components/datetimeutils";
import {Prop} from "vue-property-decorator";
import {juniorProgressTypes} from "@/components/udr/udr.service";
import {JuniorDetailDataContainer} from "@/components/udr/junior-details.data.container";
import InDialogForm from "@/components/shared/forms/InDialogForm.vue";


@Component({
  components: {InDialogForm}
})
export default class JuniorInfoReports extends Vue {

  @Prop({required: true})
  data!: JuniorDetailDataContainer;

  private progressTypes = juniorProgressTypes.map(v => {
    return {
      text: this.$t('JUNIOR_PROGRESS_TYPE.'+v),
      value: v
    }
  });

  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

  private formatDateTime(date: string): string | undefined {
    return DateTimeUtils.formatDateTimeFromIso(date);
  }

  doOnSubmit() {
    this.$emit('submit');
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
