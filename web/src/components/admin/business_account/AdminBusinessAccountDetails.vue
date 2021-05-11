<!-- Business accounts detailed page -->
<template>
  <v-container>
    <v-card>

      <v-card-title>
        <!-- Refresh button -->
        <v-btn text icon to="/admin/ba">
          <v-icon>mdi-arrow-left</v-icon>
        </v-btn>
        <v-btn text icon @click="fetchDetails()" :loading="loading">
          <v-icon>refresh</v-icon>
        </v-btn>
        <v-divider vertical class="mr-5 ml-5"></v-divider>
        <div v-if="businessAccount" :class="{archived: businessAccount.archived}">{{ businessAccount.name }} <span
            v-if="businessAccount.description">({{ businessAccount.description }})</span></div>
        <v-spacer></v-spacer>
        <v-divider vertical class="mr-5 ml-5"></v-divider>

        <!-- Update businessAccount -->
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="col-auto">
              <v-btn text color="primary" :disabled="loading" @click="openBADialog()" icon>
                <v-icon>mdi-pencil</v-icon>
              </v-btn>
            </div>
          </template>
          <span>{{ $t('Редактировать бизнес акаунт') }}</span>
        </v-tooltip>

      </v-card-title>
    </v-card>

    <v-dialog v-model="baDialog">
      <admin-b-a-form
          v-bind:input="businessAccount"
          v-bind:allEmployees="allEmployees"
          @close="baDialog=false;fetchDetails()"></admin-b-a-form>
    </v-dialog>

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
import adminBaService, {
  BusinessAccount,
  BusinessAccountPosition
} from "@/components/admin/business_account/admin.ba.service";
import employeeService, {Employee} from "@/components/empl/employee.service";
import {Prop} from "vue-property-decorator";
import AdminBAPositions from "@/components/admin/business_account/AdminBAPositions.vue";


@Component({
      components: {AdminBAPositions, AdminBAForm}
    }
)
export default class AdminBusinessAccountDetails extends Vue {
  loading: boolean = false;
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
        }).finally(() => {
          if (showLoadingBar) this.loading = false;
        });
  }


  public openBADialog() {
    this.baDialog = true;
  }

}
</script>

<style>
.archived{
  text-decoration: line-through;
}
</style>
