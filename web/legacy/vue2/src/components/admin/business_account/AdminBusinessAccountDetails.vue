<!-- Business accounts detailed page -->
<template>
  <v-container>
    <v-card v-if="businessAccount">

      <v-card-title>
        <!-- Refresh button -->
        <v-btn text icon to="/admin/ba">
          <v-icon>mdi-arrow-left</v-icon>
        </v-btn>
        <v-btn text icon @click="fetchDetails()" :loading="loading">
          <v-icon>refresh</v-icon>
        </v-btn>
        <div v-if="businessAccount" :class="{archived: businessAccount.archived}">{{ businessAccount.name }}</div>
        <v-spacer></v-spacer>

        <!-- Update businessAccount -->
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="col-auto">
              <v-btn text color="primary" :disabled="loading" @click="openBADialog()" icon>
                <v-icon>mdi-pencil</v-icon>
              </v-btn>
            </div>
          </template>
          <span>{{ $t('Редактировать бизнес аккаунт') }}</span>
        </v-tooltip>
      </v-card-title>
      <v-card-text v-if="businessAccount.description">
        {{businessAccount.description}}
      </v-card-text>
    </v-card>

    <v-dialog v-model="baDialog">
      <admin-b-a-form
          v-bind:input="businessAccount"
          v-bind:allEmployees="allEmployees"
          @close="baDialog=false;fetchDetails()"></admin-b-a-form>
    </v-dialog>

    <!-- Managers -->
    <v-card class="mt-5 mb-5">
      <admin-managers
          ref="baManagersTable"
          v-if="businessAccount"
          :selected-object="responsibleObject"
          mode="compact"
          :title="$t('Менеджеры бизнес аккаунта')"></admin-managers>
    </v-card>

    <admin-b-a-positions
        ref="baPositions"
        :load-on-create=false
        v-bind:business-account-id="businessAccountId"></admin-b-a-positions>

  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import logger from "@/logger";
import AdminBAForm from "@/components/admin/business_account/AdminBAForm.vue";
import adminBaService, {BusinessAccount} from "@/components/admin/business_account/admin.ba.service";
import employeeService, {Employee} from "@/components/empl/employee.service";
import {Prop} from "vue-property-decorator";
import AdminBAPositions from "@/components/admin/business_account/AdminBAPositions.vue";
import AdminManagers from "@/components/admin/manager/AdminManagers.vue";
import {ManagerResponsibilityObjectId} from "@/components/admin/manager/admin.manager.service";


@Component({
      components: {AdminManagers, AdminBAPositions, AdminBAForm}
    }
)
export default class AdminBusinessAccountDetails extends Vue {
  loading = false;
  baDialog = false;

  @Prop({required: true})
  private businessAccountId!: number;

  private allEmployees: Employee[] = [];
  private businessAccount: BusinessAccount | null = null;

  /**
   * Lifecycle hook
   */
  created() {
    logger.log(`Admin BA Details component created for BA with ID ${this.businessAccountId}`);
    this.loading = true;
    employeeService.findAll().then(employees => {
          this.allEmployees = employees;
        }
    ).then(() => this.fetchDetails(false))
        .finally(() => this.loading = false);
  }

  private fetchDetails(showLoadingBar = true) {
    if (showLoadingBar) this.loading = true;
    this.businessAccount = null;
    return adminBaService.get(this.businessAccountId)
        .then(ba => {
          this.businessAccount = ba;
          if (this.$refs.baPositions) {
            (this.$refs.baPositions as AdminBAPositions).refresh();
          }
          if (this.$refs.baManagersTable){
            (this.$refs.baManagersTable as AdminManagers).refresh();
          }
        }).finally(() => {
          if (showLoadingBar) this.loading = false;
        });
  }


  public openBADialog() {
    this.baDialog = true;
  }

  private get responsibleObject(): ManagerResponsibilityObjectId|null{
    return this.businessAccount ? {
      id: this.businessAccount.id,
      type: 'business_account'
    } : null;
  }

}
</script>

<style>
.archived {
  text-decoration: line-through;
}
</style>
