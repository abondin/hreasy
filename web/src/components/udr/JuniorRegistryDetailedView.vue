<!-- Detailed information for selected junior-->
<template>
  <v-container>
    <v-card v-if="fetchError">
      <router-link to="/juniors">{{ $t('Менторство') }}</router-link>
      <v-card-text>
        <v-alert class="error">{{ fetchError }}</v-alert>
      </v-card-text>
    </v-card>
    <div v-else-if="data">
      <router-link to="/juniors">{{ $t('Менторство') }}</router-link>
      / {{ data.item.juniorEmpl.name }}
      <!-- General information and actions -->
      <v-card class="mt-5" :loading="fetchLoading">
        <v-card-text>
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title class="title d-flex">
                <span>{{ data.item.juniorEmpl.name }}</span>
                <v-spacer></v-spacer>
                <v-tooltip bottom>
                  <template v-slot:activator="{ on: ton, attrs: tattrs}">
                    <v-btn @click="data.openUpdateDialog()"
                           v-bind="tattrs" v-on="ton" class="col-auto" color="primary"
                           :disabled="false" icon>
                      <v-icon>mdi-pencil</v-icon>
                    </v-btn>
                  </template>
                  <span>{{ $t('Редактировать') }}</span>
                </v-tooltip>
                <v-tooltip bottom>
                  <template v-slot:activator="{ on: ton, attrs: tattrs}">
                    <v-btn @click="data.openDeleteDialog()"
                           v-bind="tattrs" v-on="ton" class="col-auto" color="error"
                           :disabled="false" icon>
                      <v-icon>mdi-delete</v-icon>
                    </v-btn>
                  </template>
                  <span>{{ $t('Удалить') }}</span>
                </v-tooltip>

              </v-list-item-title>
              <v-list-item-subtitle>{{ $t('Дата трудоуйства') }} :
                {{ formatDate(data.item.juniorDateOfEmployment) }} ({{ $tc('months', data.item.juniorInCompanyMonths) }}
                {{ $t('в компании') }})
              </v-list-item-subtitle>
              <v-list-item-subtitle>{{ $t('Ментор') }} :
                {{ data.item.mentor?.name || $t('Нет') }}
              </v-list-item-subtitle>
              <v-list-item-subtitle>{{ $t('Роль') }} :
                {{ data.item.role || $t('Нет') }}
              </v-list-item-subtitle>
              <v-list-item-subtitle>{{ $t('Бюджет') }} :
                {{ data.item.budgetingAccount?.name || $t('Нет') }}
              </v-list-item-subtitle>
              <v-list-item-subtitle>{{ $t('Текущий проект') }} :
                {{ data.item.currentProject?.name || $t('Нет') }}
              </v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>

          <!-- Dialogs -->
          <in-dialog-form size="lg" form-ref="updateForm" :data="data.updateDialogAction"
                          :title="()=>data.item.juniorEmpl.name + ': ' + $t('Редактирование записи в реестре')"
                          :submit-button-text="$t('Применить')" v-on:submit="fetchData">
            <template v-slot:fields>
              <junior-add-update-form-fields mode="update" :create-or-update-body="data.updateDialogAction.formData">
              </junior-add-update-form-fields>
            </template>
          </in-dialog-form>
          <in-dialog-form size="lg" form-ref="deleteForm" :data="data.deleteDialogAction"
                          :title="()=>data.item.juniorEmpl.name + ': ' + $t('Удаление')"
                          :submit-button-text="$t('Удалить')" v-on:submit="navigateRegistry">
            <template v-slot:fields>
              <p>{{ $t('Вы уверены, что хотите удалить запись?') }}</p>
            </template>
          </in-dialog-form>
        </v-card-text>
      </v-card>
    </div>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {DateTimeUtils} from "@/components/datetimeutils";
import {Prop} from "vue-property-decorator";
import juniorService from "@/components/udr/udr.service";
import {errorUtils} from "@/components/errors";
import {JuniorDetailDataContainer} from "@/components/udr/junior-details.data.container";
import InDialogForm from "@/components/shared/forms/InDialogForm.vue";
import JuniorAddUpdateFormFields from "@/components/udr/JuniorAddUpdateFormFields.vue";


@Component({
  components: {JuniorAddUpdateFormFields, InDialogForm}
})
export default class JuniorRegistryDetailedView extends Vue {

  private fetchLoading = false;
  private fetchError: string | null = null;

  @Prop({required: true})
  juniorRegistryId!: number;

  data: JuniorDetailDataContainer | null = null;

  /**
   * Lifecycle hook
   */
  created() {
    return this.fetchData();
  }


  private fetchData() {
    this.fetchLoading = true;
    this.fetchError = null;
    return juniorService.juniorDetails(this.juniorRegistryId)
        .then(data => {
              this.data = new JuniorDetailDataContainer(data);
              return this.data;
            }
        ).catch((error: any) => {
          this.fetchError = errorUtils.shortMessage(error);
        })
        .finally(() => {
          this.fetchLoading = false
        });
  }

  public navigateRegistry() {
    this.$router.push(`/juniors`);
  }


  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

  private formatDateTime(date: string): string | undefined {
    return DateTimeUtils.formatDateTimeFromIso(date);
  }

}
</script>

<style scoped>

</style>
