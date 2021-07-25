<!-- Dialog to create or update employee -->
<template>
  <v-form ref="employeeEditForm">
    <v-card>
      <v-card-title>
        <span v-if="employeeFrom.isNew">{{ $t('Создание карточки сотрудника') }}</span>
        <span v-else>{{ $t('Изменение карточки сотрудника') }}</span>
        <v-spacer></v-spacer>
        <v-btn icon @click="closeDialog()">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-card-title>
      <v-card-text>
        <v-container>
          <!--<editor-fold desc="First row (email, lastname, firstname, patronymicName)">-->
          <v-row>
            <v-col>
              <v-text-field v-model="employeeFrom.email"
                            :counter="255"
                            :rules="[v=>(v && v.length <= 255 || $t('Обязательное поле. Не более N символов', {n:255}))]"
                            :label="$t('Email')"
                            required>
                >
              </v-text-field>
            </v-col>
            <v-col>
              <v-text-field v-model="employeeFrom.lastname"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Фамилия')">
                >
              </v-text-field>
            </v-col>
            <v-col>
              <v-text-field v-model="employeeFrom.firstname"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Имя')">
                >
              </v-text-field>
            </v-col>
            <v-col>
              <v-text-field v-model="employeeFrom.patronymicName"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Отчество')">
                >
              </v-text-field>
            </v-col>
          </v-row>
          <!--</editor-fold>-->

          <!--<editor-fold desc="Second row (currentProjectId, phone, skype, dateOfEmployment)">-->
          <v-row>
            <v-col>
              <v-autocomplete v-model="employeeFrom.currentProjectId"
                              :items="allProjects"
                              item-value="id"
                              item-text="name"
                              :label="$t('Текущий проект')"
              ></v-autocomplete>
            </v-col>

            <v-col>
              <v-text-field v-model="employeeFrom.phone"
                            :counter="11"
                            :rules="[v=>(!v || /^\d{11}$/.test(v) || $t('Число 11 символов'))]"
                            :label="$t('Телефон')"
                            hint="Формат 81112223344">
                >
              </v-text-field>
            </v-col>
            <v-col>
              <v-text-field v-model="employeeFrom.skype"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', 255))]"
                            :label="$t('Skype')"
                            hint="Формат 81112223344">
                >
              </v-text-field>
            </v-col>
            <v-col>
              <my-date-form-component v-model="employeeFrom.dateOfEmployment"
                                      :label="$t('Дата трудоустройства')"
                                      :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГ'))]"
              ></my-date-form-component>
            </v-col>
          </v-row>
          <!--</editor-fold>-->

          <!--<editor-fold desc="Third row (birthday, positionId, levelId, officeLocationId)">-->
          <v-row>
            <v-col>
              <my-date-form-component v-model="employeeFrom.birthday"
                                      :label="$t('День рождения')"
                                      :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГ'))]"
              ></my-date-form-component>
            </v-col>

            <v-col>
              <v-autocomplete v-model="employeeFrom.positionId"
                              :items="allPositions"
                              item-value="id"
                              item-text="name"
                              :label="$t('Позиция')"
              ></v-autocomplete>
            </v-col>
            <v-col>
              <v-autocomplete v-model="employeeFrom.levelId"
                              :items="allLevels"
                              item-value="id"
                              item-text="name"
                              :label="$t('Уровень экспертизы')"
              ></v-autocomplete>
            </v-col>
            <v-col>
              <v-autocomplete v-model="employeeFrom.officeLocationId"
                              :items="allOfficeLocations"
                              item-value="id"
                              item-text="name"
                              :label="$t('Рабочее место')"
              ></v-autocomplete>
            </v-col>
          </v-row>
          <!--</editor-fold>-->

          <!--<editor-fold desc="fourth row (documentSeries, documentNumber, documentIssuedDate, documentIssuedBy)">-->
          <v-row>
            <v-col>
              <v-text-field v-model="employeeFrom.documentSeries"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Серия документа')">
              </v-text-field>
            </v-col>
            <v-col>
              <v-text-field v-model="employeeFrom.documentNumber"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Номер документа')">
              </v-text-field>
            </v-col>
            <v-col>
              <my-date-form-component v-model="employeeFrom.documentIssuedDate"
                                      :label="$t('Документ выдан (когда)')"
                                      :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГ'))]"
              ></my-date-form-component>
            </v-col>
            <v-col>
              <v-text-field v-model="employeeFrom.documentIssuedBy"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Документ выдан (кем)')">
                >
              </v-text-field>
            </v-col>
          </v-row>
          <!--</editor-fold>-->

          <!--<editor-fold desc="fifth row (registrationAddress, sex, spouseName, workDay)">-->
          <v-row>
            <v-col>
              <v-text-field v-model="employeeFrom.registrationAddress"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Адрес по регистрации')">
              </v-text-field>
            </v-col>
            <v-col>
              <v-text-field v-model="employeeFrom.sex"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Пол')">
              </v-text-field>
            </v-col>
            <v-col>
              <v-text-field v-model="employeeFrom.spouseName"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('ФИО супруга/супруги')">
              </v-text-field>
            </v-col>
            <v-col>
              <v-text-field v-model="employeeFrom.workDay"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Рабочий день (Полный/Неполный)')">
              </v-text-field>
            </v-col>
          </v-row>
          <!--</editor-fold>-->

          <!--<editor-fold desc="sixth row (workType, departmentId, cityOfResidence, children)">-->
          <v-row>
            <v-col>
              <v-text-field v-model="employeeFrom.workType"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Место работы')">
              </v-text-field>
            </v-col>
            <v-col>
              <v-autocomplete v-model="employeeFrom.departmentId"
                              :items="allDepartments"
                              item-value="id"
                              item-text="name"
                              :label="$t('Подразделение')"
              ></v-autocomplete>
            </v-col>
            <v-col>
              <v-text-field v-model="employeeFrom.cityOfResidence"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Город проживания')">
              </v-text-field>
            </v-col>
            <v-col>
              <v-text-field v-model="employeeFrom.children"
                            :counter="1024"
                            :rules="[v=>(!v || v.length <= 1024 || $t('Не более N символов', {n:1024}))]"
                            :label="$t('Дети')">
              </v-text-field>
            </v-col>
          </v-row>
          <!--</editor-fold>-->

          <!--<editor-fold desc="seventh row (familyStatus, departmentId, englishLevel, dateOfEmployment)">-->
          <v-row>
            <v-col>
              <v-text-field v-model="employeeFrom.familyStatus"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Семейный статус')">
              </v-text-field>
            </v-col>
            <v-col>
              <v-text-field v-model="employeeFrom.foreignPassport"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Загранпаспорт')">
              </v-text-field>
            </v-col>
            <v-col>
              <v-text-field v-model="employeeFrom.englishLevel"
                            :counter="255"
                            :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                            :label="$t('Уровень английского')">
              </v-text-field>
            </v-col>
            <v-col>
              <my-date-form-component v-model="employeeFrom.dateOfEmployment"
                                      :label="$t('Дата увольнения')"
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
import {SimpleDict} from "@/store/modules/dict";
import {DateTimeUtils} from "@/components/datetimeutils";

