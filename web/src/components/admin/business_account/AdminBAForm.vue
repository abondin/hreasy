<!-- Dialog to create or update business account -->
<template>
  <v-form ref="baEditForm">
    <v-card>
      <v-card-title v-if="emplForm.isNew">{{ $t('Создание бизнес акаунта') }}</v-card-title>
      <v-card-title v-else>{{ $t('Изменение бизнес акаунта') }}</v-card-title>
      <v-card-text>
        <!-- name -->
        <v-text-field
            v-model="emplForm.name"
            :counter="255"
            :rules="[v=>(v && v.length <= 255 || $t('Обязательное поле. Не более N символов', {n:255}))]"
            :label="$t('Наименование')"
            required>
          >
        </v-text-field>

        <v-autocomplete
            v-model="emplForm.responsibleEmployee"
            :items="allEmployees"
            item-value="id"
            item-text="displayName"
            :label="$t('Ответственный сотрудник')"
        ></v-autocomplete>

        <!-- description -->
        <v-textarea
            v-model="emplForm.description"
            :counter="1024"
            :rules="[v=>(!v || v.length <= 1024 || $t('Не более N символов', {n:1024}))]"
            :label="$t('Описание')"
            required>
          >
        </v-textarea>

        <v-select
            v-model="emplForm.archived"
            :label="$t('Архив')"
            :items="[{value:false, text:'Нет'}, {value:true, text:'Да'}]">
        </v-select>

        <!-- Error block -->
        <v-alert v-if="error" type="error">
          {{ error }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="closeDialog()">{{ $t('Закрыть') }}</v-btn>
        <v-btn @click="submit()" color="primary">{{ baForm.isNew ? $t('Создать') : $t('Изменить') }}</v-btn>
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
  BusinessAccount,
  CreateOrUpdateBusinessAccount
} from "@/components/admin/business_account/admin.ba.service";
import {Employee} from "@/components/empl/employee.service";


class BaForm {
  public isNew = true;
  public id?: number;
  public name = '';
  public description = '';
  public responsibleEmployee: number|null = null;
  public archived=false;
}

@Component(
    {components: {MyDateFormComponent}}
)

export default class AdminBAForm extends Vue {
  loading: boolean = false;

  @Prop({required: false})
  private input: BusinessAccount | undefined;

  @Prop({required: true})
  private allEmployees!: Employee[];

  private baForm = new BaForm();

  private error: String | null = null;


  @Watch("input")
  private watch() {
    this.reset();
  }

  private created() {
    this.reset();
  }

  private reset() {
    this.baForm.isNew = true;
    this.baForm.id = undefined;
    this.baForm.name = '';
    this.baForm.description = '';
    this.baForm.responsibleEmployee = null;
    this.baForm.archived = false;

    if (this.input) {
      this.baForm.isNew = false;
      this.baForm.id = this.input.id;
      this.baForm.name = this.input.name ? this.input.name : '';
      this.baForm.description = this.input.description ? this.input.description : '';
      this.baForm.responsibleEmployee = this.input.responsibleEmployee ? this.input.responsibleEmployee.id : null;
      this.baForm.archived = this.input.archived;
    }
  }

  private closeDialog() {
    this.reset();
    this.$nextTick(function () {
      this.$emit('close');
    })
  }

  private submit() {
    const form: any = this.$refs.baEditForm;
    if (form.validate()) {
      const body = {
        name: this.baForm.name,
        description: this.baForm.description,
        responsibleEmployee: this.baForm.responsibleEmployee,
        archived: this.baForm.archived
      } as CreateOrUpdateBusinessAccount;
      let serverRequest;
      if (this.baForm.isNew) {
        logger.log(`Create business account ${JSON.stringify(this.baForm)}`);
        serverRequest = adminBaService.create(body)
      } else {
        serverRequest = adminBaService.update(this.baForm.id!, body);
      }
      return serverRequest
          .then((result) => {
            logger.log(`Business Account updated/created: ${result}`);
            this.closeDialog();
          })
          .catch(error => {
            this.error = errorUtils.shortMessage(error);
          });
    }
  }

}
</script>
