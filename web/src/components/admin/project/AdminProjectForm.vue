<!-- Dialog to create or update project -->
<template>
  <v-form ref="projectEditForm">
    <v-card>
      <v-card-title v-if="projectForm.isNew">{{ $t('Создание проекта') }}</v-card-title>
      <v-card-title v-else>{{ $t('Изменение проекта') }}</v-card-title>
      <v-card-text>
        <!-- department -->
        <v-select
            v-model="projectForm.departmentId"
            :items="allDepartments"
            item-text="name"
            item-value="id"
            :label="$t('Отдел')"
        ></v-select>

        <!-- business account -->
        <v-select
            v-model="projectForm.baId"
            :items="basWithCurrent"
            item-text="name"
            item-value="id"
            :label="$t('Бизнес аккаунт')"
        ></v-select>


        <!-- name -->
        <v-text-field
            v-model="projectForm.name"
            :counter="255"
            :rules="[v=>(v && v.length <= 255 || $t('Обязательное поле. Не более N символов', {n:255}))]"
            :label="$t('Наименование')"
            required>
          >
        </v-text-field>

        <!-- customer -->
        <v-text-field
            v-model="projectForm.customer"
            :counter="255"
            :rules="[v=>(v && v.length <= 255 || $t('Обязательное поле. Не более N символов', {n:255}))]"
            :label="$t('Заказчик')"
            required>
          >
        </v-text-field>

        <!-- start date -->
        <my-date-form-component
            ref="startDateRef"
            v-model="projectForm.startDate"
            :label="$t('Начало')"
            :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГ'))]"
        ></my-date-form-component>

        <!-- end date -->
        <my-date-form-component
            ref="endDateRef"
            v-model="projectForm.endDate"
            :label="$t('Окончание')"
            :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГ'))]"
        ></my-date-form-component>


        <!-- info -->
        <vue-editor
            id="project-info-editor"
            v-model="projectForm.info">
        </vue-editor>

        <!-- Error block -->
        <v-alert v-if="error" type="error">
          {{ error }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="closeDialog()">{{ $t('Закрыть') }}</v-btn>
        <v-btn @click="submit()" color="primary">{{ projectForm.isNew ? $t('Создать') : $t('Изменить') }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-form>
</template>


<script lang="ts">
import Vue from 'vue'
import adminProjectService, {
  CreateOrUpdateProject,
  ProjectCreatedEvent,
  ProjectFullInfo
} from "@/components/admin/project/admin.project.service";
import Component from "vue-class-component";
import {Prop} from "vue-property-decorator";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import logger from "@/logger";
import {errorUtils} from "@/components/errors";
import {SimpleDict} from "@/store/modules/dict";
import {DateTimeUtils} from "@/components/datetimeutils";
import {VueEditor} from "vue2-editor";


class ProjectForm {
  public isNew = true;
  public id?: number;
  public name = '';
  public startDate = '';
  public endDate = '';
  public customer = '';
  public departmentId?: number;
  public baId: number | null = null;
  public info?: string;
}

@Component(
    {components: {MyDateFormComponent, VueEditor}}
)

export default class AdminProjectForm extends Vue {
  loading: boolean = false;

  @Prop({required: false})
  private input: ProjectFullInfo | undefined;

  @Prop({required: true, type: Array})
  private allDepartments!: Array<SimpleDict>;

  @Prop({required: true, type: Array})
  private allBusinessAccounts!: Array<SimpleDict>;

  /**
   * If current BA is archived we should add it to select combobox all values
   * @private
   */
  private basWithCurrent: Array<SimpleDict> = [];


  private projectForm = new ProjectForm();

  private error: String | null = null;

  private created() {
    this.reset();
  }

  public reset() {
    this.basWithCurrent = [...this.allBusinessAccounts];
    if (this.input && this.input.businessAccount) {
      this.basWithCurrent.push(this.input.businessAccount);
    }
    this.projectForm.isNew = true;
    this.projectForm.name = '';
    this.projectForm.id = undefined;
    this.projectForm.customer = '';
    this.projectForm.departmentId = undefined;
    this.projectForm.startDate = '';
    this.projectForm.endDate = '';
    this.projectForm.baId = null;
    this.projectForm.info='';

    if (this.input) {
      this.projectForm.isNew = false;
      this.projectForm.name = this.input.name ? this.input.name : '';
      this.projectForm.id = this.input.id;
      this.projectForm.customer = this.input.customer ? this.input.customer : '';
      this.projectForm.departmentId = this.input.department ? this.input.department.id : undefined;
      this.projectForm.startDate = this.input.startDate ? this.input.startDate : '';
      this.projectForm.endDate = this.input.endDate ? this.input.endDate : '';
      this.projectForm.baId = this.input.businessAccount ? this.input.businessAccount.id : null;
      this.projectForm.info = this.input.info;
    }
    if (this.$refs.startDateRef && this.$refs.endDateRef) {
      (this.$refs.startDateRef as MyDateFormComponent).reset();
      (this.$refs.endDateRef as MyDateFormComponent).reset();
    }
  }

  private closeDialog() {
    this.$nextTick(function () {
      this.$emit('close');
    })
  }

  private submit() {
    const form: any = this.$refs.projectEditForm;
    if (form.validate()) {
      const body = {
        name: this.projectForm.name,
        startDate: this.projectForm.startDate,
        customer: this.projectForm.customer,
        endDate: this.projectForm.endDate,
        departmentId: this.projectForm.departmentId,
        baId: this.projectForm.baId,
        info: this.projectForm.info
      } as CreateOrUpdateProject;
      var serverRequest;
      if (this.projectForm.isNew) {
        logger.log(`Create project ${JSON.stringify(this.projectForm)}`);
        serverRequest = adminProjectService.create(body)
      } else {
        serverRequest = adminProjectService.update(this.projectForm.id!, body);
      }
      return serverRequest
          .then((result: number) => {
            logger.log(`Project updated/created: ${result}`);
            this.$emit('close', new ProjectCreatedEvent(result));
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
