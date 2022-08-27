<template>
  <v-container>
    <v-row v-if="project">
      <v-col cols="auto" style="min-width: 400px">
        <my-description-list :items="[
            {t:$t('Наименование'), d:project.name}
            ,{t:$t('Отдел'), d:project.department ? project.department.name : $t('Не задан')}
            ,{t:$t('Бизнес аккаунт'), d:project.businessAccount ? project.businessAccount.name : $t('Не задан')}
            ,{t:$t('Заказчик'), d:(project.customer ? project.customer : $t('Не задан'))}
            ,{t:$t('Начало'), d:formatDate(project.startDate)}
            ,{t:$t('Окончание'), d:formatDate(project.endDate)}
        ]">
        </my-description-list>
        <div v-if="managers()">
          <p>{{ $t('Менеджеры') }}:</p>
          <v-col cols="auto" v-for="m in managers()" v-bind:key="m.id">
            <v-tooltip bottom>
              <template v-slot:activator="{ on: ton, attrs: tattrs}">
                <v-chip v-bind="tattrs" v-on="ton" class="col-auto">
                  {{ m.employeeName }}
                </v-chip>
              </template>
              <span>{{
                  $t("Основное направление") + ': ' + $t('MANAGER_RESPONSIBILITY_TYPE.' + m.responsibilityType)
                }}</span>
            </v-tooltip>
          </v-col>
        </div>
      </v-col>
      <v-col v-if="project.info">
        <v-card>
          <v-card-text>
            <div v-html="project.info" :style="{'height': height}" class="overflow-y-auto"></div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Vue from "vue";
import {ProjectFullInfo} from "../admin/project/admin.project.service";
import {Prop} from "vue-property-decorator";
import Component from "vue-class-component";
import {DateTimeUtils} from "@/components/datetimeutils";
import MyDescriptionList from "@/components/shared/MyDescriptionList.vue";
import {ProjectInfo} from "@/store/modules/dict.service";
import {ManagerOfObject} from "@/store/modules/dict";

@Component({
  components: {MyDescriptionList}
})
export default class ProjectInfoComponent extends Vue {
  @Prop({required: true})
  private project!: ProjectFullInfo | ProjectInfo;

  private managers(): ManagerOfObject[] {
    return (this.project && this.project.managers && this.project.managers.length > 0) ? this.project.managers : null;
  }

  @Prop({required: false, default: "250px"})
  private height!: string;

  private formatDate(date: string | undefined): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }
}
</script>

<style scoped>

</style>
