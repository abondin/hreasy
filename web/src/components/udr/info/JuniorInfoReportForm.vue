<!-- Detailed information for selected junior-->
<template>
  <span v-if="formData">
       <v-autocomplete
           v-model="formData.progress"
           :items="progressTypes"
           :label="$t('Динамика роста')"
           :rules="[v => !!v || $t('Обязательное поле')]"
       ></v-autocomplete>
        <span class="subtitle-2">{{$t('Текущий уровень сотрудника')}}</span>

        <junior-info-report-form-ratings v-if="formData.ratings" :ratings="formData.ratings"
                                         :prev-ratings="prevReport?.ratings">
        </junior-info-report-form-ratings>
        <v-textarea
            v-model="formData.comment"
            :rules="[v=>(v && v.length <= 1024 || $t('Обязательное поле. Не более N символов', {n:1024}))]"
            counter="1024"
            :label="$t('Комментарий')">
        </v-textarea>
  </span>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {DateTimeUtils} from "@/components/datetimeutils";
import {Prop} from "vue-property-decorator";
import {AddOrUpdateJuniorReportBody, juniorProgressTypes, JuniorReport} from "@/components/udr/udr.service";
import JuniorInfoReportFormRatings from "@/components/udr/info/JuniorInfoReportFormRatings.vue";


@Component({
  components: {JuniorInfoReportFormRatings}
})
export default class JuniorInfoReportForm extends Vue {

  @Prop({required: true})
  formData!: AddOrUpdateJuniorReportBody;

  @Prop({required: false})
  prevReport?: JuniorReport;

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

}
</script>
