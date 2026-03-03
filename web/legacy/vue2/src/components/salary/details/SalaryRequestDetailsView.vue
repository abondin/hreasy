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
      <salary-request-details-view-info :data="data" @updated="fetchData()"/>
      <salary-request-details-view-implementation :data="data" @updated="fetchData()"/>
      <salary-request-details-view-approvals :data="data" @updated="fetchData()"/>
      <salary-request-employee-history :data="data"
                                       v-if="isEmployeeHistoryAccessible()"
                                       @updated="fetchData()"></salary-request-employee-history>
    </div>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {Prop, Watch} from "vue-property-decorator";
import {SalaryDetailsDataContainer} from "@/components/salary/details/salary-details.data.container";
import {errorUtils} from "@/components/errors";
import salaryService from "@/components/salary/salary.service";
import SalaryRequestDetailsViewInfo from "@/components/salary/details/info/SalaryRequestDetailsViewInfo.vue";
import SalaryRequestDetailsViewImplementation
  from "@/components/salary/details/impl/SalaryRequestDetailsViewImplementation.vue";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import SalaryRequestDetailsViewApprovals
  from "@/components/salary/details/approval/SalaryRequestDetailsViewApprovals.vue";
import logger from "@/logger";
import SalaryRequestEmployeeHistory from "@/components/salary/details/info/SalaryRequestEmployeeHistory.vue";
import permissionService from "@/store/modules/permission.service";
import {Route} from "vue-router";


@Component({
  components: {
    SalaryRequestEmployeeHistory,
    SalaryRequestDetailsViewApprovals, SalaryRequestDetailsViewImplementation, SalaryRequestDetailsViewInfo
  }
})
export default class SalaryRequestDetailsView extends Vue {


  private fetchLoading = false;
  private fetchError: string | null = null;

  // Only string passes from URL
  @Prop({required: true})
  period!: string;

  // Only string passes from URL
  @Prop({required: true})
  requestId!: string;


  data: SalaryDetailsDataContainer | null = null;

  /**
   * Lifecycle hook
   */
  created() {
    return this.fetchData();
  }

  @Watch('$route', {immediate: true, deep: true})
  onRouteChange(newRoute: Route) {
    if (this.data?.item) {
      const params = newRoute.params as { period: string; requestId: string };
      if (this.data.item.req.increaseStartPeriod.toString() != params.period
          || this.data.item.id.toString() != params.requestId) {
        logger.log(`Salary Request Details: Parameters changed. Reload data for ${params.period}:${params.requestId}`)
        this.fetchData();
      }
    }
  }


  private fetchData() {
    this.fetchLoading = true;
    this.fetchError = null;
    const periodNum = Number(this.period);
    return salaryService.getClosedSalaryRequestPeriods()
        .then(closedPeriods => {
          return salaryService.get(periodNum, Number(this.requestId))
              .then(data => {
                    this.data = new SalaryDetailsDataContainer({
                      period: ReportPeriod.fromPeriodId(periodNum),
                      closed: closedPeriods.map(p => p.period).includes(periodNum)
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

  private isEmployeeHistoryAccessible() {
    return permissionService.canAdminSalaryRequests();
  }

}
</script>

<style scoped lang="scss">
@import '~vuetify/src/styles/styles.sass';

</style>
