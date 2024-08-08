<template>
  <span v-if="createOrUpdateBody">
  <v-autocomplete
      v-if="mode=='add'"
      v-model="createOrUpdateBody.juniorEmplId"
      :items="allEmployees"
      item-value="id" item-text="displayName"
      :label="$t('Молодой специалист')"
      :rules="[v => !!v || $t('Обязательное поле')]"
  ></v-autocomplete>
  <v-autocomplete
      v-model="createOrUpdateBody.mentorId"
      clearable
      :items="allEmployees"
      item-value="id" item-text="displayName"
      :label="$t('Ментор')"
  ></v-autocomplete>
  <v-autocomplete
      v-model="createOrUpdateBody.budgetingAccount"
      clearable
      item-value="id" item-text="name"
      :items="allBas"
      :label="$t('Бюджет из бизнес аккаунта')"
  ></v-autocomplete>
    <role-combobox :all-current-project-roles="allCurrentProjectRoles" v-model="createOrUpdateBody.role"></role-combobox>
  </span>
</template>

<script lang="ts">
import {Prop, Vue, Watch} from "vue-property-decorator";
import Component from "vue-class-component";
import employeeService, {Employee} from "@/components/empl/employee.service";
import {Getter} from "vuex-class";
import {CurrentProjectRole, SimpleDict} from "@/store/modules/dict";
import {DateTimeUtils} from "@/components/datetimeutils";
import logger from "@/logger";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import {AddJuniorRegistryBody, UpdateJuniorRegistryBody} from "@/components/udr/udr.service";
import RoleCombobox from "@/components/shared/RoleCombobox.vue";

const namespace_dict = 'dict';
@Component({
  components: {RoleCombobox, MyDateFormComponent}
})
export default class JuniorAddUpdateFormFields extends Vue {

  @Prop({required: true})
  private createOrUpdateBody!: AddJuniorRegistryBody | UpdateJuniorRegistryBody;

  @Prop({required: true})
  private mode!: 'add' | 'update';

  private allEmployees: Employee[] = [];

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  @Getter("currentProjectRoles", {namespace: namespace_dict})
  private allCurrentProjectRoles!: Array<CurrentProjectRole>;


  /**
   * Lifecycle hook
   */
  created() {
    this.$store.dispatch('dict/reloadBusinessAccounts')
        .then(() => this.$store.dispatch('dict/reloadCurrentProjectRoles'))
        .then(() =>
            employeeService.findAll().then(employees => {
                  this.allEmployees = employees;
                }
            )
        );
  }

  @Watch("createOrUpdateBody.juniorEmplId")
  private juniorSelected(junId: number) {
    const jun = this.allEmployees.find(e => e.id == junId);
    if (!jun) {
      console.error(`Employee not found ${jun}`)
      return;
    }
    this.createOrUpdateBody.budgetingAccount = jun.ba?.id || null;
    this.createOrUpdateBody.role = jun.currentProject?.role || null;
    this.createOrUpdateBody.mentorId = null;
  }

  private validateDate(formattedDate: string, allowEmpty = true): boolean {
    return DateTimeUtils.validateFormattedDate(formattedDate, allowEmpty);
  }

}

</script>

<style scoped>

</style>
