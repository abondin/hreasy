<!-- Junior Report Ratings to show on pages or edit in forms -->
<template>
  <span v-if="ratings">
    <rating-with-description
        v-for="prop in ['overallReadiness', 'competence', 'process', 'teamwork', 'contribution', 'motivation']"
        :key="prop"
        v-model="ratings[prop]" :prev="prevRatings ? prevRatings[prop]:null"
        :readonly="readonly"
        :name="$t(`JUNIOR_REPORT_RATING.${prop}.title`)">
      <template v-slot:description>
        <p>{{ $t(`JUNIOR_REPORT_RATING.${prop}.description`) }}</p>
        <ul>
          <li v-for="i in ['min','max']" :key="i"><strong>{{
              i
            }}</strong> — {{ $t(`JUNIOR_REPORT_RATING.${prop}.${i}`) }}</li>
        </ul>
      </template>
    </rating-with-description>
  </span>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {Prop} from "vue-property-decorator";
import {JuniorReportRatings} from "@/components/udr/udr.service";
import RatingWithDescription from "@/components/shared/RatingWithDescription.vue";


@Component({
  components: {RatingWithDescription}
})
export default class JuniorInfoReportFormRatings extends Vue {

  @Prop({required: true})
  ratings!: JuniorReportRatings;

  @Prop({required: false})
  prevRatings?: JuniorReportRatings | null;

  @Prop({required: false, default: false})
  private readonly!: boolean;

}
</script>
