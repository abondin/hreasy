<template>
  <span>
  <v-autocomplete
      v-if="mode=='add'"
      v-model="createOrUpdateBody.juniorId"
      :items="allEmployees"
      item-value="id" item-text="displayName"
      :label="$t('Молодой специалист')"
      :rules="[v => !!v || $t('Обязательное поле')]"
  ></v-autocomplete>
  <v-autocomplete
      v-model="createOrUpdateBody.mentorId"
      :items="allEmployees"
      item-value="id" item-text="displayName"
      :label="$t('Ментор')"
  ></v-autocomplete>
  <v-autocomplete
      v-model="createOrUpdateBody.budgetingAccount"
      item-value="id" item-text="name"
      :items="allBas"
      :label="$t('Бюджет из бизнес аккаунта')"
  ></v-autocomplete>
  <v-text-field
      v-model="createOrUpdateBody.role"
      counter="256"
      :rules="[v=>(v && v.length <= 256 || $t('Обязательное поле. Не более N символов', {n:256}))]"
      :label="$t('Обоснование')">
  </v-text-field>
  </span>
</template>

<script lang="ts">
import {Prop, Vue, Watch} from "vue-property-decorator";
import Component from "vue-class-component";
import employeeService, {Employee} from "@/components/empl/employee.service";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";
import {DateTimeUtils} from "@/components/datetimeutils";
import logger from "@/logger";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import {AddJuniorRegistryBody, UpdateJuniorRegistryBody} from "@/components/udr/udr.service";

const namespace_dict = 'dict';
@Component({
  components: {MyDateFormComponent}
})
export default class JuniorAddUpdateFormFields extends Vue {

  @Prop({required: true})
  private createOrUpdateBody!: AddJuniorRegistryBody | UpdateJuniorRegistryBody;

  @Prop({required: true})
  private mode!: 'add' | 'update';

  private allEmployees: Employee[] = [];

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;


  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Add/Update junior form created');
    return this.$nextTick()
        .then(() => this.$store.dispatch('dict/reloadBusinessAccounts'))
        .then(() =>
            employeeService.findAll().then(employees => {
                  this.allEmployees = employees;
                }
            )
        );
  }

  @Watch("createOrUpdateBody.juniorId")
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
