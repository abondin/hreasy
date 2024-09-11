<!--
Form to create or delete approve/decline or just comment
-->
<template>
  <div>
    <v-textarea
        autofocus
        tabindex="1"
        v-model="formData.comment"
        :rules="[v=> isCommentRequired() ?
            (v && v.length <= 4096 || $t('Обязательное поле. Не более N символов', {n:4096}))
            : (!v || v.length <= 4096 || $t('Не более N символов', {n:4096}))]"
        :label="$t('Комментарий')">
    </v-textarea>
  </div>
</template>

<script lang="ts">
import {Prop, Vue} from "vue-property-decorator";
import Component from "vue-class-component";
import {SalaryApprovalState, salaryApprovalStates} from "@/components/salary/salary.service";

export interface SalaryRequestApproveFormData {
  action: SalaryApprovalState;
  comment: string | null;
  approvalId: number | null;
}


const namespace_dict = 'dict';
@Component({
  components: {}
})
export default class SalaryRequestApprovalFormFields extends Vue {

  @Prop({required: true})
  private formData!: SalaryRequestApproveFormData;

  private approvalActionDict = salaryApprovalStates.map(v => {
    return {text: this.$tc(`SALARY_REQUEST_APPROVAL_ACTION.${v}`), value: v};
  });


  private isCommentRequired() {
    return Boolean(this.formData?.action != SalaryApprovalState.APPROVE);
  }

}

</script>

<style scoped>

</style>