/**
 * 32 fields from EmployeeWithAllDetails
 *  + isNew
 *  - displayName
 *  - documentFull
 *  - active
 *  - skills
 * ------------------
 * Expected field counts: 29
 */
class EmployeeFrom {
  public isNew = true;
  public id?: number;
  currentProjectId?: number;
  lastname?: string;
  firstname?: string;
  patronymicName?: string;
  departmentId?: number;
  birthday?: string;
  sex?: string;
  email?: string;
  phone?: string;
  skype?: string;
  dateOfEmployment?: string;
  levelId?: number;
  workType?: string;
  workDay?: string;
  registrationAddress?: string;
  documentSeries?: string;
  documentNumber?: string;
  documentIssuedBy?: string;
  documentIssuedDate?: string;
  foreignPassport?: string;
  cityOfResidence?: string;
  englishLevel?: string;
  familyStatus?: string;
  spouseName?: string;
  children?: string;
  dateOfDismissal?: string;
  positionId?: number;
  officeLocationId?: number;
}

@Component(
    {components: {MyDateFormComponent}}
)

export default class AdminEmployeeForm extends Vue {
  loading: boolean = false;

  @Prop({required: false})
  private input: EmployeeWithAllDetails | undefined;

  @Prop({required: true})
  private allDepartments!: Array<SimpleDict>;

