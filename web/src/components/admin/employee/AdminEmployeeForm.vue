<!-- Dialog to create or update employee -->
<template>
  <v-form ref="employeeEditForm">
    <v-card>
      <v-card-title>
        <span v-if="employeeForm.isNew">{{ $t('Создание карточки сотрудника') }}</span>
        <span v-else>{{ $t('Изменение карточки сотрудника') }}</span>
        <v-spacer></v-spacer>
        <v-btn icon @click="closeDialog()">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row>
            <!--<editor-fold desc="1 row (display name)">-->
            <v-col cols=12>
              <v-text-field v-model="employeeForm.displayName"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('ФИО')">
                >
              </v-text-field>
            </v-col>
            <!--</editor-fold>-->
            <!--<editor-fold desc="2 row (email, phone, skype,telegram)">-->
            <v-col cols=4>
              <v-text-field v-model="employeeForm.email"
                            :counter="255"
                            :rules="[v=>(v && v.length <= 255 || $t('Обязательное поле. Не более N символов', {n:255}))]"
                            :label="$t('Email')"
                            required>
                >
              </v-text-field>
            </v-col>
            <v-col cols=4>
              <v-text-field v-model="employeeForm.phone"
                            :counter="12"
                            :rules="[v=>(!v || v.length <= 12 || $t('Не более N символов', 12))]"
                            :label="$t('Телефон')"
                            hint="Формат +71112223344">
                >
              </v-text-field>
            </v-col>
            <v-col cols=2>
              <v-text-field v-model="employeeForm.skype"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', 255))]"
                            :label="$t('Skype')">
                >
              </v-text-field>
            </v-col>
            <v-col cols=2>
              <v-text-field v-model="employeeForm.telegram"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', 255))]"
                            :label="$t('Telegram')">
                >
              </v-text-field>
            </v-col>
            <!--</editor-fold>-->
            <!--<editor-fold desc="3 row (departmentId, currentProjectId, currentProjectRole)">-->
            <v-col cols=4>
              <v-autocomplete v-model="employeeForm.departmentId"
                              :items="allDepartmentsWithCurrent"
                              item-value="id"
                              item-text="name"
                              :label="$t('Подразделение')"
              ></v-autocomplete>
            </v-col>

            <v-col cols=4>
              <v-autocomplete v-model="employeeForm.currentProjectId"
                              :items="allProjectsWithCurrent"
                              item-value="id"
                              item-text="name"
                              :label="$t('Текущий проект')"
              ></v-autocomplete>
            </v-col>
            <v-col cols=4>
              <v-text-field v-model="employeeForm.currentProjectRole"
                            :counter="64"
                            :rules="[v=>(!v || v.length <= 64 || $t('Не более N символов', 64))]"
                            :label="$t('Роль на проекте')">
                >
              </v-text-field>
            </v-col>
            <!--</editor-fold>-->
            <!--<editor-fold desc="4 row (levelId, positionId, officeLocationId)">-->
            <v-col cols=4>
              <v-autocomplete v-model="employeeForm.levelId"
                              :items="allLevelsWithCurrent"
                              item-value="id"
                              item-text="name"
                              :label="$t('Уровень экспертизы')"
              ></v-autocomplete>
            </v-col>
            <v-col cols=4>
              <v-autocomplete v-model="employeeForm.positionId"
                              :items="allPositionsWithCurrent"
                              item-value="id"
                              item-text="name"
                              :label="$t('Позиция')"
              ></v-autocomplete>
            </v-col>
            <v-col cols=4>
              <v-autocomplete v-model="employeeForm.officeLocationId"
                              :items="allOfficeLocationsWithCurrent"
                              item-value="id"
                              item-text="name"
                              :label="$t('Рабочее место')"
              ></v-autocomplete>
            </v-col>
            <!-- </editor-fold> -->
            <!-- <editor-fold desc="5 row (dateOfEmployment, dateOfDismissal, workType, workDay, birthday, sex)"-->
            <v-col cols=2>
              <my-date-form-component v-model="employeeForm.dateOfEmployment"
                                      :label="$t('Дата трудоустройства')"
                                      :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГ'))]"
              ></my-date-form-component>
            </v-col>
            <v-col cols=2>
              <my-date-form-component v-model="employeeForm.dateOfDismissal"
                                      :label="$t('Дата увольнения')"
                                      :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГ'))]"
              ></my-date-form-component>
            </v-col>
            <v-col cols=2>
              <v-text-field v-model="employeeForm.workType"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Место работы')">
              </v-text-field>
            </v-col>
            <v-col cols=2>
              <v-text-field v-model="employeeForm.workDay"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Рабочий день (Полный/Неполный)')">
              </v-text-field>
            </v-col>

            <v-col cols=2>
              <my-date-form-component v-model="employeeForm.birthday"
                                      :label="$t('День рождения')"
                                      :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГ'))]"
              ></my-date-form-component>
            </v-col>
            <v-col cols=2>
              <v-text-field v-model="employeeForm.sex"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Пол')">
              </v-text-field>
            </v-col>
            <!--</editor-fold>-->
            <!--<editor-fold desc="6 row (documentSeries, documentNumber, documentIssuedDate, documentIssuedBy)">-->
            <v-col cols="2">
              <v-text-field v-model="employeeForm.documentSeries"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Серия документа')">
              </v-text-field>
            </v-col>
            <v-col cols="2">
              <v-text-field v-model="employeeForm.documentNumber"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Номер документа')">
              </v-text-field>
            </v-col>
            <v-col cols="2">
              <my-date-form-component v-model="employeeForm.documentIssuedDate"
                                      :label="$t('Документ выдан (когда)')"
                                      :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГ'))]"
              ></my-date-form-component>
            </v-col>
            <v-col cols="6">
              <v-text-field v-model="employeeForm.documentIssuedBy"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Документ выдан (кем)')">
                >
              </v-text-field>
            </v-col>
            <!--</editor-fold>-->
            <!--<editor-fold desc="7 row (registrationAddress, cityOfResidence, foreignPassport, englishLevel)">-->
            <v-col cols="6">
              <v-text-field v-model="employeeForm.registrationAddress"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Адрес по регистрации')">
              </v-text-field>
            </v-col>
            <v-col cols=2>
              <v-text-field v-model="employeeForm.cityOfResidence"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Город проживания')">
              </v-text-field>
            </v-col>
            <v-col cols=2>
              <v-text-field v-model="employeeForm.foreignPassport"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Загранпаспорт')">
              </v-text-field>
            </v-col>
            <v-col cols=2>
              <v-text-field v-model="employeeForm.englishLevel"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Уровень английского')">
              </v-text-field>
            </v-col>
            <!-- </editor-fold> -->
            <!--<editor-fold desc="8 row (familyStatus, spouseName, children)">-->
            <v-col cols=2>
              <v-text-field v-model="employeeForm.familyStatus"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Семейный статус')">
              </v-text-field>
            </v-col>
            <v-col cols=4>
              <v-text-field v-model="employeeForm.spouseName"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('ФИО супруга/супруги')">
              </v-text-field>
            </v-col>
            <v-col cols=6>
              <v-text-field v-model="employeeForm.children"
                            disabled
                            :counter="1024"
                            :rules="[v=>(!v || v.length <= 1024 || $t('Не более N символов', {n:1024}))]"
                            :label="$t('Дети')">
              </v-text-field>
            </v-col>
            <!--</editor-fold>-->
          </v-row>


        </v-container>
        <!-- Error block -->
        <v-alert v-if="error" type="error">
          {{ error }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="closeDialog()">{{ $t('Закрыть') }}</v-btn>
        <v-btn @click="submit()" color="primary">{{ employeeForm.isNew ? $t('Создать') : $t('Изменить') }}</v-btn>
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
import {SimpleDict} from "@/store/modules/dict";
import {DateTimeUtils} from "@/components/datetimeutils";

/**
 * 33 fields from EmployeeWithAllDetails
 *  + isNew
 *  - displayName
 *  - documentFull
 *  - active
 *  - skills
 * ------------------
 * Expected field counts: 29
 */
class employeeForm {
  public isNew = true;
  public id: number | null = null;
  currentProjectId: number | null = null;
  currentProjectRole: string | null = null;
  displayName: string | null = null;
  departmentId: number | null = null;
  birthday: string | null = null;
  sex: string | null = null;
  email: string | null = null;
  phone: string | null = null;
  skype: string | null = null;
  telegram: string | null = null;
  dateOfEmployment: string | null = null;
  levelId: number | null = null;
  workType: string | null = null;
  workDay: string | null = null;
  registrationAddress: string | null = null;
  documentSeries: string | null = null;
  documentNumber: string | null = null;
  documentIssuedBy: string | null = null;
  documentIssuedDate: string | null = null;
  foreignPassport: string | null = null;
  cityOfResidence: string | null = null;
  englishLevel: string | null = null;
  familyStatus: string | null = null;
  spouseName: string | null = null;
  children: string | null = null;
  dateOfDismissal: string | null = null;
  positionId: number | null = null;
  officeLocationId: number | null = null;
}

@Component(
    {components: {MyDateFormComponent}}
)

export default class AdminEmployeeForm extends Vue {
  loading = false;

  @Prop({required: false})
  private input: EmployeeWithAllDetails | undefined;

  @Prop({required: true})
  private allDepartments!: Array<SimpleDict>;
  private allDepartmentsWithCurrent: Array<SimpleDict> = [];

  @Prop({required: true})
  private allProjects!: Array<SimpleDict>;
  private allProjectsWithCurrent: Array<SimpleDict> = [];

  @Prop({required: true})
  private allPositions!: Array<SimpleDict>;
  private allPositionsWithCurrent: Array<SimpleDict> = [];

  @Prop({required: true})
  private allLevels!: Array<SimpleDict>;
  private allLevelsWithCurrent: Array<SimpleDict> = [];

  @Prop({required: true})
  private allOfficeLocations!: Array<SimpleDict>;
  private allOfficeLocationsWithCurrent: Array<SimpleDict> = [];

  private employeeForm = new employeeForm();

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
    this.employeeForm.isNew = true;
    this.employeeForm.id = null;
    this.employeeForm.email = null;
    this.employeeForm.currentProjectId = null;
    this.employeeForm.currentProjectRole = null;
    this.employeeForm.skype = null;
    this.employeeForm.telegram = null;
    this.employeeForm.birthday = null;
    this.employeeForm.dateOfEmployment = null;
    this.employeeForm.children = null;
    this.employeeForm.cityOfResidence = null;
    this.employeeForm.departmentId = null;
    this.employeeForm.documentIssuedBy = null;
    this.employeeForm.documentIssuedDate = null;
    this.employeeForm.documentNumber = null;
    this.employeeForm.documentSeries = null;
    this.employeeForm.englishLevel = null;
    this.employeeForm.familyStatus = null;
    this.employeeForm.dateOfDismissal = null;
    this.employeeForm.phone = null;
    this.employeeForm.foreignPassport = null;
    this.employeeForm.displayName = null;
    this.employeeForm.levelId = null;
    this.employeeForm.officeLocationId = null;
    this.employeeForm.spouseName = null;
    this.employeeForm.sex = null;
    this.employeeForm.positionId = null;
    this.employeeForm.registrationAddress = null;
    this.employeeForm.workDay = this.$t('Полный') as string;
    this.employeeForm.workType = this.$t('Основной') as string;


    if (this.input) {
      this.employeeForm.isNew = false;
      this.employeeForm.id = this.input.id;
      this.employeeForm.email = this.input.email;
      this.employeeForm.displayName = this.input.displayName;
      this.employeeForm.currentProjectId = this.input.currentProjectId || null;
      this.employeeForm.currentProjectRole = this.input.currentProjectRole || null;
      this.employeeForm.skype = this.input.skype;
      this.employeeForm.telegram = this.input.telegram;
      this.employeeForm.birthday = this.input.birthday;
      this.employeeForm.dateOfEmployment = this.input.dateOfEmployment || null;
      this.employeeForm.children = this.input.children || null;
      this.employeeForm.cityOfResidence = this.input.cityOfResidence || null;
      this.employeeForm.departmentId = this.input.departmentId || null;
      this.employeeForm.documentIssuedBy = this.input.documentIssuedBy || null;
      this.employeeForm.documentIssuedDate = this.input.documentIssuedDate || null;
      this.employeeForm.documentNumber = this.input.documentNumber || null;
      this.employeeForm.documentSeries = this.input.documentSeries || null;
      this.employeeForm.englishLevel = this.input.englishLevel || null;
      this.employeeForm.familyStatus = this.input.familyStatus || null;
      this.employeeForm.dateOfDismissal = this.input.dateOfDismissal || null;
      this.employeeForm.phone = this.input.phone || null;
      this.employeeForm.foreignPassport = this.input.foreignPassport || null;
      this.employeeForm.levelId = this.input.levelId || null;
      this.employeeForm.officeLocationId = this.input.officeLocationId || null;
      this.employeeForm.spouseName = this.input.spouseName || null;
      this.employeeForm.sex = this.input.sex || null;
      this.employeeForm.positionId = this.input.positionId || null;
      this.employeeForm.registrationAddress = this.input.registrationAddress || null;
      this.employeeForm.workDay = this.input.workDay || null;
      this.employeeForm.workType = this.input.workType || null;
    }

    this.allDepartmentsWithCurrent = this.withCurrent(this.allDepartments, this.employeeForm.departmentId);
    this.allProjectsWithCurrent = this.withCurrent(this.allProjects, this.employeeForm.currentProjectId);
    this.allLevelsWithCurrent = this.withCurrent(this.allLevels, this.employeeForm.levelId);
    this.allOfficeLocationsWithCurrent = this.withCurrent(this.allOfficeLocations, this.employeeForm.officeLocationId);
    this.allPositionsWithCurrent = this.withCurrent(this.allPositions, this.employeeForm.positionId);

  }

  private closeDialog() {
    this.reset();
    this.$nextTick(function () {
      this.$emit('close');
    })
  }

  private submit() {
    const form: any = this.$refs.employeeEditForm;
    if (form.validate()) {
      const body = Object.assign({}, this.employeeForm) as CreateOrUpdateEmployeeBody;
      let serverRequest;
      if (this.employeeForm.isNew) {
        logger.log(`Create employee ${JSON.stringify(this.employeeForm)}`);
        serverRequest = adminEmployeeService.create(body)
      } else {
        serverRequest = adminEmployeeService.update(this.employeeForm.id!, body);
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

  private validateDate(formattedDate: string, allowEmpty = true): boolean {
    return DateTimeUtils.validateFormattedDate(formattedDate, allowEmpty);
  }

  private withCurrent(items: Array<SimpleDict>, currentValueId: number | null) {
    return items ? (items.filter(i => i.active || i.id == currentValueId)) : [];
  }

}
</script>
