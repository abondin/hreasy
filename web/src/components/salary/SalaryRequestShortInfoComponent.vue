<template>
  <v-card>
    <v-card-subtitle>
      {{
        $t('SALARY_REQUEST_TITLE', {
          type: $t('SALARY_REQUEST_TYPE.' + data.type).toLowerCase(),
          employee: data.employee.name,
          amount: formatMoney(data.req.increaseAmount),
          period: fromPeriodId(data.req.increaseStartPeriod)
        })
      }}
    </v-card-subtitle>
    <v-card-text>
      <div class="row">
        <div class="col-lg-3 col-6">
          <v-list>
            <v-list-item>
              <dl>
                <dt class="font-weight-bold">{{ $t('Запрос от') }}:</dt>
                <dd>{{ data.createdBy.name }} ({{ formatDateTime(data.createdAt) }})</dd>
                <dt class="font-weight-bold">{{ $t('Причина запроса') }}:</dt>
                <dd>{{ data.req.reason }}</dd>
                <dt class="font-weight-bold" v-if="data.req.comment">{{
                    $t('Дополнительный комментарий к запросу')
                  }}:
                </dt>
                <dd v-if="data.req.comment">{{ data.req.comment }}</dd>
              </dl>
            </v-list-item>
          </v-list>
        </div>
        <div class="col-lg-3 col-6">
          <v-list>
            <v-list-item>
              <dl>
                <dt class="font-weight-bold" v-if="data.budgetBusinessAccount">{{
                    $t('Бюджет из бизнес аккаунта')
                  }}:
                </dt>
                <dd v-if="data.budgetBusinessAccount">{{ data.budgetBusinessAccount.name }}</dd>

                <dt class="font-weight-bold" v-if="data.budgetExpectedFundingUntil">
                  {{ $t('Планируемая дата окончания финансирования') }}:
                </dt>
                <dd v-if="data.budgetExpectedFundingUntil">{{ formatDate(data.budgetExpectedFundingUntil) }}</dd>

                <dt class="font-weight-bold" v-if="data.assessment">{{ $t('Связанный ассессмент') }}:</dt>
                <dd v-if="data.assessment">
                  <router-link :to="{name:'AssessmentDetailedVue', params:{
                  employeeId: data.employee.id,
                  assessmentId: data.assessment.id
                }}" target="_blank">{{ data.assessment.name }}
                  </router-link>
                </dd>
              </dl>
            </v-list-item>
          </v-list>
        </div>
        <div class="col-lg-3 col-6" v-if="data.impl">
          <v-list>
            <v-list-item>
              <dl>
                <dt class="font-weight-bold">{{ $t('Решение') }}:</dt>
                <dd v-bind:class="{'error--text':isRejected()}">{{ $t('SALARY_REQUEST_STAT.' + data.impl.state) }}</dd>

                <dt class="font-weight-bold">{{ $t('Решение принято') }}:</dt>
                <dd>{{ data.impl.implementedBy.name }} ({{ formatDateTime(data.impl.implementedAt) }})</dd>

                <dt class="font-weight-bold" v-if="data.impl.increaseAmount">{{ $t('Зафиксированная сумма') }}:</dt>
                <dd v-if="data.impl.increaseAmount">{{ formatMoney(data.impl.increaseAmount) }}</dd>

                <dt class="font-weight-bold" v-if="data.impl.increaseStartPeriod">{{
                    $t('Зафиксированная период')
                  }}:
                </dt>
                <dd v-if="data.impl.increaseStartPeriod">{{ fromPeriodId(data.impl.increaseStartPeriod) }}</dd>

                <dt class="font-weight-bold" v-if="data.impl.newPosition">{{ $t('Позиция') }}:</dt>
                <dd v-if="data.impl.newPosition">{{ data.impl.newPosition.name }}</dd>
              </dl>
            </v-list-item>
          </v-list>
        </div>
        <div class="col-lg-3 col-6" v-if="data.impl">
          <v-list>
            <v-list-item>
              <dl>
                <dt class="font-weight-bold" v-if="data.impl.reason">{{ $t('Причина решения') }}:</dt>
                <dd v-if="data.impl.reason">{{ data.impl.reason }}</dd>

                <dt class="font-weight-bold" v-if="data.impl.comment">{{
                    $t('Дополнительный комментарий к решению')
                  }}:
                </dt>
                <dd v-if="data.impl.comment">{{ data.impl.comment }}</dd>
              </dl>
            </v-list-item>
          </v-list>
        </div>
      </div>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import {SalaryIncreaseRequest} from "@/components/salary/salary.service";
import {Prop, Vue} from "vue-property-decorator";
import Component from "vue-class-component";
import {DateTimeUtils} from "@/components/datetimeutils";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import {SalaryRequestImplementationState} from "@/components/admin/salary/admin.salary.service";

@Component
export default class SalaryRequestShortInfoComponent extends Vue {

  @Prop({required: true})
  private data!: SalaryIncreaseRequest;

  formatDateTime = (v: string | undefined) => DateTimeUtils.formatDateTimeFromIso(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => Number(v).toLocaleString();

  fromPeriodId = (v: number | null | undefined) => v && !isNaN(v) ? ReportPeriod.fromPeriodId(v) : null;

  private isRejected(): boolean {
    return this.data.impl?.state == SalaryRequestImplementationState.REJECTED;
  }
}
</script>

<style scoped>

</style>