  @Prop({required: true})
  private allProjects!: Array<SimpleDict>;

  @Prop({required: true})
  private allPositions!: Array<SimpleDict>;

  @Prop({required: true})
  private allLevels!: Array<SimpleDict>;

  @Prop({required: true})
  private allOfficeLocations!: Array<SimpleDict>;

  private employeeFrom = new EmployeeFrom();

  private error: String | null = null;

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
    this.employeeFrom.isNew = true;
    this.employeeFrom.id = undefined;
    this.employeeFrom.email = '';
    this.employeeFrom.currentProjectId = undefined;
    this.employeeFrom.skype = '';
    this.employeeFrom.birthday = '';
    this.employeeFrom.dateOfEmployment = '';
    this.employeeFrom.children = '';
    this.employeeFrom.cityOfResidence = '';
    this.employeeFrom.departmentId = undefined;
    this.employeeFrom.documentIssuedBy = '';
    this.employeeFrom.documentIssuedDate = '';
    this.employeeFrom.documentNumber = '';
    this.employeeFrom.documentSeries = '';
    this.employeeFrom.englishLevel = '';
    this.employeeFrom.familyStatus = '';
    this.employeeFrom.dateOfDismissal = '';
    this.employeeFrom.phone = '';
    this.employeeFrom.foreignPassport = '';
    this.employeeFrom.lastname = '';
    this.employeeFrom.firstname = '';
    this.employeeFrom.levelId = undefined;
    this.employeeFrom.officeLocationId = undefined;
    this.employeeFrom.patronymicName = '';
    this.employeeFrom.spouseName = '';
    this.employeeFrom.sex = '';
    this.employeeFrom.positionId = undefined;
    this.employeeFrom.registrationAddress = undefined;
    this.employeeFrom.workDay = undefined;
    this.employeeFrom.workType = undefined;


    if (this.input) {
      this.employeeFrom.isNew = false;
      this.employeeFrom.id = this.input.id;
      this.employeeFrom.email = this.input.email;
      this.employeeFrom.lastname = this.input.lastname;
      this.employeeFrom.firstname = this.input.firstname;
      this.employeeFrom.patronymicName = this.input.patronymicName;
      this.employeeFrom.currentProjectId = this.input.currentProjectId;
      this.employeeFrom.skype = this.input.skype;
      this.employeeFrom.birthday = this.input.birthday;
      this.employeeFrom.dateOfEmployment = this.input.dateOfEmployment;
      this.employeeFrom.children = this.input.children;
      this.employeeFrom.cityOfResidence = this.input.cityOfResidence;
      this.employeeFrom.departmentId = this.input.departmentId;
      this.employeeFrom.documentIssuedBy = this.input.documentIssuedBy;
      this.employeeFrom.documentIssuedDate = this.input.documentIssuedDate;
      this.employeeFrom.documentNumber = this.input.documentNumber;
      this.employeeFrom.documentSeries = this.input.documentSeries;
      this.employeeFrom.englishLevel = this.input.englishLevel;
      this.employeeFrom.familyStatus = this.input.familyStatus;
      this.employeeFrom.dateOfDismissal = this.input.dateOfDismissal;
      this.employeeFrom.phone = this.input.phone;
      this.employeeFrom.foreignPassport = this.input.foreignPassport;
      this.employeeFrom.levelId = this.input.levelId;
      this.employeeFrom.officeLocationId = this.input.officeLocationId;
      this.employeeFrom.spouseName = this.input.spouseName;
      this.employeeFrom.sex = this.input.sex;
      this.employeeFrom.positionId = this.input.positionId;
      this.employeeFrom.registrationAddress = this.input.registrationAddress;
      this.employeeFrom.workDay = this.input.workDay;
      this.employeeFrom.workType = this.input.workType;
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

  private validateDate(formattedDate: string, allowEmpty = true): boolean {
    return DateTimeUtils.validateFormattedDate(formattedDate, allowEmpty);
  }

}
</script>
