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
        <v-card-title>
          <span>{{ data.item.juniorEmpl.name }}</span>
          <v-spacer></v-spacer>
          <v-tooltip bottom>
            <template v-slot:activator="{ on: ton, attrs: tattrs}">
              <v-btn @click="data.openGraduateDialog(Boolean(data.item.graduation))"
                     v-bind="tattrs" v-on="ton" class="col-auto" color="success"
                     :disabled="false" icon>
                <v-icon>mdi-school</v-icon>
              </v-btn>
            </template>
            <span>{{ $t('Закончить обучение') }}</span>
          </v-tooltip>
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
        </v-card-title>
        <v-card-text>
          <junior-info-view v-if="data?.item" :item="data.item"></junior-info-view>

          <!-- Dialogs -->
          <in-dialog-form size="lg" form-ref="updateForm" :data="data.updateDialogAction"
                          :title="()=>data.item.juniorEmpl.name + ': ' + $t('редактирование записи в реестре')"
                          :submit-button-text="$t('Применить')" v-on:submit="fetchData">
            <template v-slot:fields>
              <junior-add-update-form-fields mode="update" :create-or-update-body="data.updateDialogAction.formData">
              </junior-add-update-form-fields>
            </template>
          </in-dialog-form>
          <in-dialog-form size="lg" form-ref="deleteForm" :data="data.deleteDialogAction"
                          :title="()=>data.item.juniorEmpl.name + ': ' + $t('удаление')"
                          :submit-button-text="$t('Удалить')" v-on:submit="navigateRegistry">
            <template v-slot:fields>
              <p>{{ $t('Вы уверены, что хотите удалить запись?') }}</p>
            </template>
          </in-dialog-form>
          <in-dialog-form v-if="data.graduateDialogAction" size="lg" form-ref="graduateForm"
                          :data="data.graduateDialogAction"
                          :title="()=>data.item.juniorEmpl.name + ': ' +(data.item.graduation? $t('отменить') : $t('закончить обучение?'))"
                          :submit-button-text="$t('Применить')" v-on:submit="fetchData">
            <template v-slot:fields>
              <span v-if="!data.item.graduation">
              <v-textarea
                  v-if="data.graduateDialogAction.formData"
                  v-model="data.graduateDialogAction.formData.comment"
                  :rules="[v=>(!v || v.length <= 1024 || $t('Не более N символов', {n:1024}))]"
                  :label="$t('Комментарий')">
              </v-textarea>
              </span>
              <p v-else>{{ $t('Вы уверены, что хотите отменить решение об окончании обучения?') }}</p>
            </template>
          </in-dialog-form>
        </v-card-text>
      </v-card>

      <junior-info-reports :data="data"  v-on:submit="fetchData"></junior-info-reports>
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
import JuniorInfoReports from "@/components/udr/info/JuniorInfoReports.vue";
import JuniorInfoView from "@/components/udr/info/JuniorInfoView.vue";


@Component({
  components: {JuniorInfoReports, JuniorInfoView, JuniorAddUpdateFormFields, InDialogForm}
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
