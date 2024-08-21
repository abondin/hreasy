<!-- Detailed information for selected salary request or bonus-->
<template>
  <v-container>
    <v-skeleton-loader v-if="fetchLoading"></v-skeleton-loader>
    <v-card v-if="fetchError">
      <router-link to="/salaries/requests">{{ $t('Повышения и бонусы') }}</router-link>
      <v-card-text>
        <v-alert class="error">{{ fetchError }}</v-alert>
      </v-card-text>
    </v-card>
    <div v-else-if="data">
      <router-link to="/salaries/requests">{{ $t('Повышения и бонусы') }}</router-link>
      / {{ data.item.employee.name }}
      <salary-request-details-view-info :data="data"/>
      <salary-request-details-view-implementation :data="data" @updated="fetchData()"/>
      <salary-request-details-view-approvals :data="data" @updated="fetchData()"/>
    </div>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {Prop} from "vue-property-decorator";
import {SalaryDetailsDataContainer} from "@/components/salary/details/salary-details.data.container";
import {errorUtils} from "@/components/errors";
import salaryService from "@/components/salary/salary.service";
import SalaryRequestDetailsViewInfo from "@/components/salary/details/info/SalaryRequestDetailsViewInfo.vue";
import SalaryRequestDetailsViewImplementation
  from "@/components/salary/details/impl/SalaryRequestDetailsViewImplementation.vue";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import SalaryRequestDetailsViewApprovals from "@/components/salary/details/approval/SalaryRequestDetailsViewApprovals.vue";


@Component({
  components: {SalaryRequestDetailsViewApprovals, SalaryRequestDetailsViewImplementation, SalaryRequestDetailsViewInfo}
})
export default class SalaryRequestDetailsView extends Vue {


  private fetchLoading = false;
  private fetchError: string | null = null;

  @Prop({required: true})
  period!: number;

  @Prop({required: true})
  requestId!: number;

  data: SalaryDetailsDataContainer | null = null;

  /**
   * Lifecycle hook
   */
  created() {
    return this.fetchData();
  }


  private fetchData() {
    this.fetchLoading = true;
    this.fetchError = null;
    return salaryService.getClosedSalaryRequestPeriods()
        .then(closedPeriods => {
          return salaryService.get(this.period, this.requestId)
              .then(data => {
                    this.data = new SalaryDetailsDataContainer({
                      period: ReportPeriod.fromPeriodId(this.period),
                      closed: closedPeriods.map(c => c.period).includes(this.period)
                    }, data);
                    return this.data;
                  }
              );
        }).catch((error: any) => {
          this.fetchError = errorUtils.shortMessage(error);
        })
        .finally(() => {
          this.fetchLoading = false
        });
  }

  public navigateRequestsTable() {
    this.$router.push(`salaries/requests`);
  }


}
</script>

<style scoped lang="scss">
@import '~vuetify/src/styles/styles.sass';

</style>
