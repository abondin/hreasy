<!--
Simple dialog to approve or decline overtime report

Emits:

1) 'submit' when approved or declined
2) 'close' when dialog closed

 -->

<template>
  <v-dialog
      max-width="800"
      v-model="dialog">
    <template v-slot:activator="{on, attrs}">
      <v-tooltip bottom>
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <div v-bind="tattrs" v-on="ton">
            <v-btn-toggle>
              <v-btn icon
                     :disabled="previousDecision && previousDecision.decision=='APPROVED' && !previousDecision.outdated"
                     @click="approveNoDialog()">
                <v-icon color="success">mdi-checkbox-marked-circle-outline</v-icon>
              </v-btn>
              <v-btn v-bind="attrs"
                     v-on="on" icon>
                <v-icon>mdi-dots-horizontal</v-icon>
              </v-btn>
            </v-btn-toggle>
          </div>
        </template>
        <span>{{ $t('Согласование овертаймов') }}</span>
      </v-tooltip>
    </template>
    <v-form :ref="`overtime-approve-${employeeId}-${period.periodId()}`">
      <v-card>
        <v-card-title>{{ $t('Согласование овертаймов') }}</v-card-title>
        <v-card-text>
          <v-textarea
              autofocus
              v-model="comment"
              :rules="[v=>(!v ||  v.length <= 1024 || $t('Не более N символов', {n:1024}))]"
              :label="$t('Комментарий')">
          </v-textarea>
          <!-- Error block -->
          <v-alert v-if="error" type="error">
            {{ error }}
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-btn @click="closeDialog">{{ $t('Закрыть') }}</v-btn>
          <v-spacer></v-spacer>
          <v-btn-toggle>
            <v-btn @click="approve()" color="success">{{ $t('Согласовать') }}</v-btn>
            <v-btn @click="decline()" color="error">{{ $t('Отклонить') }}</v-btn>
          </v-btn-toggle>
        </v-card-actions>
      </v-card>
    </v-form>
  </v-dialog>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {Prop, Watch} from "vue-property-decorator";
import overtimeService, {ApprovalDecision, ReportPeriod} from "@/components/overtimes/overtime.service";
import {errorUtils} from "@/components/errors";


@Component
export default class ApproveOvertimeReportDialog extends Vue {

  @Prop({required: true})
  employeeId!: number;

  @Prop({required: true})
  period!: ReportPeriod;
  /**
   * Id of previous approval decisions to overide
   */
  @Prop({required: false, default: null})
  previousDecision!: ApprovalDecision | null;


  private dialog = false;

  private error: String | null = null;
  private comment: String | null = null;

  @Watch("dialog")
  private watch() {
    if (this.dialog) {
      this.reset();
    }
  }

  private approve() {
    const form: any = this.$refs[`overtime-approve-${this.employeeId}-${this.period.periodId()}`];
    if (form.validate()) {
      return overtimeService.approve(this.employeeId, this.period.periodId(), this.comment,
          this.previousDecision ? this.previousDecision.id : null)
          .then((result) => {
            this.$emit('submit', 'APPROVED');
            this.closeDialog();
          }).catch(error => {
            this.error = errorUtils.shortMessage(error);
          });
    }
  }

  private decline() {
    const form: any = this.$refs[`overtime-approve-${this.employeeId}-${this.period.periodId()}`];
    if (form.validate()) {
      if (this.comment == null || this.comment.trim().length == 0) {
        this.error = this.$tc('Комментарий обязателен при отклонении');
        return;
      }
      return overtimeService.decline(this.employeeId, this.period.periodId(), this.comment!,
          this.previousDecision ? this.previousDecision.id : null)
          .then((result) => {
            this.$emit('submit', 'DECLINED');
            this.closeDialog();
          }).catch(error => {
            this.error = errorUtils.shortMessage(error);
          });
    }
  }

  private approveNoDialog() {
    return overtimeService.approve(this.employeeId, this.period.periodId(), null,
        this.previousDecision ? this.previousDecision.id : null)
        .then((result) => {
          this.$emit('submit', 'APPROVED');
          this.closeDialog();
        }).catch(error => {
          this.error = errorUtils.shortMessage(error);
          this.dialog = true;
        });
  }

  private closeDialog() {
    this.dialog = false;
  }

  private reset() {
    this.error = null;
    this.comment = null;
  }

}
</script>

<style scoped>

</style>
