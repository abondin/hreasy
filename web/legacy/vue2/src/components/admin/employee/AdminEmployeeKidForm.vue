<!-- Dialog to create or update employee kid -->
<template>
  <v-form ref="employeeKidEditForm">
    <v-card>
      <v-card-title>
        <span v-if="employeeKidForm.isNew">{{ $t('Создание карточки ребёнка сотрудника') }}</span>
        <span v-else>{{ $t('Изменение карточки ребёнка сотрудника') }}</span>
        <v-spacer></v-spacer>
        <v-btn icon @click="closeDialog()">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row>
            <v-col>
              <v-autocomplete v-model="employeeKidForm.parent"
                              :items="allEmployees"
                              item-value="id"
                              item-text="displayName"
                              :label="$t('Сотрудник')"
                              :rules="[v=>(v ? true:$t('Обязательное поле'))]"
                              :disabled="!employeeKidForm.isNew"
              ></v-autocomplete>
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-text-field v-model="employeeKidForm.displayName"
                            :counter="255"
                            :rules="[v=>(v && v.length <= 255 || $t('Обязательное поле. Не более N символов', {n:255}))]"
                            :label="$t('ФИО')"
                            required>
                >
              </v-text-field>
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <my-date-form-component v-model="employeeKidForm.birthday"
                                      :label="$t('Дата рождения')"
                                      :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГ'))]"
              ></my-date-form-component>
            </v-col>
          </v-row>
          <!--</editor-fold>-->


        </v-container>
        <!-- Error block -->
        <v-alert v-if="error" type="error">
          {{ error }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="closeDialog()">{{ $t('Закрыть') }}</v-btn>
        <v-btn @click="submit()" color="primary">{{ employeeKidForm.isNew ? $t('Создать') : $t('Изменить') }}</v-btn>
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
  CreateOrUpdateEmployeeKidBody,
  EmployeeKid
} from "@/components/admin/employee/admin.employee.service";
import {SimpleDict} from "@/store/modules/dict";
import {DateTimeUtils} from "@/components/datetimeutils";

class EmployeeKidForm {
  public isNew = true;
  public id: number | null = null;
  public parent: number | null = null;
  public displayName: string | null = null;
  public birthday: string | null = null;
}

@Component(
    {components: {MyDateFormComponent}}
)

export default class AdminEmployeeForm extends Vue {
  loading = false;

  @Prop({required: false})
  private input: EmployeeKid | undefined;

  @Prop({required: true})
  private allEmployees!: Array<SimpleDict>;
  private employeeKidForm = new EmployeeKidForm();

  private error: string | null = null;

  @Watch("input")
  private watch() {
    this.reset();
  }

  private created() {
    this.reset();
  }

  /**
   * Expected field counts: 30
   * @private
   */
  private reset() {
    this.employeeKidForm.isNew = true;
    this.employeeKidForm.id = null;
    this.employeeKidForm.displayName = null;
    this.employeeKidForm.birthday = null;
    this.employeeKidForm.parent = null;

    if (this.input) {
      this.employeeKidForm.isNew = false;
      this.employeeKidForm.id = this.input.id;
      this.employeeKidForm.displayName = this.input.displayName;
      this.employeeKidForm.birthday = this.input.birthday ? this.input.birthday : null;
      this.employeeKidForm.parent = this.input.parent.id;
    }
  }

  private closeDialog() {
    this.reset();
    this.$nextTick(function () {
      this.$emit('close');
    })
  }

  private submit() {
    const form: any = this.$refs.employeeKidEditForm;
    if (form.validate()) {
      const body = Object.assign({}, this.employeeKidForm) as CreateOrUpdateEmployeeKidBody;
      let serverRequest;
      if (this.employeeKidForm.isNew) {
        logger.log(`Create employee kid ${JSON.stringify(this.employeeKidForm)}`);
        serverRequest = adminEmployeeService.createKid(this.employeeKidForm.parent!, body)
      } else {
        serverRequest = adminEmployeeService.updateKid(this.employeeKidForm.parent!, this.employeeKidForm.id!, body);
      }
      return serverRequest
          .then((result) => {
            logger.log(`Employee kid updated/created: ${result}`);
            this.closeDialog();
          })
          .catch(error => {
            this.error = errorUtils.shortMessage(error);
          });
    }
  }

  private validateDate(formattedDate: string, allowEmpty = true): boolean {
    return DateTimeUtils.validateFormattedDate(formattedDate, allowEmpty);
  }

}
</script>
