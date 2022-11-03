<!-- Update user roles and accessible projects -->
<template>
  <v-form ref="userRolesEditForm">
    <v-card>
      <v-card-title>{{ $t('Редактирование ролей') }}</v-card-title>
      <v-card-text>
        <!-- roles -->
        <v-select
            multiple
            chips
            item-disabled="disabled"
            v-model="userRolesEditForm.roles"
            :items="allRoles"
            item-text="name"
            item-value="id"
            :label="$t('Роли')"
        ></v-select>

        <!-- accessible department -->
        <v-select
            multiple
            chips
            v-model="userRolesEditForm.accessibleDepartments"
            :items="allDepartments"
            item-text="name"
            item-value="id"
            :label="$t('Доступные отделы')"
        ></v-select>

        <!-- accessible projects -->
        <v-select
            multiple
            chips
            v-model="userRolesEditForm.accessibleProjects"
            :items="allProjects"
            item-text="name"
            item-value="id"
            :label="$t('Доступные проекты')"
        ></v-select>

        <!-- accessible business accounts -->
        <v-select
            multiple
            chips
            v-model="userRolesEditForm.accessibleBas"
            :items="allBas"
            item-text="name"
            item-value="id"
            :label="$t('Доступные бизнес аккаунты')"
        ></v-select>


        <!-- Error block -->
        <v-alert v-if="error" type="error">
          {{ error }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="closeDialog()">{{ $t('Закрыть') }}</v-btn>
        <v-btn @click="submit()" color="primary">{{ $t('Изменить') }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-form>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import {Prop, Watch} from "vue-property-decorator";
import logger from "@/logger";
import {errorUtils} from "@/components/errors";
import {SimpleDict} from "@/store/modules/dict";
import adminUserService, {
  RoleDict,
  UserRolesUpdateBody,
  UserSecurityInfo
} from "@/components/admin/users/admin.user.service";


class UserRoleForm {
  public employee: SimpleDict | null = null;
  public roles: string[] = [];
  public accessibleDepartments: number[] = [];
  public accessibleProjects: number[] = [];
  public accessibleBas: number[] = [];
}

@Component(
    {}
)

export default class AdminUserRolesForm extends Vue {
  loading = false;

  @Prop({required: true})
  private input!: UserSecurityInfo;

  @Prop({required: true, type: Array})
  private allDepartments!: Array<SimpleDict>

  @Prop({required: true, type: Array})
  private allProjects!: Array<SimpleDict>

  @Prop({required: true, type: Array})
  private allBas!: Array<SimpleDict>


  @Prop({required: true, type: Array})
  private allRoles!: Array<RoleDict>

  private userRolesEditForm = new UserRoleForm();

  private error: string | null = null;

  @Watch("input")
  private watch() {
    this.reset();
  }

  private created() {
    this.reset();
  }

  private reset() {
    this.userRolesEditForm.employee = this.input.employee;
    this.userRolesEditForm.roles = this.input.roles;
    this.userRolesEditForm.accessibleDepartments = this.input.accessibleDepartments;
    this.userRolesEditForm.accessibleProjects = this.input.accessibleProjects;
    this.userRolesEditForm.accessibleBas = this.input.accessibleBas;
  }

  private closeDialog() {
    this.reset();
    this.$nextTick(function () {
      this.$emit('close');
    })
  }

  private submit() {
    const form: any = this.$refs.userRolesEditForm;
    if (form.validate()) {
      const body = {
        roles: this.userRolesEditForm.roles,
        accessibleDepartments: this.userRolesEditForm.accessibleDepartments,
        accessibleProjects: this.userRolesEditForm.accessibleProjects,
        accessibleBas: this.userRolesEditForm.accessibleBas
      } as UserRolesUpdateBody;
      logger.log(`Update user with emplId ${this.userRolesEditForm.employee!.id} with roles ${JSON.stringify(body)}`);
      return adminUserService.updateRolesAndAccessibleProjects(this.userRolesEditForm.employee!.id, body)
          .then(() => {
            this.$emit('close');
          })
          .catch(error => {
            this.error = errorUtils.shortMessage(error);
          });
    }
  }

}
</script>
