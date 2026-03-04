<template>
  <span>
        <v-text-field
            :disabled="itemReadonly()"
            v-model="formData.increaseText"
            counter="256"
            :rules="[v=> v && v.length <= 128 || $t('Обязательное поле. Не более N символов', {n:128})]"
            :label="$t('Сообщение об изменениях')">
        </v-text-field>
  </span>
</template>

<script lang="ts">
import {Prop, Vue} from "vue-property-decorator";
import Component from "vue-class-component";

export interface SalaryRequestUpdateImplTextFormData {
  increaseText: string|null
}


const namespace_dict = 'dict';
@Component({
  components: {}
})
export default class SalaryRequestUpdateImplTextFormFields extends Vue {

  @Prop({required: true})
  private formData!: SalaryRequestUpdateImplTextFormData;


  private itemReadonly() {
    return !this.formData;
  }


}

</script>

<style scoped>

</style>
