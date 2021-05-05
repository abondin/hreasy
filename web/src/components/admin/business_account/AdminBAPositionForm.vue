<!-- Dialog to create or update business account position -->
<template>
  <v-form ref="baPositionEditForm">
    <v-card>
      <v-card-title v-if="baPositionForm.isNew">{{ $t('Создание позиции бизнес акаунта') }}</v-card-title>
      <v-card-title v-else>{{ $t('Изменение позиции бизнес акаунта') }}</v-card-title>
      <v-card-text>
        <!-- name -->
        <v-text-field
            v-model="baPositionForm.name"
            :counter="255"
            :rules="[v=>(v && v.length <= 255 || $t('Обязательное поле. Не более N символов', {n:255}))]"
            :label="$t('Наименование')"
            required>
          >
        </v-text-field>

        <!-- rate -->
        <v-text-field
            type="number"
            v-model="baPositionForm.rate"
            :rules="[v=>(v && (v>0||v<0)|| $t('Обязательное поле'))]"
            :label="$t('Ставка (с НДС)')"
            required>
          >
        </v-text-field>

        <!-- description -->
        <v-textarea
            v-model="baPositionForm.description"
            :counter="1024"
            :rules="[v=>(!v || v.length <= 1024 || $t('Обязательное поле. Не более N символов', {n:1024}))]"
            :label="$t('Описание')"
            required>
          >
        </v-textarea>

        <!-- Error block -->
        <v-alert v-if="error" type="error">
          {{ error }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="closeDialog()">{{ $t('Закрыть') }}</v-btn>
        <v-btn @click="submit()" color="primary">{{ baPositionForm.isNew ? $t('Создать') : $t('Изменить') }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-form>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import {Prop, Watch} from "vue-property-decorator";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import logger from "@/logger";
import {errorUtils} from "@/components/errors";
import adminBaService, {
  BusinessAccount, BusinessAccountPosition, CreateOrUpdateBAPosition,
  CreateOrUpdateBusinessAccount
} from "@/components/admin/business_account/admin.ba.service";
import {Employee} from "@/components/empl/employee.service";


class BaPositionForm {
  public isNew = true;
  public id?: number;
  public name = '';
  public rate: number=0;
  public description = '';
}

@Component(
    {components: {MyDateFormComponent}}
)

export default class AdminBaPositionForm extends Vue {
  loading: boolean = false;

  @Prop({required: true})
  private businessAccountId!: number;

  @Prop({required: false})
  private input: BusinessAccountPosition | undefined;

  private baPositionForm = new BaPositionForm();

  private error: String | null = null;


  @Watch("input")
  private watch() {
    this.reset();
  }

  private created() {
    this.reset();
  }

  private reset() {
    this.baPositionForm.isNew = true;
    this.baPositionForm.id = undefined;
    this.baPositionForm.name = '';
    this.baPositionForm.description = '';

    if (this.input) {
      this.baPositionForm.isNew = false;
      this.baPositionForm.id = this.input.id;
      this.baPositionForm.name = this.input.name ? this.input.name : '';
      this.baPositionForm.description = this.input.description ? this.input.description : '';
      this.baPositionForm.rate = this.input.rate;
    }
  }

  private closeDialog() {
    this.reset();
    this.$nextTick(function () {
      this.$emit('close');
    })
  }

  private submit() {
    const form: any = this.$refs.baPositionEditForm;
    if (form.validate()) {
      const body = {
        name: this.baPositionForm.name,
        description: this.baPositionForm.description,
        rate: this.baPositionForm.rate,
      } as CreateOrUpdateBAPosition;
      var serverRequest;
      if (this.baPositionForm.isNew) {
        logger.log(`Create business account position ${JSON.stringify(this.baPositionForm)}`);
        serverRequest = adminBaService.createPosition(this.businessAccountId, body)
      } else {
        serverRequest = adminBaService.updatePosition(this.businessAccountId, this.baPositionForm.id!, body);
      }
      return serverRequest
          .then((result) => {
            logger.log(`Business Account updated/created: ${result}`);
            this.$emit('close');
          })
          .catch(error => {
            this.error = errorUtils.shortMessage(error);
          });
    }
  }

}
</script>
