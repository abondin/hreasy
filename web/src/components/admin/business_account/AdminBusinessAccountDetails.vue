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
        <div v-if="businessAccount">{{ businessAccount.name }} ({{ businessAccount.description }})</div>
        <v-spacer></v-spacer>
        <v-divider vertical class="mr-5 ml-5"></v-divider>

        <!-- Add to archive businessAccount -->
        <v-btn v-if="businessAccount && businessAccount.archivedAt" icon>
          <v-icon>mdi-close</v-icon>
        </v-btn>
        <v-tooltip v-else bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="col-auto">
              <v-btn text color="secondary" :disabled="loading" @click="openDeleteDialog()" icon>
                <v-icon>mdi-delete</v-icon>
              </v-btn>
            </div>
          </template>
          <span>{{ $t('Архивировать бизнес акаунт') }}</span>
        </v-tooltip>

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

    <!-- Delete ba dialog -->
    <v-dialog v-model="deleteDialog"
              width="500">
      <v-card>
        <v-card-title primary-title>
          {{ $t('Удаление') }}
        </v-card-title>

        <v-card-text>
          {{ $t('Вы уверены что хотите удалить запись?') }}
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
              text
              @click="deleteDialog = false">
            {{ $t('Нет') }}
          </v-btn>
          <v-btn
              color="primary"
              @click="deleteItem()">
            {{ $t('Да') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

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
  deleteDialog = false;

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
          (this.$refs.baPositions as AdminBAPositions).refresh();
        }).finally(() => {
          if (showLoadingBar) this.loading = false;
        });
  }


  public openBADialog() {
    this.baDialog = true;
  }

  private openDeleteDialog(item: BusinessAccountPosition) {
    this.$nextTick(() => {
      this.deleteDialog = true;
    });
  }

  private deleteItem() {
      adminBaService.archive(this.businessAccountId).then(() => {
        this.deleteDialog = false;
        return this.fetchDetails();
      });
  }

}
</script>
