<template>
  <v-container>
    <v-row v-if="project">
      <v-col cols="3">

        <v-container class="pa-0">
          <v-row no-gutters>
            <v-col cols="4">{{ $t('Наименование') }}:</v-col>
            <v-col cols="8">{{ project.name }}</v-col>
          </v-row>
          <v-row no-gutters>
            <v-col cols="4">{{ $t('Отдел') }}:</v-col>
            <v-col cols="8">{{ project.department ? project.department.name : this.$t('Не задан') }}</v-col>
          </v-row>
          <v-row no-gutters>
            <v-col cols="4">{{ $t('Бизнес аккаунт') }}:</v-col>
            <v-col cols="8">{{ project.businessAccount ? project.businessAccount.name : this.$t('Не задан') }}</v-col>
          </v-row>
          <v-row no-gutters>
            <v-col cols="4">{{ $t('Заказчик') }}:</v-col>
            <v-col cols="8">{{ project.customer ? project.customer : $t('Не задан') }}</v-col>
          </v-row>
          <v-row no-gutters>
            <v-col cols="4">{{ $t('Начало') }}:</v-col>
            <v-col cols="8">{{ formatDate(project.startDate) }}</v-col>
          </v-row>
          <v-row no-gutters>
            <v-col cols="4">{{ $t('Окончание') }}:</v-col>
            <v-col cols="8">{{ formatDate(project.endDate) }}</v-col>
          </v-row>
          <v-row no-gutters v-if="managers()">
            <v-col cols="4">{{ $t('Менеджеры') }}:</v-col>
            <v-col cols="8">
              <v-tooltip bottom v-for="m in managers()" v-bind:key="m.id">
                <template v-slot:activator="{ on: ton, attrs: tattrs}">
                  <v-chip v-bind="tattrs" v-on="ton" class="col-auto mr-1 mb-1" outlined>
                    {{ m.employeeName }}
                  </v-chip>
                </template>
                <span>{{
                    $t("Основное направление") + ': ' + $t('MANAGER_RESPONSIBILITY_TYPE.' + m.responsibilityType)
                  }}</span>
              </v-tooltip>
            </v-col>
          </v-row>
        </v-container>
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
import {ProjectInfo} from "@/store/modules/dict.service";
import {ManagerOfObject} from "@/store/modules/dict";

@Component({
  components: {}
})
export default class ProjectInfoComponent extends Vue {
  @Prop({required: true})
  private project!: ProjectFullInfo | ProjectInfo;

  private managers(): ManagerOfObject[] | null {
    return (this.project && "managers" in this.project && this.project.managers && this.project.managers.length > 0)
        ? this.project.managers : null;
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
