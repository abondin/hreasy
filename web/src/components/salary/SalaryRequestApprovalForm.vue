<!--
Form to create or delete approve/decline or just comment
-->
<template>
  <v-form ref="salaryApprovalForm" v-if="data.approveBody">
    <v-card>
      <v-card-title>{{ title() }}</v-card-title>

      <!--<editor-fold desc="Delete approval">-->
      <v-card-text v-if="data.approveBody.approvalId">
        {{ $t('Вы уверены, что хотите удалить согласование/комментарий?') }}
      </v-card-text>
      <!--</editor-fold> -->

      <!--<editor-fold desc="Approve">-->
      <v-card-text v-else>
        <!--<editor-fold desc="Fields">-->
        <v-select
            :disabled="itemReadonly()"
            v-model="data.approveBody.action"
            :label="$t('Действие')"
            :rules="[v => !!v || $t('Обязательное поле')]"
            :items="approvalActionDict">
        </v-select>
        <v-textarea
            autofocus
            tabindex="1"
            :disabled="itemReadonly()"
            v-model="data.approveBody.comment"
            :rules="[v=> isCommentRequired() ?
            (v && v.length <= 4096 || $t('Обязательное поле. Не более N символов', {n:4096}))
            : (!v || v.length <= 4096 || $t('Не более N символов', {n:4096}))]"
            :label="$t('Комментарий')">
        </v-textarea>
        <!--</editor-fold>-->
      </v-card-text>
      <!--</editor-fold> -->
      <!-- Error block -->
      <v-alert v-if="data.actionError" type="error">
        {{ data.actionError }}
      </v-alert>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-progress-circular class="mr-2" v-if="data.loading" indeterminate></v-progress-circular>
        <v-btn @click="data.closeApproveDialog()">{{ $t('Закрыть') }}</v-btn>
        <v-btn @click="submitForm" color="primary" :disabled="data.loading || itemReadonly()">{{
            data.approveBody.approvalId ? $t('Удалить') : $t('Добавить')
          }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-form>
</template>

<script lang="ts">
import {Prop, Vue} from "vue-property-decorator";
import Component from "vue-class-component";
import salaryService, {
  SalaryApprovalState,
  salaryApprovalStates,
  SalaryRequestApproval,
  SalaryRequestApproveBody,
  SalaryRequestCommentBody,
  SalaryRequestDeclineBody
} from "@/components/salary/salary.service";
import logger from "@/logger";
import SalaryRequestCard from "@/components/salary/SalaryRequestCard.vue";
import {SalaryRequestDataContainer} from "@/components/salary/salary.data.container";

export interface SalaryRequestApproveFormData {
  action: SalaryApprovalState;
  comment: string | null;
  approvalId: number | null;
}

export class SalaryRequestApproveAction {

  public doApprovalAction(requestId: number, formData: SalaryRequestApproveFormData) {
    if (formData.approvalId) {
      logger.log(`Delete approval/comment ${requestId}:${formData.approvalId}`);
      return salaryService.deleteApproval(requestId, formData.approvalId);
    }
    const body = {
      comment: formData.comment,
    };
    switch (formData.action) {
      case SalaryApprovalState.APPROVE:
        logger.log(`Approve salary request ${requestId}: ${body}`);
        return salaryService.approve(requestId, body as SalaryRequestApproveBody);
      case SalaryApprovalState.DECLINE:
        logger.log(`Decline salary request ${requestId}: ${body}`);
        return salaryService.decline(requestId, body as SalaryRequestDeclineBody);
      case SalaryApprovalState.COMMENT:
        logger.log(`Comment salary request ${requestId}: ${body}`);
        return salaryService.comment(requestId, body as SalaryRequestCommentBody);
      default:
        throw new Error(`Unsupported action ${formData.action}`);
    }
  }

  public itemToBody(item: SalaryRequestApproval | null, action?: SalaryApprovalState): SalaryRequestApproveFormData {
    if (item == null) {
      return {
        action: action||SalaryApprovalState.COMMENT,
        comment: null,
        approvalId: null
      } as SalaryRequestApproveFormData;
    } else {
      return {
        approvalId: item.id,
        action: item.state,
        comment: item.comment,
      } as SalaryRequestApproveFormData;
    }
  }
}

const namespace_dict = 'dict';
@Component({
  components: {SalaryRequestCard}
})
export default class SalaryRequestApprovalForm extends Vue {

  @Prop({required: true})
  private data!: SalaryRequestDataContainer;

  private approvalActionDict = salaryApprovalStates.map(v => {
    return {text: this.$tc(`SALARY_REQUEST_APPROVAL_ACTION.${v}`), value: v};
  });

  private submitForm() {
    const form: any = this.$refs.salaryApprovalForm;
    if (form.validate()) {
      return this.data.submitApproveForm();
    }
  }

  private itemReadonly() {
    return !this.data;
  }

  private title() {
    if (!this.data) {
      return '-';
    }
    return this.data.approveBody?.approvalId ? this.$tc('Удалить согласование/комментарий')
        : this.$tc('Добавить согласование/комментарий');
  }

  private isCommentRequired() {
    return Boolean(this.data.approveBody?.action != SalaryApprovalState.APPROVE);
  }


  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Salary approve form created');
  }


}

</script>

<style scoped>

</style>
