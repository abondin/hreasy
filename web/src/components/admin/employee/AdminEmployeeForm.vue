<!-- Dialog to create or update employee -->
<template>
  <v-form ref="employeeEditForm">
    <v-card>
      <v-card-title v-if="employeeFrom.isNew">{{ $t('Создание карточки сотрудника') }}</v-card-title>
      <v-card-title v-else>{{ $t('Изменение карточки сотрудника') }}</v-card-title>
      <v-card-text>
        <!-- email -->
        <v-text-field
            v-model="employeeFrom.email"
            :counter="255"
            :rules="[v=>(v && v.length <= 255 || $t('Обязательное поле. Не более N символов', {n:255}))]"
            :label="$t('Email')"
            required>
          >
        </v-text-field>

        \
        <!-- Error block -->
        <v-alert v-if="error" type="error">
          {{ error }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="closeDialog()">{{ $t('Закрыть') }}</v-btn>
        <v-btn @click="submit()" color="primary">{{ employeeFrom.isNew ? $t('Создать') : $t('Изменить') }}</v-btn>
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
import adminEmployeeService, {
  CreateOrUpdateEmployeeBody,
  EmployeeWithAllDetails
} from "@/components/admin/employee/admin.employee.service";


class employeeFrom {
  public isNew = true;
  public id?: number;
  public email = '';
  // TODO Add fields
}

@Component(
    {components: {MyDateFormComponent}}
)

export default class AdminEmployeeForm extends Vue {
  loading: boolean = false;

  @Prop({required: false})
  private input: EmployeeWithAllDetails | undefined;

  private employeeFrom = new employeeFrom();

  private error: String | null = null;


  @Watch("input")
  private watch() {
    this.reset();
  }

  private created() {
    this.reset();
  }

  private reset() {
    this.employeeFrom.isNew = true;
    this.employeeFrom.id = undefined;
    this.employeeFrom.email = '';
    // TODO Add fields


    if (this.input) {
      this.employeeFrom.isNew = false;
      this.employeeFrom.id = this.input.id;
      this.employeeFrom.email = this.input.email ? this.input.email : '';
    }
  }

  private closeDialog() {
    this.reset();
    this.$nextTick(function () {
      this.$emit('close');
    })
  }

  private submit() {
    const form: any = this.$refs.emplEditForm;
    if (form.validate()) {
      const body = {
        email: this.employeeFrom.email,
        // TODO Add fields
      } as CreateOrUpdateEmployeeBody;
      let serverRequest;
      if (this.employeeFrom.isNew) {
        logger.log(`Create employee ${JSON.stringify(this.employeeFrom)}`);
        serverRequest = adminEmployeeService.create(body)
      } else {
        serverRequest = adminEmployeeService.update(this.employeeFrom.id!, body);
      }
      return serverRequest
          .then((result) => {
            logger.log(`Business employee updated/created: ${result}`);
            this.closeDialog();
          })
          .catch(error => {
            this.error = errorUtils.shortMessage(error);
          });
    }
  }

}
</script>
